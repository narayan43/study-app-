package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
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

    private val localDataDir = File(context.filesDir, "Data")

    init {
        val uriStr = prefs.getString("data_folder_uri", null)
        if (uriStr != null) {
            externalTreeUri = Uri.parse(uriStr)
        }
        // First-run internal fallback
        if (externalTreeUri == null && !localDataDir.exists()) {
            localDataDir.mkdirs()
            DummyDataGenerator.generateDummyDataTree(localDataDir)
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

        return qTopics.distinct().filter { it.isNotBlank() }
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

    private fun getOrCreateDocumentFile(treeUri: Uri, relPath: String, headerIfNew: String): DocumentFile? {
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
            targetFile = current.createFile("text/comma-separated-values", fileName)
            if (targetFile != null) {
                context.contentResolver.openOutputStream(targetFile.uri)?.use { stream ->
                    stream.write((headerIfNew + "\n").toByteArray())
                }
            }
        }
        return targetFile
    }
}
