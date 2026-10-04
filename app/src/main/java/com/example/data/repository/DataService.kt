package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import com.example.data.csv.CsvHelper
import com.example.data.model.AttemptLog
import com.example.data.model.ChapterStatItem
import com.example.data.model.DailyAttemptStat
import com.example.data.model.DashboardStats
import com.example.data.model.DrillDownFilter
import com.example.data.model.NoteItem
import com.example.data.model.NoteQuestionLink
import com.example.data.model.NoteUsageLog
import com.example.data.model.QuestionItem
import com.example.data.model.VideoItem
import com.example.data.model.VideoQuestionLink
import com.example.data.model.VideoUsageLog
import com.example.data.sample.DummyDataGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.UUID

class DataService(private val context: Context) {

    private val prefs = context.getSharedPreferences("exam_prep_prefs", Context.MODE_PRIVATE)
    private var externalTreeUri: Uri? = null

    // In-memory cache loaded directly from CSVs
    private var cachedQuestions: List<QuestionItem> = emptyList()
    private var cachedNotes: List<NoteItem> = emptyList()
    private var cachedVideos: List<VideoItem> = emptyList()
    private var cachedNoteQuestions: List<NoteQuestionLink> = emptyList()
    private var cachedVideoQuestions: List<VideoQuestionLink> = emptyList()
    private var cachedAttempts: List<AttemptLog> = emptyList()
    private var cachedNoteUsage: List<NoteUsageLog> = emptyList()
    private var cachedVideoUsage: List<VideoUsageLog> = emptyList()
    private var cachedTree: List<CsvHelper.TreeRow> = emptyList()

    private val localDataDir = File(context.filesDir, "Data")

    init {
        val uriStr = prefs.getString("data_folder_uri", null)
        if (uriStr != null) {
            externalTreeUri = Uri.parse(uriStr)
        }
        // First-run internal fallback & demo data verification (never wipe if files already exist)
        if (externalTreeUri == null) {
            if (!localDataDir.exists() || localDataDir.listFiles().isNullOrEmpty()) {
                localDataDir.mkdirs()
                DummyDataGenerator.generateDummyDataTree(localDataDir)
            }
        }
    }

    fun isFolderLinked(): Boolean = externalTreeUri != null
    fun getCurrentFolderDisplay(): String = externalTreeUri?.toString() ?: localDataDir.absolutePath

    fun isDarkTheme(): Boolean = prefs.getBoolean("is_dark_theme", false)
    fun setDarkTheme(isDark: Boolean) {
        prefs.edit().putBoolean("is_dark_theme", isDark).apply()
    }

    suspend fun linkDataFolder(uri: Uri) = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (_: Exception) {}

