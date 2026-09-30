# ExamPrep CSV

[![Build APK](https://github.com/narayan43/study-app-/actions/workflows/build-apk.yml/badge.svg)](https://github.com/narayan43/study-app-/actions/workflows/build-apk.yml)

Offline exam practice app. Questions, notes, and reels live in a `Data/` folder of CSV files. No server.

## What it does

- **5 tabs:** Dashboard, Test, Notes, Mistakes, Reels.
- **Tree hierarchy:** Exam → Subject → Chapter.
- **Test:** Multiple-choice questions with answer feedback, stopwatch timer, and an optional expandable notes drawer directly under the question.
- **Notes:** Markdown, text, and PDF study notes loaded from `notes/files/` with chapter-based revision test triggering.
- **Reels:** Short vertical video lectures loaded from `videos/videos.csv` (local video files or URLs); testing from a video slice tests only linked question IDs.
- **Mistakes:** Automatic aggregation of chapters containing incorrect attempts for targeted re-testing and review.
- **Dashboard:** Progress tracking showing total questions, test attempts, study streak, study time, and weak vs. strong chapters computed directly from `logs/`.

## How data works

All application data is stored in standard CSV files and asset folders inside a single root directory:

```
Data/
  questions/questions.csv
  notes/notes.csv
  notes/files/
  videos/videos.csv
  videos/files/
  links/note_questions.csv
  links/video_questions.csv
  logs/attempts.csv
  logs/notes_usage.csv
  logs/video_usage.csv
```

### Exact CSV Headers

- **`questions/questions.csv`**
  ```csv
  exam,question_id,subject,chapter,topic,question_text,option_a,option_b,option_c,option_d,question_image,option_a_image,option_b_image,option_c_image,option_d_image,correct_answer
  ```

- **`notes/notes.csv`**
  ```csv
  note_id,exam,subject,chapter,topic,title,file_path,note_type
  ```

- **`videos/videos.csv`**
  ```csv
  video_id,exam,subject,chapter,topic,title,video_path,duration_sec
  ```

- **`links/note_questions.csv`**
  ```csv
  note_id,question_id
  ```

- **`links/video_questions.csv`**
  ```csv
  video_id,question_id
  ```

- **`logs/attempts.csv`**
  ```csv
  attempt_id,question_id,exam,subject,chapter,chosen_answer,is_correct,time_spent_sec,timestamp
  ```

- **`logs/notes_usage.csv`**
  ```csv
  event_id,note_id,exam,subject,chapter,opened_at,closed_at,time_spent_sec
  ```

- **`logs/video_usage.csv`**
  ```csv
  event_id,video_id,exam,subject,chapter,opened_at,closed_at,time_spent_sec,started_test
  ```

### Managing Data

1. **Linking the folder:** In the app, go to **Dashboard → Link Data Folder** (or tap the SAF status chip in the top bar) to select your `Data/` folder using Android Storage Access Framework (SAF), then tap **Reload**.
2. **Logging:** Do **not** hand-edit files in `logs/` (`attempts.csv`, `notes_usage.csv`, `video_usage.csv`); the app automatically writes and updates study metrics and history to these logs.
3. **Adding content manually or in-app:**
   - **Add a question:** Append a row to `questions/questions.csv` with a unique `question_id` (e.g. `Q081`), all 4 options, and `correct_answer` (`A`, `B`, `C`, or `D`).
   - **Add a note:** Append a row to `notes/notes.csv` with a unique `note_id` (e.g. `N009`) and place the corresponding document file in `notes/files/`.
   - **Add a reel:** Append a row to `videos/videos.csv` with a unique `video_id` (e.g. `V007`) and place the video file in `videos/files/` or provide a direct URL.
   - **Link IDs:** Add pairs of `(note_id, question_id)` in `links/note_questions.csv` or `(video_id, question_id)` in `links/video_questions.csv` to connect study material with revision questions.

## Screens

- **Dashboard:** Displays overall study analytics, question counts, daily streaks, time spent, weak and strong chapters, and folder linking status.
- **Test:** Drill-down selector to browse exams, subjects, and chapters, practice MCQ tests, review answer explanations, and author new questions or import questions.csv.
- **Notes:** Document reader to view chapter notes in Markdown, plain text, or PDF format, and launch targeted tests for the active note or chapter.
- **Mistakes:** Dedicated review screen grouping past incorrect question attempts by chapter for targeted re-testing and note review.
- **Reels:** Vertical video lecture feed with video playback, chapter navigation, and one-tap test generation from questions linked to the current reel.
- **First-Run Demo Data:** If no external `Data/` folder is linked, the app automatically runs in Demo Mode using embedded sample data so all tabs are fully functional immediately.

## Download the APK

- **From GitHub Actions:** Navigate to **GitHub → Actions → workflow Build APK → latest green run → Artifacts → `app-debug`**.
- **From GitHub Releases:** Download `ExamPrep-vX.Y.apk` directly from **GitHub → Releases** when a tag `v*` is published.
  To publish a new release APK, tag your commit and push the tag (e.g. tag `v1.0.1` and push tags):
  ```bash
  git tag v1.0.1
  git push origin v1.0.1
  ```
  The `Release APK` workflow will automatically build, package, and attach `ExamPrep-v1.0.1.apk` to the GitHub release.

## Build locally

Build the debug APK using the Gradle wrapper:

```bash
./gradlew assembleDebug
```

APK output location:
```
app/build/outputs/apk/debug/app-debug.apk
```

## Release Signing (Optional)

The GitHub Actions workflows produce and upload the debug APK by default so that CI runs succeed immediately without requiring signing secrets.

If you wish to configure signed release builds in GitHub Actions:
1. Go to your GitHub repository **Settings → Secrets and variables → Actions**.
2. Add the following repository secrets:
   - `KEYSTORE_BASE64`: Base64-encoded string of your `.jks` or `.keystore` file.
   - `KEYSTORE_PASSWORD`: Password for your keystore.
   - `KEY_ALIAS`: Alias of your release signing key.
   - `KEY_PASSWORD`: Password for the key alias.
3. Build locally with `./gradlew assembleRelease` or enable release signing in your CI workflow.

## Requirements

- **Android 7.0+** (minSdk 24).
- **Jetpack Compose & Material 3**.
- **100% Offline:** No account, registration, or server login required.
