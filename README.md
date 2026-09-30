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

## In-Place Updates (No Uninstall Required)

ExamPrep CSV is designed so newer versions published on GitHub install seamlessly as a normal Android **in-place update**. Your local CSVs, notes, HTML files, attempt logs, and reels are completely preserved.

### 1. Stable Identity
- **Application ID (Package Name):** `com.aistudio.upsiprep.kxmpzq` (permanently fixed).
- **Version Code:** Automatically incremented on every release (`100 + GITHUB_RUN_NUMBER`). Android requires `versionCode` to be strictly greater than the installed version.
- **Signing Key:** Every release APK must be signed with the **exact same release keystore**.

> ⚠️ **Important Sideload Rule:**
> If your phone ever displays "App not installed" or asks to uninstall first when installing an APK update:
> **DO NOT uninstall the app!** Uninstalling wipes your local notes, attempts, and CSV files.
> The prompt occurs because either the APK package name changed or the APK was signed with a different keystore certificate than the currently installed version. Ensure both builds are signed with the same release key.

### 2. One-Time Release Keystore Setup

To enable signed in-place updates across GitHub Releases:

1. **Generate your release keystore** using the included helper script:
   ```bash
   ./scripts/generate-release-keystore.sh
   ```
   Or manually with `keytool`:
   ```bash
   keytool -genkey -v -keystore release.keystore -alias upload -keyalg RSA -keysize 2048 -validity 10000
   ```

2. **Add GitHub Actions Secrets** in your repository under **Settings → Secrets and variables → Actions**:
   - `KEYSTORE_BASE64`: Output of `base64 -w 0 release.keystore`
   - `KEYSTORE_PASSWORD`: The password for your keystore
   - `KEY_ALIAS`: `upload`
   - `KEY_PASSWORD`: The password for the key alias

3. **Local Builds:**
   Copy `key.properties.example` to `key.properties` (this file is ignored by Git) to sign release builds locally:
   ```bash
   cp key.properties.example key.properties
   ./gradlew assembleRelease
   ```

### 3. In-App Update Checker

Tap the **Update** icon (down-arrow device icon) in the top bar near the Refresh button:
- Queries the latest release from the GitHub repository (`narayan43/study-app-`).
- Compares installed vs. remote version code and release tags.
- Downloads the APK directly to app cache and initiates the system package installer session.
- Seamlessly updates the application in-place while keeping all user data intact.

## Download the APK

- **From GitHub Releases:** Download `ExamPrepCSV-vX.Y.apk` directly from **GitHub → Releases** when a tag `v*` is published.
  To publish a new release, tag your commit and push:
  ```bash
  git tag v1.0.2
  git push origin v1.0.2
  ```
- **From GitHub Actions:** Navigate to **GitHub → Actions → workflow Build APK → latest run → Artifacts → `app-debug`**.

## Build locally

Build the debug APK using the Gradle wrapper:

```bash
./gradlew assembleDebug
```

Build the signed release APK (when `key.properties` or environment variables exist):
```bash
./gradlew assembleRelease
```

APK output locations:
```
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/release/app-release.apk
```

## Requirements

- **Android 7.0+** (minSdk 24).
- **Jetpack Compose & Material 3**.
- **100% Offline:** No account, registration, or server login required.