        externalTreeUri = uri
        prefs.edit().putString("data_folder_uri", uri.toString()).apply()
        ensureMissingLogFilesOnExternal(uri)
        reloadData()
    }

    suspend fun unlinkFolder() = withContext(Dispatchers.IO) {
        externalTreeUri = null
        prefs.edit().remove("data_folder_uri").apply()
        if (!localDataDir.exists()) {
            localDataDir.mkdirs()
            DummyDataGenerator.generateDummyDataTree(localDataDir)
        }
        reloadData()
    }

    suspend fun resetToDefaultData() = withContext(Dispatchers.IO) {
        unlinkFolder()
        DummyDataGenerator.generateDummyDataTree(localDataDir)
        reloadData()
    }

    private fun ensureMissingLogFilesOnExternal(treeUri: Uri) {
        val filesWithHeaders = listOf(
            "logs/attempts.csv" to CsvHelper.ATTEMPTS_HEADER,
            "logs/notes_usage.csv" to CsvHelper.NOTES_USAGE_HEADER,
            "logs/video_usage.csv" to CsvHelper.VIDEO_USAGE_HEADER
        )
        for ((relPath, header) in filesWithHeaders) {
            getOrCreateDocumentFile(treeUri, relPath, header)
        }
    }

    suspend fun reloadData() = withContext(Dispatchers.IO) {
        // Read questions
        val qCsv = readCsvContent("questions/questions.csv", CsvHelper.QUESTIONS_HEADER)
        cachedQuestions = CsvHelper.parseQuestions(qCsv)

        // Read notes
        val nCsv = readCsvContent("notes/notes.csv", CsvHelper.NOTES_HEADER)
        cachedNotes = CsvHelper.parseNotes(nCsv)

        // Read videos
        val vCsv = readCsvContent("videos/videos.csv", CsvHelper.VIDEOS_HEADER)
        cachedVideos = CsvHelper.parseVideos(vCsv)

        // Read links
        val nqCsv = readCsvContent("links/note_questions.csv", CsvHelper.NOTE_QUESTIONS_HEADER)
        cachedNoteQuestions = CsvHelper.parseNoteQuestions(nqCsv)

        val vqCsv = readCsvContent("links/video_questions.csv", CsvHelper.VIDEO_QUESTIONS_HEADER)
        cachedVideoQuestions = CsvHelper.parseVideoQuestions(vqCsv)

        // Read logs
        val attCsv = readCsvContent("logs/attempts.csv", CsvHelper.ATTEMPTS_HEADER)
        cachedAttempts = CsvHelper.parseAttempts(attCsv)

        val nuCsv = readCsvContent("logs/notes_usage.csv", CsvHelper.NOTES_USAGE_HEADER)
        cachedNoteUsage = CsvHelper.parseNotesUsage(nuCsv)

        val vuCsv = readCsvContent("logs/video_usage.csv", CsvHelper.VIDEO_USAGE_HEADER)
        cachedVideoUsage = CsvHelper.parseVideoUsage(vuCsv)
    }

    // --- Query APIs ---
    fun listExams(): List<String> {
        val qExams = cachedQuestions.map { it.exam }
        val nExams = cachedNotes.map { it.exam }
        val vExams = cachedVideos.map { it.exam }
        return (qExams + nExams + vExams).distinct().filter { it.isNotBlank() }
    }

    fun listSubjects(exam: String?): List<String> {
        val qSubs = cachedQuestions.filter { exam == null || it.exam.equals(exam, ignoreCase = true) }.map { it.subject }
        val nSubs = cachedNotes.filter { exam == null || it.exam.equals(exam, ignoreCase = true) }.map { it.subject }
        val vSubs = cachedVideos.filter { exam == null || it.exam.equals(exam, ignoreCase = true) }.map { it.subject }
        return (qSubs + nSubs + vSubs).distinct().filter { it.isNotBlank() }
    }

    fun listChapters(exam: String?, subject: String?): List<String> {
        val qChaps = cachedQuestions.filter {
            (exam == null || it.exam.equals(exam, ignoreCase = true)) &&
            (subject == null || it.subject.equals(subject, ignoreCase = true))
        }.map { it.chapter }

        val nChaps = cachedNotes.filter {
            (exam == null || it.exam.equals(exam, ignoreCase = true)) &&
            (subject == null || it.subject.equals(subject, ignoreCase = true))
        }.map { it.chapter }

        val vChaps = cachedVideos.filter {
            (exam == null || it.exam.equals(exam, ignoreCase = true)) &&
            (subject == null || it.subject.equals(subject, ignoreCase = true))
        }.map { it.chapter }

        return (qChaps + nChaps + vChaps).distinct().filter { it.isNotBlank() }
    }

    fun listTopics(exam: String?, subject: String?, chapter: String?): List<String> {
        val qTopics = cachedQuestions.filter {
            (exam == null || it.exam.equals(exam, ignoreCase = true)) &&
            (subject == null || it.subject.equals(subject, ignoreCase = true)) &&
            (chapter == null || it.chapter.equals(chapter, ignoreCase = true))
        }.map { it.topic }

        val nTopics = cachedNotes.filter {
            (exam == null || it.exam.equals(exam, ignoreCase = true)) &&
            (subject == null || it.subject.equals(subject, ignoreCase = true)) &&
            (chapter == null || it.chapter.equals(chapter, ignoreCase = true))
        }.map { it.topic }

        val vTopics = cachedVideos.filter {
            (exam == null || it.exam.equals(exam, ignoreCase = true)) &&
            (subject == null || it.subject.equals(subject, ignoreCase = true)) &&
            (chapter == null || it.chapter.equals(chapter, ignoreCase = true))
        }.map { it.topic }

        return (qTopics + nTopics + vTopics).distinct().filter { it.isNotBlank() }
    }

    fun listVideoTitles(exam: String?, subject: String?, chapter: String?, topic: String?): List<String> {
        return cachedVideos.filter { v ->
            (exam == null || v.exam.equals(exam, ignoreCase = true)) &&
            (subject == null || v.subject.equals(subject, ignoreCase = true)) &&
            (chapter == null || v.chapter.equals(chapter, ignoreCase = true)) &&
            (topic == null || v.topic.equals(topic, ignoreCase = true))
        }.map { it.title }.distinct().filter { it.isNotBlank() }
    }

    fun findCanonicalMatch(input: String, candidates: List<String>): String {
        val trimmed = input.trim()
        val match = candidates.firstOrNull { it.trim().equals(trimmed, ignoreCase = true) }
        return match ?: trimmed
    }

    // Gap 5: Multi-width slices (Exam / Subject / Chapter / Topic)
    fun questionsFor(filter: DrillDownFilter): List<QuestionItem> {
        return cachedQuestions.filter { q ->
            (filter.exam == null || q.exam.equals(filter.exam, ignoreCase = true)) &&
            (filter.subject == null || q.subject.equals(filter.subject, ignoreCase = true)) &&
            (filter.chapter == null || q.chapter.equals(filter.chapter, ignoreCase = true)) &&
            (filter.topic == null || q.topic.equals(filter.topic, ignoreCase = true))
        }
    }

    fun notesFor(filter: DrillDownFilter): List<NoteItem> {
        return cachedNotes.filter { n ->
            (filter.exam == null || n.exam.equals(filter.exam, ignoreCase = true)) &&
            (filter.subject == null || n.subject.equals(filter.subject, ignoreCase = true)) &&
            (filter.chapter == null || n.chapter.equals(filter.chapter, ignoreCase = true)) &&
            (filter.topic == null || n.topic.equals(filter.topic, ignoreCase = true))
        }
    }

    fun videosFor(filter: DrillDownFilter): List<VideoItem> {
        return cachedVideos.filter { v ->
            (filter.exam == null || v.exam.equals(filter.exam, ignoreCase = true)) &&
            (filter.subject == null || v.subject.equals(filter.subject, ignoreCase = true)) &&
            (filter.chapter == null || v.chapter.equals(filter.chapter, ignoreCase = true)) &&
            (filter.topic == null || v.topic.equals(filter.topic, ignoreCase = true))
        }
    }

    fun questionsForNote(noteId: String): List<QuestionItem> {
        val qIds = cachedNoteQuestions.filter { it.noteId.trim() == noteId.trim() }.map { it.questionId.trim() }.toSet()
        return cachedQuestions.filter { it.questionId.trim() in qIds }
    }

    fun questionsForVideo(videoId: String): List<QuestionItem> {
        val qIds = cachedVideoQuestions.filter { it.videoId.trim() == videoId.trim() }.map { it.questionId.trim() }.toSet()
        return cachedQuestions.filter { it.questionId.trim() in qIds }
    }

    fun notesForQuestion(questionId: String): List<NoteItem> {
        val nIds = cachedNoteQuestions.filter { it.questionId.trim() == questionId.trim() }.map { it.noteId.trim() }.toSet()
        return cachedNotes.filter { it.noteId.trim() in nIds }
    }

    fun videosForQuestion(questionId: String): List<VideoItem> {
        val vIds = cachedVideoQuestions.filter { it.questionId.trim() == questionId.trim() }.map { it.videoId.trim() }.toSet()
        return cachedVideos.filter { it.videoId.trim() in vIds }
    }

    fun getQuestionsByIds(ids: List<String>): List<QuestionItem> {
        val set = ids.map { it.trim() }.toSet()
        return cachedQuestions.filter { it.questionId.trim() in set }
    }

    fun getMistakesGrouped(): Map<String, Map<String, Map<String, List<String>>>> {
        val wrongAttempts = cachedAttempts.filter { it.isCorrect == 0 }
        val map = mutableMapOf<String, MutableMap<String, MutableMap<String, MutableList<String>>>>()

        for (a in wrongAttempts) {
            val exam = if (a.exam.isNotBlank()) a.exam else "General"
            val subject = if (a.subject.isNotBlank()) a.subject else "General"
            val chapter = if (a.chapter.isNotBlank()) a.chapter else "General"

            val subMap = map.getOrPut(exam) { mutableMapOf() }
            val chapMap = subMap.getOrPut(subject) { mutableMapOf() }
            val qList = chapMap.getOrPut(chapter) { mutableListOf() }
            if (a.questionId !in qList) {
                qList.add(a.questionId)
            }
        }
        return map
    }

    // --- Media Resolution ---
    fun resolveMediaUri(relPath: String): Uri? {
        if (relPath.isBlank()) return null
        if (relPath.startsWith("http://") || relPath.startsWith("https://")) {
            return Uri.parse(relPath)
        }
        val cleanRel = relPath.trimStart('/')
        val uri = externalTreeUri
        if (uri != null) {
            val doc = findDocumentFile(uri, cleanRel)
            if (doc != null && doc.exists()) {
                return doc.uri
            }
        }
        // Fallback to local
        val f = File(localDataDir, cleanRel)
        return if (f.exists()) Uri.fromFile(f) else null
    }

    // --- Write Operations (Gap 1: Writes ISO-8601 with offset) ---
    suspend fun appendAttempt(
        question: QuestionItem,
        chosenAnswer: String,
        isCorrect: Boolean,
        timeSpentSec: Int
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val isoTimestamp = CsvHelper.formatIsoTimestamp(now)
        val log = AttemptLog(
            attemptId = "att_${UUID.randomUUID().toString().take(8)}",
            questionId = question.questionId,
            exam = question.exam,
            subject = question.subject,
            chapter = question.chapter,
            chosenAnswer = chosenAnswer,
            isCorrect = if (isCorrect) 1 else 0,
            timeSpentSec = timeSpentSec,
            timestamp = now,
            rawTimestamp = isoTimestamp
        )
        cachedAttempts = cachedAttempts + log
        appendToFile("logs/attempts.csv", CsvHelper.formatAttemptLine(log), CsvHelper.ATTEMPTS_HEADER)
    }

    suspend fun appendNoteUsage(
        note: NoteItem,
        openedAt: Long,
        closedAt: Long,
        timeSpentSec: Int
    ) = withContext(Dispatchers.IO) {
        val log = NoteUsageLog(
            eventId = "nu_${UUID.randomUUID().toString().take(8)}",
            noteId = note.noteId,
            exam = note.exam,
            subject = note.subject,
            chapter = note.chapter,
            openedAt = openedAt,
            closedAt = closedAt,
            timeSpentSec = timeSpentSec,
            rawOpenedAt = CsvHelper.formatIsoTimestamp(openedAt),
            rawClosedAt = CsvHelper.formatIsoTimestamp(closedAt)
        )
        cachedNoteUsage = cachedNoteUsage + log
        appendToFile("logs/notes_usage.csv", CsvHelper.formatNoteUsageLine(log), CsvHelper.NOTES_USAGE_HEADER)
    }

    suspend fun appendVideoUsage(
        video: VideoItem,
        openedAt: Long,
        closedAt: Long,
        timeSpentSec: Int,
        startedTest: Boolean
    ) = withContext(Dispatchers.IO) {
        val log = VideoUsageLog(
            eventId = "vu_${UUID.randomUUID().toString().take(8)}",
            videoId = video.videoId,
            exam = video.exam,
            subject = video.subject,
            chapter = video.chapter,
            openedAt = openedAt,
            closedAt = closedAt,
            timeSpentSec = timeSpentSec,
            startedTest = if (startedTest) 1 else 0,
            rawOpenedAt = CsvHelper.formatIsoTimestamp(openedAt),
            rawClosedAt = CsvHelper.formatIsoTimestamp(closedAt)
        )
        cachedVideoUsage = cachedVideoUsage + log
        appendToFile("logs/video_usage.csv", CsvHelper.formatVideoUsageLine(log), CsvHelper.VIDEO_USAGE_HEADER)
    }

    // --- ID Allocator & Authoring Append APIs (Step 1) ---
    fun nextQuestionId(): String {
        var maxId = 0
        for (q in cachedQuestions) {
            val num = extractNumericSuffix(q.questionId, "Q")
            if (num != null && num > maxId) {
                maxId = num
            }
        }
        return String.format(Locale.US, "Q%03d", maxId + 1)
    }

    fun nextNoteId(): String {
        var maxId = 0
        for (n in cachedNotes) {
            val num = extractNumericSuffix(n.noteId, "N")
            if (num != null && num > maxId) {
                maxId = num
            }
        }
        return String.format(Locale.US, "N%03d", maxId + 1)
    }

    fun nextVideoId(): String {
        var maxId = 0
        for (v in cachedVideos) {
            val num = extractNumericSuffix(v.videoId, "V")
            if (num != null && num > maxId) {
                maxId = num
            }
        }
        return String.format(Locale.US, "V%03d", maxId + 1)
    }

    private fun extractNumericSuffix(raw: String, prefix: String): Int? {
        val trimmed = raw.trim()
        if (trimmed.startsWith(prefix, ignoreCase = true)) {
            val digits = trimmed.substring(prefix.length).trim()
            val parsed = digits.toIntOrNull()
            if (parsed != null) return parsed
        }
        val match = Regex("\\d+").find(trimmed)
        return match?.value?.toIntOrNull()
    }

    suspend fun appendQuestion(item: QuestionItem) = withContext(Dispatchers.IO) {
        cachedQuestions = cachedQuestions + item
        appendToFile("questions/questions.csv", CsvHelper.formatQuestionLine(item), CsvHelper.QUESTIONS_HEADER)
        reloadData()
    }

    suspend fun appendNote(
        item: NoteItem,
        content: String? = null,
        sourceFileUri: Uri? = null
    ) = withContext(Dispatchers.IO) {
        val filePath = if (item.filePath.isNotBlank()) item.filePath else "notes/files/${item.noteId}.txt"
        val normalizedNote = item.copy(filePath = filePath)
        writeOrCopyFile(filePath, content = content ?: "", sourceUri = sourceFileUri)
        cachedNotes = cachedNotes + normalizedNote
        appendToFile("notes/notes.csv", CsvHelper.formatNoteLine(normalizedNote), CsvHelper.NOTES_HEADER)
        reloadData()
    }

    suspend fun appendVideo(
        item: VideoItem,
        sourceVideoUri: Uri? = null
    ) = withContext(Dispatchers.IO) {
        if (sourceVideoUri != null && item.videoPath.isNotBlank() &&
            !item.videoPath.startsWith("http://") && !item.videoPath.startsWith("https://")
        ) {
            writeOrCopyFile(item.videoPath, sourceUri = sourceVideoUri)
        }
        cachedVideos = cachedVideos + item
        appendToFile("videos/videos.csv", CsvHelper.formatVideoLine(item), CsvHelper.VIDEOS_HEADER)
        reloadData()
    }

    suspend fun appendNoteRow(item: NoteItem) = withContext(Dispatchers.IO) {
        cachedNotes = cachedNotes + item
        appendToFile("notes/notes.csv", CsvHelper.formatNoteLine(item), CsvHelper.NOTES_HEADER)
        reloadData()
    }

    suspend fun appendVideoRow(item: VideoItem) = withContext(Dispatchers.IO) {
        cachedVideos = cachedVideos + item
        appendToFile("videos/videos.csv", CsvHelper.formatVideoLine(item), CsvHelper.VIDEOS_HEADER)
        reloadData()
    }

    fun writeNoteFileContent(relPath: String, content: String) {
        writeOrCopyFile(relPath, content = content)
    }

    /**
     * Queries original file name from a content or file URI.
     */
    fun queryFileName(context: Context, uri: Uri): String {
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(
                    uri,
                    arrayOf(OpenableColumns.DISPLAY_NAME),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx != -1) {
                            val name = cursor.getString(idx)
                            if (!name.isNullOrBlank()) return name
                        }
                    }
                }
            } catch (_: Exception) {}
        }
        val seg = uri.lastPathSegment ?: uri.toString()
        val name = seg.substringAfterLast("/").substringAfterLast(":")
        return name.ifBlank { "file" }
    }

    /**
     * Resolves unique relative destination path inside [destFolder] (e.g. "notes/files", "videos/files", "questions")
     * without overwriting existing files by appending a numerical suffix (_1, _2...).
     */
    fun resolveUniqueDestPath(destFolder: String, originalFileName: String): String {
        val cleanFolder = destFolder.trim('/')
        val treeUri = externalTreeUri
        val nameWithoutExt = originalFileName.substringBeforeLast(".")
        val ext = if (originalFileName.contains(".")) ".${originalFileName.substringAfterLast(".")}" else ""

        if (treeUri != null) {
            var current = DocumentFile.fromTreeUri(context, treeUri)
            val folderParts = cleanFolder.split("/").filter { it.isNotBlank() }
            for (part in folderParts) {
                current = current?.findFile(part)
            }
            if (current == null || current.findFile(originalFileName) == null) {
                return "$cleanFolder/$originalFileName"
            }
            var counter = 1
            while (true) {
                val candidate = "${nameWithoutExt}_$counter$ext"
                if (current.findFile(candidate) == null) {
                    return "$cleanFolder/$candidate"
                }
                counter++
            }
        } else {
            val folder = File(localDataDir, cleanFolder)
            if (!File(folder, originalFileName).exists()) {
                return "$cleanFolder/$originalFileName"
            }
            var counter = 1
            while (true) {
                val candidate = "${nameWithoutExt}_$counter$ext"
                if (!File(folder, candidate).exists()) {
                    return "$cleanFolder/$candidate"
                }
                counter++
            }
        }
    }

    /**
     * Copies content from [sourceUri] to [destRelPath] in the linked Data folder.
     */
    fun copySourceUriToDest(sourceUri: Uri, destRelPath: String): Boolean {
        val cleanRel = destRelPath.trimStart('/')
        val mime = when {
            cleanRel.endsWith(".txt") || cleanRel.endsWith(".md") -> "text/plain"
            cleanRel.endsWith(".html") || cleanRel.endsWith(".htm") -> "text/html"
            cleanRel.endsWith(".pdf") -> "application/pdf"
            cleanRel.endsWith(".mp4") -> "video/mp4"
            cleanRel.endsWith(".csv") -> "text/comma-separated-values"
            else -> "application/octet-stream"
        }

        val treeUri = externalTreeUri
        if (treeUri != null) {
            val docFile = getOrCreateDocumentFile(treeUri, cleanRel, headerIfNew = "", mimeType = mime)
                ?: return false
            try {
                context.contentResolver.openOutputStream(docFile.uri, "wt")?.use { os ->
                    context.contentResolver.openInputStream(sourceUri)?.use { input ->
                        input.copyTo(os)
                    }
                } ?: return false
                return true
            } catch (_: Exception) {
                return false
            }
        } else {
            val f = File(localDataDir, cleanRel)
            f.parentFile?.mkdirs()
            try {
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    f.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: return false
                return true
            } catch (_: Exception) {
                return false
            }
        }
    }

    /**
     * Deletes original source document after moving it into the Data folder.
     */
    fun deleteOriginalSourceFile(context: Context, uri: Uri): Boolean {
        var deleted = false
        try {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                deleted = DocumentsContract.deleteDocument(context.contentResolver, uri)
            }
        } catch (_: Exception) {}

        if (!deleted) {
            try {
                val rows = context.contentResolver.delete(uri, null, null)
                if (rows > 0) deleted = true
            } catch (_: Exception) {}
        }

        if (!deleted) {
            try {
                val path = uri.path
                if (path != null) {
                    val f = File(path)
                    if (f.exists()) {
                        deleted = f.delete()
                    }
                }
            } catch (_: Exception) {}
        }
        return deleted
    }

    /**
     * Moves a picked file (note, video, or questions CSV) from anywhere on the phone
     * into the linked Data folder. Deletes the original so only one file remains in Data.
     */
    suspend fun movePickedFileIntoDataFolder(
        sourceUri: Uri,
        destFolder: String
    ): String = withContext(Dispatchers.IO) {
        if (!isFolderLinked()) {
            throw IllegalStateException("No Data folder is linked. Please link a Data folder first.")
        }

        val originalName = queryFileName(context, sourceUri)
        val destRelPath = resolveUniqueDestPath(destFolder, originalName)

        val copied = copySourceUriToDest(sourceUri, destRelPath)
        if (!copied) {
            throw IllegalStateException("Failed to copy file into linked Data folder: $destRelPath")
        }

        deleteOriginalSourceFile(context, sourceUri)
        destRelPath
    }

    /**
     * Moves a questions CSV into Data/questions/, merges its rows into questions/questions.csv
     * by question_id (retaining existing questions not in incoming file), and adds links if
     * note ID or video ID is selected.
     */
    suspend fun importAndMergeQuestionsCsv(
        sourceUri: Uri,
        linkedNoteId: String? = null,
        linkedVideoId: String? = null,
        overrideFilter: DrillDownFilter? = null
    ): Int = withContext(Dispatchers.IO) {
        if (!isFolderLinked()) {
            throw IllegalStateException("No Data folder is linked. Please link a Data folder first.")
        }

        val csvText = context.contentResolver.openInputStream(sourceUri)?.bufferedReader()?.use { it.readText() }
            ?: throw IllegalArgumentException("Cannot read source CSV file")

        val headerLine = csvText.lines().firstOrNull { it.isNotBlank() } ?: ""
        if (!CsvHelper.validateQuestionsHeader(headerLine)) {
            throw IllegalArgumentException("Invalid questions.csv header: must include exam, question_id, question_text, correct_answer")
        }

        val incomingQuestions = CsvHelper.parseQuestions(csvText)
        if (incomingQuestions.isEmpty()) {
            throw IllegalArgumentException("No valid questions found in CSV")
        }

        // 1. Move file into Data/questions/
        val originalName = queryFileName(context, sourceUri)
        val destRelPath = resolveUniqueDestPath("questions", originalName)
        val copied = copySourceUriToDest(sourceUri, destRelPath)
        if (!copied) {
            throw IllegalStateException("Failed to move CSV into Data/questions/ folder")
        }
        deleteOriginalSourceFile(context, sourceUri)

        // 2. Apply override filter for any currently selected tree levels
        val adjustedQuestions = incomingQuestions.map { q ->
            val exam = overrideFilter?.exam?.takeIf { it.isNotBlank() } ?: q.exam
            val subject = overrideFilter?.subject?.takeIf { it.isNotBlank() } ?: q.subject
            val chapter = overrideFilter?.chapter?.takeIf { it.isNotBlank() } ?: q.chapter
            val topic = overrideFilter?.topic?.takeIf { it.isNotBlank() } ?: q.topic
            q.copy(exam = exam, subject = subject, chapter = chapter, topic = topic)
        }

        // 3. Keep existing colliding question_id remap
        val (remapped, _) = remapIncomingQuestionIds(adjustedQuestions)
        appendQuestionsBulk(remapped)

        // 4. Add link rows for imported questions if a note ID or video ID is selected
        val cleanNoteId = linkedNoteId?.trim()
        if (!cleanNoteId.isNullOrBlank()) {
            for (q in remapped) {
                val qId = q.questionId.trim()
                val exists = cachedNoteQuestions.any {
                    it.noteId.equals(cleanNoteId, ignoreCase = true) &&
                    it.questionId.equals(qId, ignoreCase = true)
                }
                if (!exists) {
                    appendToFile(
                        "links/note_questions.csv",
                        CsvHelper.formatNoteQuestionLine(cleanNoteId, qId),
                        CsvHelper.NOTE_QUESTIONS_HEADER
                    )
                }
            }
        }

        val cleanVideoId = linkedVideoId?.trim()
        if (!cleanVideoId.isNullOrBlank()) {
            for (q in remapped) {
                val qId = q.questionId.trim()
                val exists = cachedVideoQuestions.any {
                    it.videoId.equals(cleanVideoId, ignoreCase = true) &&
                    it.questionId.equals(qId, ignoreCase = true)
                }
                if (!exists) {
                    appendToFile(
                        "links/video_questions.csv",
                        CsvHelper.formatVideoQuestionLine(cleanVideoId, qId),
                        CsvHelper.VIDEO_QUESTIONS_HEADER
                    )
                }
            }
        }

        reloadData()
        remapped.size
    }

    /**
     * Deletes a single relative file from Data folder if it exists.
     */
    fun deleteRelativeFileIfExists(relPath: String): Boolean {
        val cleanRel = relPath.trimStart('/')
        if (cleanRel.isBlank()) return false
        val treeUri = externalTreeUri
        if (treeUri != null) {
            val docFile = findDocumentFile(treeUri, cleanRel)
            if (docFile != null && docFile.exists()) {
                try {
                    return docFile.delete()
                } catch (_: Exception) {}
            }
        }
        val f = File(localDataDir, cleanRel)
        if (f.exists()) {
            try {
                return f.delete()
            } catch (_: Exception) {}
        }
        return false
    }

    /**
     * Deletes a single note by ID:
     * - Removes note_id row from notes/notes.csv
     * - Deletes file at file_path if it exists
     * - Removes link rows for note_id in links/note_questions.csv
     * - Does NOT touch logs/
     */
    suspend fun deleteNote(noteId: String, filePath: String? = null) = withContext(Dispatchers.IO) {
        val cleanId = noteId.trim()
        if (cleanId.isBlank()) return@withContext

        // 1. Delete file if present
        val actualPath = filePath?.ifBlank { null } ?: cachedNotes.firstOrNull { it.noteId.equals(cleanId, ignoreCase = true) }?.filePath
        if (!actualPath.isNullOrBlank()) {
            deleteRelativeFileIfExists(actualPath)
        }

        // 2. Remove row from notes.csv
        cachedNotes = cachedNotes.filterNot { it.noteId.equals(cleanId, ignoreCase = true) }
        val builder = StringBuilder()
        builder.append(CsvHelper.NOTES_HEADER).append("\n")
        for (n in cachedNotes) {
            builder.append(CsvHelper.formatNoteLine(n))
        }
        overwriteFile("notes/notes.csv", builder.toString())

        // 3. Remove link rows from links/note_questions.csv
        cachedNoteQuestions = cachedNoteQuestions.filterNot { it.noteId.equals(cleanId, ignoreCase = true) }
        val linksBuilder = StringBuilder()
        linksBuilder.append(CsvHelper.NOTE_QUESTIONS_HEADER).append("\n")
        for (l in cachedNoteQuestions) {
            linksBuilder.append(CsvHelper.formatNoteQuestionLine(l.noteId, l.questionId))
        }
        overwriteFile("links/note_questions.csv", linksBuilder.toString())

        reloadData()
    }

    /**
     * Deletes a single reel video by ID:
     * - Removes video_id row from videos/videos.csv
     * - Deletes file at video_path if it exists
     * - Removes link rows for video_id in links/video_questions.csv
     * - Does NOT touch logs/
     */
    suspend fun deleteVideo(videoId: String, videoPath: String? = null) = withContext(Dispatchers.IO) {
        val cleanId = videoId.trim()
        if (cleanId.isBlank()) return@withContext

        // 1. Delete file if present
        val actualPath = videoPath?.ifBlank { null } ?: cachedVideos.firstOrNull { it.videoId.equals(cleanId, ignoreCase = true) }?.videoPath
        if (!actualPath.isNullOrBlank() && !actualPath.startsWith("http://") && !actualPath.startsWith("https://")) {
            deleteRelativeFileIfExists(actualPath)
        }

        // 2. Remove row from videos.csv
        cachedVideos = cachedVideos.filterNot { it.videoId.equals(cleanId, ignoreCase = true) }
        val builder = StringBuilder()
        builder.append(CsvHelper.VIDEOS_HEADER).append("\n")
        for (v in cachedVideos) {
            builder.append(CsvHelper.formatVideoLine(v))
        }
        overwriteFile("videos/videos.csv", builder.toString())

        // 3. Remove link rows from links/video_questions.csv
        cachedVideoQuestions = cachedVideoQuestions.filterNot { it.videoId.equals(cleanId, ignoreCase = true) }
        val linksBuilder = StringBuilder()
        linksBuilder.append(CsvHelper.VIDEO_QUESTIONS_HEADER).append("\n")
        for (l in cachedVideoQuestions) {
            linksBuilder.append(CsvHelper.formatVideoQuestionLine(l.videoId, l.questionId))
        }
        overwriteFile("links/video_questions.csv", linksBuilder.toString())

        reloadData()
    }

    /**
     * Deletes a single question by ID:
     * - Removes question_id row from questions/questions.csv
     * - Removes link rows that point at question_id
     * - Does NOT touch logs/
     */
    suspend fun deleteQuestion(questionId: String) = withContext(Dispatchers.IO) {
        val cleanId = questionId.trim()
        if (cleanId.isBlank()) return@withContext

        // 1. Remove from questions.csv
        cachedQuestions = cachedQuestions.filterNot { it.questionId.equals(cleanId, ignoreCase = true) }
        val builder = StringBuilder()
        builder.append(CsvHelper.QUESTIONS_HEADER).append("\n")
        for (q in cachedQuestions) {
            builder.append(CsvHelper.formatQuestionLine(q))
        }
        overwriteFile("questions/questions.csv", builder.toString())

        // 2. Remove from links/note_questions.csv
        cachedNoteQuestions = cachedNoteQuestions.filterNot { it.questionId.equals(cleanId, ignoreCase = true) }
        val nqBuilder = StringBuilder()
        nqBuilder.append(CsvHelper.NOTE_QUESTIONS_HEADER).append("\n")
        for (l in cachedNoteQuestions) {
            nqBuilder.append(CsvHelper.formatNoteQuestionLine(l.noteId, l.questionId))
        }
        overwriteFile("links/note_questions.csv", nqBuilder.toString())

        // 3. Remove from links/video_questions.csv
        cachedVideoQuestions = cachedVideoQuestions.filterNot { it.questionId.equals(cleanId, ignoreCase = true) }
        val vqBuilder = StringBuilder()
        vqBuilder.append(CsvHelper.VIDEO_QUESTIONS_HEADER).append("\n")
        for (l in cachedVideoQuestions) {
            vqBuilder.append(CsvHelper.formatVideoQuestionLine(l.videoId, l.questionId))
        }
        overwriteFile("links/video_questions.csv", vqBuilder.toString())

        reloadData()
    }

    fun overwriteFile(relPath: String, content: String) {
        val cleanRel = relPath.trimStart('/')
        val uri = externalTreeUri
        if (uri != null) {
            val docFile = getOrCreateDocumentFile(uri, cleanRel, headerIfNew = "")
            if (docFile != null) {
                try {
                    context.contentResolver.openOutputStream(docFile.uri, "wt")?.use { stream ->
                        stream.write(content.toByteArray())
                    }
                    return
                } catch (_: Exception) {}
            }
        }
        val f = File(localDataDir, cleanRel)
        f.parentFile?.mkdirs()
        f.writeText(content)
    }

    suspend fun appendNoteLink(noteId: String, questionId: String) = withContext(Dispatchers.IO) {
        val cleanNoteId = noteId.trim()
        val cleanQuestionId = questionId.trim()
        if (cleanNoteId.isBlank() || cleanQuestionId.isBlank()) return@withContext

        val exists = cachedNoteQuestions.any {
            it.noteId.equals(cleanNoteId, ignoreCase = true) &&
            it.questionId.equals(cleanQuestionId, ignoreCase = true)
        }
        if (!exists) {
            val link = NoteQuestionLink(cleanNoteId, cleanQuestionId)
            cachedNoteQuestions = cachedNoteQuestions + link
            appendToFile(
                "links/note_questions.csv",
                CsvHelper.formatNoteQuestionLine(cleanNoteId, cleanQuestionId),
                CsvHelper.NOTE_QUESTIONS_HEADER
            )
            reloadData()
        }
    }

    suspend fun appendVideoLink(videoId: String, questionId: String) = withContext(Dispatchers.IO) {
        val cleanVideoId = videoId.trim()
        val cleanQuestionId = questionId.trim()
        if (cleanVideoId.isBlank() || cleanQuestionId.isBlank()) return@withContext

        val exists = cachedVideoQuestions.any {
            it.videoId.equals(cleanVideoId, ignoreCase = true) &&
            it.questionId.equals(cleanQuestionId, ignoreCase = true)
        }
        if (!exists) {
            val link = VideoQuestionLink(cleanVideoId, cleanQuestionId)
            cachedVideoQuestions = cachedVideoQuestions + link
            appendToFile(
                "links/video_questions.csv",
                CsvHelper.formatVideoQuestionLine(cleanVideoId, cleanQuestionId),
                CsvHelper.VIDEO_QUESTIONS_HEADER
            )
            reloadData()
        }
    }

    fun remapIncomingQuestionIds(rows: List<QuestionItem>): Pair<List<QuestionItem>, Map<String, String>> {
        val usedIds = cachedQuestions.map { it.questionId.trim().lowercase() }.toMutableSet()
        var currentMax = 0
        for (id in usedIds) {
            val num = extractNumericSuffix(id, "q")
            if (num != null && num > currentMax) {
                currentMax = num
            }
        }

        val remappedList = mutableListOf<QuestionItem>()
        val idMap = mutableMapOf<String, String>()

        for (row in rows) {
            val rawOldId = row.questionId.trim()
            val isCollision = rawOldId.isBlank() || usedIds.contains(rawOldId.lowercase())

            val finalId = if (isCollision) {
                do {
                    currentMax++
                    val candidate = String.format(Locale.US, "Q%03d", currentMax)
                } while (usedIds.contains(candidate.lowercase()))
                val newId = String.format(Locale.US, "Q%03d", currentMax)
                usedIds.add(newId.lowercase())
                idMap[rawOldId] = newId
                newId
            } else {
                usedIds.add(rawOldId.lowercase())
                idMap[rawOldId] = rawOldId
                val num = extractNumericSuffix(rawOldId, "q")
                if (num != null && num > currentMax) {
                    currentMax = num
                }
                rawOldId
            }

            remappedList.add(row.copy(questionId = finalId))
        }

        return Pair(remappedList, idMap)
    }

    fun remapIncomingNoteIds(rows: List<NoteItem>): Pair<List<NoteItem>, Map<String, String>> {
        val usedIds = cachedNotes.map { it.noteId.trim().lowercase() }.toMutableSet()
        var currentMax = 0
        for (id in usedIds) {
            val num = extractNumericSuffix(id, "n")
            if (num != null && num > currentMax) {
                currentMax = num
            }
        }

        val remappedList = mutableListOf<NoteItem>()
        val idMap = mutableMapOf<String, String>()

        for (row in rows) {
            val rawOldId = row.noteId.trim()
            val isCollision = rawOldId.isBlank() || usedIds.contains(rawOldId.lowercase())

            val finalId = if (isCollision) {
                do {
                    currentMax++
                    val candidate = String.format(Locale.US, "N%03d", currentMax)
                } while (usedIds.contains(candidate.lowercase()))
                val newId = String.format(Locale.US, "N%03d", currentMax)
                usedIds.add(newId.lowercase())
                idMap[rawOldId] = newId
                newId
            } else {
                usedIds.add(rawOldId.lowercase())
                idMap[rawOldId] = rawOldId
                val num = extractNumericSuffix(rawOldId, "n")
                if (num != null && num > currentMax) {
                    currentMax = num
                }
                rawOldId
            }

            remappedList.add(row.copy(noteId = finalId))
        }

        return Pair(remappedList, idMap)
    }

    suspend fun appendNotesBulk(notes: List<NoteItem>) = withContext(Dispatchers.IO) {
        val builder = StringBuilder()
        for (note in notes) {
            val filePath = if (note.filePath.isNotBlank()) note.filePath else "notes/files/${note.noteId}.txt"
            val normalizedNote = note.copy(filePath = filePath)
            writeOrCopyFile(filePath, content = "# ${note.title}\n\nImported note content.")
            builder.append(CsvHelper.formatNoteLine(normalizedNote))
        }
        cachedNotes = cachedNotes + notes
        appendToFile("notes/notes.csv", builder.toString(), CsvHelper.NOTES_HEADER)
        reloadData()
    }

    suspend fun appendQuestionsBulk(questions: List<QuestionItem>) = withContext(Dispatchers.IO) {
        val builder = StringBuilder()
        for (q in questions) {
            builder.append(CsvHelper.formatQuestionLine(q))
        }
        cachedQuestions = cachedQuestions + questions
        appendToFile("questions/questions.csv", builder.toString(), CsvHelper.QUESTIONS_HEADER)
        reloadData()
    }

    suspend fun importQuestionsFromCsv(csvText: String): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val headerLine = csvText.lines().firstOrNull { it.isNotBlank() } ?: ""
        if (!CsvHelper.validateQuestionsHeader(headerLine)) {
            throw IllegalArgumentException("Invalid questions.csv header: must include exam, question_id, question_text, correct_answer")
        }

        val parsed = CsvHelper.parseQuestions(csvText)
        if (parsed.isEmpty()) {
            throw IllegalArgumentException("No valid questions found in CSV")
        }

        val (remapped, idMap) = remapIncomingQuestionIds(parsed)
        appendQuestionsBulk(remapped)
        val remappedCount = idMap.count { it.key != it.value }
        Pair(remapped.size, remappedCount)
    }

    suspend fun importQuestionsFromZip(
        zipUri: Uri,
        overrideFilter: DrillDownFilter? = null
    ): Pair<Int, Int> = withContext(Dispatchers.IO) {
        var csvContent: String? = null
        val imagesToExtract = mutableListOf<Pair<String, ByteArray>>()

        context.contentResolver.openInputStream(zipUri)?.use { input ->
            java.util.zip.ZipInputStream(input).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val name = entry.name.replace('\\', '/')
                    if (!entry.isDirectory) {
                        val fileName = name.substringAfterLast("/")
                        if (fileName.equals("questions.csv", ignoreCase = true)) {
                            csvContent = zis.bufferedReader().readText()
                        } else if (name.contains("questions/images/", ignoreCase = true) || name.contains("images/", ignoreCase = true)) {
                            val relImgPath = "questions/images/$fileName"
                            val bytes = zis.readBytes()
                            imagesToExtract.add(Pair(relImgPath, bytes))
                        }
                    }
                    entry = zis.nextEntry
                }
            }
        }

        if (csvContent == null) {
            throw IllegalArgumentException("No questions.csv found inside ZIP archive")
        }

        val headerLine = csvContent.lines().firstOrNull { it.isNotBlank() } ?: ""
        if (!CsvHelper.validateQuestionsHeader(headerLine)) {
            throw IllegalArgumentException("Invalid questions.csv header inside ZIP")
        }

        val parsed = CsvHelper.parseQuestions(csvContent)
        if (parsed.isEmpty()) {
            throw IllegalArgumentException("No valid questions found in ZIP's questions.csv")
        }

        // Extract any accompanying images
        for ((relPath, bytes) in imagesToExtract) {
            writeOrCopyFile(relPath, byteContent = bytes)
        }

        // Apply override filter for any currently selected tree levels
        val adjustedQuestions = parsed.map { q ->
            val exam = overrideFilter?.exam?.takeIf { it.isNotBlank() } ?: q.exam
            val subject = overrideFilter?.subject?.takeIf { it.isNotBlank() } ?: q.subject
            val chapter = overrideFilter?.chapter?.takeIf { it.isNotBlank() } ?: q.chapter
            val topic = overrideFilter?.topic?.takeIf { it.isNotBlank() } ?: q.topic
            q.copy(exam = exam, subject = subject, chapter = chapter, topic = topic)
        }

        val (remapped, idMap) = remapIncomingQuestionIds(adjustedQuestions)
        appendQuestionsBulk(remapped)
        val remappedCount = idMap.count { it.key != it.value }
        Pair(remapped.size, remappedCount)
    }

    // --- Dashboard Stats Computation (Gap 1, Gap 9) ---
    fun dashboardStats(): DashboardStats {
        val totalQuestions = cachedQuestions.size
        val totalAttempts = cachedAttempts.size

        if (cachedAttempts.isEmpty()) {
            return DashboardStats(totalQuestions = totalQuestions, totalAttempts = 0)
        }

        val todayDate = LocalDate.now()
        val todayStr = todayDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

        // Today attempts
        val todayAttempts = cachedAttempts.filter { a ->
            if (a.timestamp > 0) {
                try {
                    val aDate = LocalDate.ofInstant(Instant.ofEpochMilli(a.timestamp), ZoneId.systemDefault())
                    aDate.format(DateTimeFormatter.ISO_LOCAL_DATE) == todayStr
                } catch (_: Exception) { false }
            } else false
        }

        val attemptedToday = todayAttempts.size
        val todayCorrect = todayAttempts.count { it.isCorrect == 1 }
        val todayAccuracy = if (attemptedToday > 0) (todayCorrect.toFloat() / attemptedToday) * 100f else null

        val totalTime = cachedAttempts.sumOf { it.timeSpentSec }
        val overallAvgTime = if (cachedAttempts.isNotEmpty()) totalTime / cachedAttempts.size else null

        // Streak computation based on distinct local dates
        val attemptDates = cachedAttempts.mapNotNull { a ->
            if (a.timestamp > 0) {
                try {
                    LocalDate.ofInstant(Instant.ofEpochMilli(a.timestamp), ZoneId.systemDefault())
                } catch (_: Exception) { null }
            } else null
        }.distinct().sortedDescending()

        var streak = 0
        var checkDate = todayDate
        if (attemptDates.contains(checkDate)) {
            streak++
            while (true) {
                checkDate = checkDate.minusDays(1)
                if (attemptDates.contains(checkDate)) {
                    streak++
                } else break
            }
        } else if (attemptDates.contains(todayDate.minusDays(1))) {
            // Did attempts yesterday, streak can count from yesterday
            checkDate = todayDate.minusDays(1)
            streak++
            while (true) {
                checkDate = checkDate.minusDays(1)
                if (attemptDates.contains(checkDate)) {
                    streak++
                } else break
            }
        }

        // Daily attempts list (grouped by ISO date string, e.g. 2026-09-27, 28, 29)
        val dailyGroups = cachedAttempts.groupBy { a ->
            try {
                LocalDate.ofInstant(Instant.ofEpochMilli(a.timestamp), ZoneId.systemDefault())
                    .format(DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (_: Exception) { "Unknown" }
        }.filterKeys { it != "Unknown" }

        val dailyList = dailyGroups.map { (dateStr, atts) ->
            val count = atts.size
            val correct = atts.count { it.isCorrect == 1 }
            val acc = if (count > 0) (correct.toFloat() / count) * 100f else 0f
            DailyAttemptStat(date = dateStr, count = count, correctCount = correct, accuracyPercent = acc)
        }.sortedBy { it.date }.takeLast(7)

        // Chapter stats (group by full path: exam • subject • chapter)
        val chapterGroups = cachedAttempts.groupBy { "${it.exam}|${it.subject}|${it.chapter}" }
        val chapterStatItems = mutableListOf<ChapterStatItem>()

        for ((key, atts) in chapterGroups) {
            val parts = key.split("|")
            val ex = parts.getOrElse(0) { "" }
            val sub = parts.getOrElse(1) { "" }
            val chap = parts.getOrElse(2) { "" }

            val count = atts.size
            val correct = atts.count { it.isCorrect == 1 }
            val acc = if (count > 0) (correct.toFloat() / count) * 100f else 0f
            val avgT = if (count > 0) atts.sumOf { it.timeSpentSec } / count else 0

            chapterStatItems.add(
                ChapterStatItem(
                    exam = ex,
                    subject = sub,
                    chapter = chap,
                    totalAttempts = count,
                    accuracyPercent = acc,
                    avgTimeSec = avgT
                )
            )
        }

        // Weak vs Strong chapters (with full "Exam • Subject • Chapter" label)
        val sortedByAcc = chapterStatItems.sortedBy { it.accuracyPercent }
        val weakChapters = sortedByAcc.filter { it.accuracyPercent < 60f }
            .map { it.fullPathLabel to it.accuracyPercent }.take(3)
        val strongChapters = sortedByAcc.filter { it.accuracyPercent >= 60f }.reversed()
            .map { it.fullPathLabel to it.accuracyPercent }.take(3)

        return DashboardStats(
            totalQuestions = totalQuestions,
            totalAttempts = totalAttempts,
            attemptedToday = attemptedToday,
            streakDays = streak,
            overallAvgTimeSec = overallAvgTime,
            todayAccuracyPercent = todayAccuracy,
            weakChapters = weakChapters,
            strongChapters = strongChapters,
            dailyAttempts = dailyList,
            chapterStats = chapterStatItems
        )
    }

    suspend fun readNoteContent(filePath: String): String = withContext(Dispatchers.IO) {
        val relClean = filePath.trimStart('/')
        val fileContent = readTextFile(relClean)
        if (fileContent != null && fileContent.isNotBlank()) {
            fileContent
        } else {
            "Note content at '$filePath' not found or file missing in Data/ folder."
        }
    }

    // --- Low-level SAF / File I/O Helpers ---
    private fun readCsvContent(relPath: String, defaultHeader: String): String {
        val text = readTextFile(relPath)
        return if (text != null && text.isNotBlank()) {
            text
        } else {
            // Write default header
            appendToFile(relPath, "", defaultHeader)
            defaultHeader + "\n"
        }
    }

    private fun readTextFile(relPath: String): String? {
        val uri = externalTreeUri
        if (uri != null) {
            val docFile = findDocumentFile(uri, relPath)
            if (docFile != null && docFile.exists() && docFile.canRead()) {
                return try {
                    context.contentResolver.openInputStream(docFile.uri)?.use { stream ->
                        stream.bufferedReader().use { it.readText() }
                    }
                } catch (e: Exception) {
                    null
                }
            }
        }
        // Fallback to local Data dir
        val f = File(localDataDir, relPath)
        return if (f.exists() && f.canRead()) f.readText() else null
    }

    private fun writeOrCopyFile(
        relPath: String,
        content: String? = null,
        sourceUri: Uri? = null,
        byteContent: ByteArray? = null
    ) {
        val cleanRel = relPath.trimStart('/')
        val mime = when {
            cleanRel.endsWith(".txt") || cleanRel.endsWith(".md") -> "text/plain"
            cleanRel.endsWith(".html") || cleanRel.endsWith(".htm") -> "text/html"
            cleanRel.endsWith(".pdf") -> "application/pdf"
            cleanRel.endsWith(".mp4") -> "video/mp4"
            cleanRel.endsWith(".png") -> "image/png"
            cleanRel.endsWith(".jpg") || cleanRel.endsWith(".jpeg") -> "image/jpeg"
            else -> "application/octet-stream"
        }

        val uri = externalTreeUri
        if (uri != null) {
            val docFile = getOrCreateDocumentFile(uri, cleanRel, headerIfNew = "", mimeType = mime)
            if (docFile != null) {
                try {
                    context.contentResolver.openOutputStream(docFile.uri)?.use { os ->
                        if (sourceUri != null) {
                            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                                input.copyTo(os)
                            }
                        } else if (byteContent != null) {
                            os.write(byteContent)
                        } else if (content != null) {
                            os.write(content.toByteArray())
                        }
                    }
                    return
                } catch (_: Exception) {}
            }
        }

        // Local fallback
        val f = File(localDataDir, cleanRel)
        f.parentFile?.mkdirs()
        if (sourceUri != null) {
            try {
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    f.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (_: Exception) {}
        } else if (byteContent != null) {
            f.writeBytes(byteContent)
        } else if (content != null) {
            f.writeText(content)
        } else if (!f.exists()) {
            f.createNewFile()
        }
    }

    private fun appendToFile(relPath: String, lineToAppend: String, headerIfNew: String) {
        val uri = externalTreeUri
        if (uri != null) {
            val docFile = getOrCreateDocumentFile(uri, relPath, headerIfNew)
            if (docFile != null) {
                try {
                    context.contentResolver.openOutputStream(docFile.uri, "wa")?.use { stream ->
                        stream.write(lineToAppend.toByteArray())
                    }
                    return
                } catch (_: Exception) {}
            }
        }

        // Local fallback
        val f = File(localDataDir, relPath)
        f.parentFile?.mkdirs()
        if (!f.exists()) {
            f.writeText(headerIfNew + "\n")
        }
        if (lineToAppend.isNotEmpty()) {
            f.appendText(lineToAppend)
        }
    }

    private fun findDocumentFile(treeUri: Uri, relPath: String): DocumentFile? {
        var current = DocumentFile.fromTreeUri(context, treeUri) ?: return null
        val parts = relPath.split("/").filter { it.isNotBlank() }
        for (part in parts) {
            current = current.findFile(part) ?: return null
        }
        return current
    }

    private fun getOrCreateDocumentFile(
        treeUri: Uri,
        relPath: String,
        headerIfNew: String = "",
        mimeType: String = "text/comma-separated-values"
    ): DocumentFile? {
        var current = DocumentFile.fromTreeUri(context, treeUri) ?: return null
        val parts = relPath.split("/").filter { it.isNotBlank() }
        if (parts.isEmpty()) return null

        for (i in 0 until parts.size - 1) {
            val folderName = parts[i]
            val sub = current.findFile(folderName) ?: current.createDirectory(folderName)
            if (sub == null) return null
            current = sub
        }

        val fileName = parts.last()
        var targetFile = current.findFile(fileName)
        if (targetFile == null) {
            targetFile = current.createFile(mimeType, fileName)
            if (targetFile != null && headerIfNew.isNotEmpty()) {
                context.contentResolver.openOutputStream(targetFile.uri)?.use { stream ->
                    stream.write((headerIfNew + "\n").toByteArray())
                }
            }
        }
        return targetFile
    }
}
