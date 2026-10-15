# Changelog

All notable changes to the ExamPrep CSV Android application are documented below.

## [Task 1] - In-App Updater Hardening
- **SHA-256 Checksum Verification:** Computed and verified cryptographic SHA-256 hash for APK assets before invoking package installation; aborted and discarded files on mismatch.
- **Secure Download Redirects:** Enforced strict HTTPS and restricted allowed download/redirect hosts to `github.com` and `*.githubusercontent.com`.
- **Structured Version Asset:** Replaced regex-based guesswork from release text with `version.json` release asset parsing.
- **CI Release Workflow:** Updated `.github/workflows/release-apk.yml` to publish `version.json` and `<APK_NAME>.sha256` alongside release APK without modifying any signing keys or credentials.
- **Unit Testing:** Added `AppUpdateManagerTest` covering semver comparisons, redirect security filtering, SHA-256 verification, and `version.json` parsing.

## [Task 2] - Spaced-Repetition Scheduler & Streak
- **Streak-Driven Intervals:** Replaced lifetime `timesCorrect` interval calculation with consecutive-correct streak logic (1, 3, 7, 14 days). Wrong answers reset the streak to 0 with a 10-minute re-test interval. Skip preserves streak and schedules due in 10 minutes.
- **Pure ReviewScheduler:** Extracted pure Kotlin `ReviewScheduler` with zero Android dependencies, accompanied by complete unit test suite in `ReviewSchedulerTest`.
- **Backward-Compatible Schema:** Added `consecutive_correct` as 9th column to `logs/review_state.csv` while ensuring legacy 8-column files load cleanly without data loss.

## [Task 3] - Safe File Writing & Storage Integrity
- **No Silent Storage Fallback:** Replaced silent `catch (_: Exception)` and fallbacks in `appendToFile`, `writeOrCopyFile`, and `overwriteFile` with explicit `Result<Unit>` returns and log reporting. If SAF write fails, error is exposed in UI and never silently diverted to local directory.
- **Crash-Safe Overwrite:** Implemented crash-safe file replacement for `saveReviewState()` and CSV overwrites using staged `.tmp` files.
- **UI Error Feedback:** Exposed `storageError` StateFlow in `ExamPrepViewModel` to immediately alert the user if disk writes or SAF permissions fail.

## [Task 4] - Quote-Aware CSV Parsing & Formatting
- **RFC 4180 Quote-Aware Parser:** Implemented `parseCsvRows` processing entire CSV content character-by-character to preserve newlines, commas, and quotes inside quoted fields.
- **Newline Preservation in Writer:** Updated `escape()` to retain multiline text rather than replacing newlines with spaces.
- **Tightened Header Validation:** Updated `validateQuestionsHeader` to verify exact token matching for required columns (`exam`, `question_id`, `question_text`, `correct_answer`).
- **Unit Testing:** Added `CsvCorrectnessTest` covering quotes, commas, multiline fields, CRLF line endings, Hindi/Unicode text, empty fields, and legacy short rows.

## [Task 5] - Thread Safety & UI State Persistence
- **Concurrency Mutex:** Guarded in-memory caches and disk operations in `DataService` with a `Mutex` to prevent concurrent write and read races between `Dispatchers.IO` and UI threads.
- **Configuration Change Resilience:** Replaced transient `remember` with `rememberSaveable` in `TestPlayerScreen` for current question index, selected option, submission state, and question timer, ensuring phone rotation does not reset state.
- **Exposed Reactive StateFlows:** Exposed categories (`availableExams`, `availableSubjects`, etc.) and storage errors as `StateFlow` on `ExamPrepViewModel`.
