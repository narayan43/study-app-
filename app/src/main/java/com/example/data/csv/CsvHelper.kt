package com.example.data.csv

import com.example.data.model.AttemptLog
import com.example.data.model.NoteItem
import com.example.data.model.NoteQuestionLink
import com.example.data.model.NoteUsageLog
import com.example.data.model.QuestionItem
import com.example.data.model.VideoItem
import com.example.data.model.VideoQuestionLink
import com.example.data.model.VideoUsageLog
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object CsvHelper {

    // EXACT CSV Headers from specification
    const val QUESTIONS_HEADER = "exam,question_id,subject,chapter,topic,question_text,option_a,option_b,option_c,option_d,question_image,option_a_image,option_b_image,option_c_image,option_d_image,correct_answer"
    const val NOTES_HEADER = "note_id,exam,subject,chapter,topic,title,file_path,note_type"
    const val VIDEOS_HEADER = "video_id,exam,subject,chapter,topic,title,video_path,duration_sec"
    const val NOTE_QUESTIONS_HEADER = "note_id,question_id"
    const val VIDEO_QUESTIONS_HEADER = "video_id,question_id"
    const val ATTEMPTS_HEADER = "attempt_id,question_id,exam,subject,chapter,chosen_answer,is_correct,time_spent_sec,timestamp"
    const val NOTES_USAGE_HEADER = "event_id,note_id,exam,subject,chapter,opened_at,closed_at,time_spent_sec"
    const val VIDEO_USAGE_HEADER = "event_id,video_id,exam,subject,chapter,opened_at,closed_at,time_spent_sec,started_test"

    // Parse ISO-8601 first, fallback to epoch millis
    fun parseTimestamp(str: String): Long {
        if (str.isBlank()) return 0L
        val trimmed = str.trim()
        // 1. Try ISO-8601 with offset
        try {
            return OffsetDateTime.parse(trimmed).toInstant().toEpochMilli()
        } catch (_: Exception) {}

        // 2. Try Instant (e.g. Z)
        try {
            return Instant.parse(trimmed).toEpochMilli()
        } catch (_: Exception) {}

        // 3. Try LocalDateTime (without offset)
        try {
            val ldt = LocalDateTime.parse(trimmed)
            return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (_: Exception) {}

        // 4. Fallback to epoch millis if all digits
        return trimmed.toLongOrNull() ?: 0L
    }

    // Format as ISO-8601 with system zone offset
    fun formatIsoTimestamp(epochMilli: Long = System.currentTimeMillis()): String {
        return try {
            val instant = Instant.ofEpochMilli(epochMilli)
            OffsetDateTime.ofInstant(instant, ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        } catch (_: Exception) {
            OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        }
    }

    fun validateQuestionsHeader(headerLine: String): Boolean {
        val lower = headerLine.lowercase()
        return lower.contains("exam") && lower.contains("question_id") && lower.contains("question_text") && lower.contains("correct_answer")
    }

    fun parseQuestions(csvContent: String): List<QuestionItem> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()
        val result = mutableListOf<QuestionItem>()
        for (i in 1 until lines.size) {
            val cols = parseCsvLine(lines[i])
            if (cols.size >= 16) {
                result.add(
                    QuestionItem(
                        exam = cols[0],
                        questionId = cols[1],
                        subject = cols[2],
                        chapter = cols[3],
                        topic = cols[4],
                        questionText = cols[5],
                        optionA = cols[6],
                        optionB = cols[7],
                        optionC = cols[8],
                        optionD = cols[9],
                        questionImage = cols[10],
                        optionAImage = cols[11],
                        optionBImage = cols[12],
                        optionCImage = cols[13],
                        optionDImage = cols[14],
                        correctAnswer = cols[15].trim().uppercase()
                    )
                )
            }
        }
        return result
    }

    fun parseNotes(csvContent: String): List<NoteItem> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()
        val result = mutableListOf<NoteItem>()
        for (i in 1 until lines.size) {
            val cols = parseCsvLine(lines[i])
            if (cols.size >= 8) {
                result.add(
                    NoteItem(
                        noteId = cols[0],
                        exam = cols[1],
                        subject = cols[2],
                        chapter = cols[3],
                        topic = cols[4],
                        title = cols[5],
                        filePath = cols[6],
                        noteType = cols[7]
                    )
                )
            }
        }
        return result
    }

    fun parseVideos(csvContent: String): List<VideoItem> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()
        val result = mutableListOf<VideoItem>()
        for (i in 1 until lines.size) {
            val cols = parseCsvLine(lines[i])
            if (cols.size >= 8) {
                result.add(
                    VideoItem(
                        videoId = cols[0],
                        exam = cols[1],
                        subject = cols[2],
                        chapter = cols[3],
                        topic = cols[4],
                        title = cols[5],
                        videoPath = cols[6],
                        durationSec = cols[7].toIntOrNull() ?: 0
                    )
                )
            }
        }
        return result
    }

    fun parseNoteQuestions(csvContent: String): List<NoteQuestionLink> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()
        val result = mutableListOf<NoteQuestionLink>()
        for (i in 1 until lines.size) {
            val cols = parseCsvLine(lines[i])
            if (cols.size >= 2) {
                result.add(NoteQuestionLink(noteId = cols[0].trim(), questionId = cols[1].trim()))
            }
        }
        return result
    }

    fun parseVideoQuestions(csvContent: String): List<VideoQuestionLink> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()
        val result = mutableListOf<VideoQuestionLink>()
        for (i in 1 until lines.size) {
            val cols = parseCsvLine(lines[i])
            if (cols.size >= 2) {
                result.add(VideoQuestionLink(videoId = cols[0].trim(), questionId = cols[1].trim()))
            }
        }
        return result
    }

    fun parseAttempts(csvContent: String): List<AttemptLog> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()
        val result = mutableListOf<AttemptLog>()
        for (i in 1 until lines.size) {
            val cols = parseCsvLine(lines[i])
            if (cols.size >= 9) {
                val rawTime = cols[8].trim()
                result.add(
                    AttemptLog(
                        attemptId = cols[0],
                        questionId = cols[1],
                        exam = cols[2],
                        subject = cols[3],
                        chapter = cols[4],
                        chosenAnswer = cols[5],
                        isCorrect = cols[6].toIntOrNull() ?: 0,
                        timeSpentSec = cols[7].toIntOrNull() ?: 0,
                        timestamp = parseTimestamp(rawTime),
                        rawTimestamp = rawTime
                    )
                )
            }
        }
        return result
    }

    fun parseNotesUsage(csvContent: String): List<NoteUsageLog> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()
        val result = mutableListOf<NoteUsageLog>()
        for (i in 1 until lines.size) {
            val cols = parseCsvLine(lines[i])
            if (cols.size >= 8) {
                val rawOpened = cols[5].trim()
                val rawClosed = cols[6].trim()
                result.add(
                    NoteUsageLog(
                        eventId = cols[0],
                        noteId = cols[1],
                        exam = cols[2],
                        subject = cols[3],
                        chapter = cols[4],
                        openedAt = parseTimestamp(rawOpened),
                        closedAt = parseTimestamp(rawClosed),
                        timeSpentSec = cols[7].toIntOrNull() ?: 0,
                        rawOpenedAt = rawOpened,
                        rawClosedAt = rawClosed
                    )
                )
            }
        }
        return result
    }

    fun parseVideoUsage(csvContent: String): List<VideoUsageLog> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()
        val result = mutableListOf<VideoUsageLog>()
        for (i in 1 until lines.size) {
            val cols = parseCsvLine(lines[i])
            if (cols.size >= 9) {
                val rawOpened = cols[5].trim()
                val rawClosed = cols[6].trim()
                result.add(
                    VideoUsageLog(
                        eventId = cols[0],
                        videoId = cols[1],
                        exam = cols[2],
                        subject = cols[3],
                        chapter = cols[4],
                        openedAt = parseTimestamp(rawOpened),
                        closedAt = parseTimestamp(rawClosed),
                        timeSpentSec = cols[7].toIntOrNull() ?: 0,
                        startedTest = cols[8].toIntOrNull() ?: 0,
                        rawOpenedAt = rawOpened,
                        rawClosedAt = rawClosed
                    )
                )
            }
        }
        return result
    }

    fun formatAttemptLine(attempt: AttemptLog): String {
        val isoTime = if (attempt.rawTimestamp.isNotBlank()) attempt.rawTimestamp else formatIsoTimestamp(attempt.timestamp)
        return "${escape(attempt.attemptId)},${escape(attempt.questionId)},${escape(attempt.exam)},${escape(attempt.subject)},${escape(attempt.chapter)},${escape(attempt.chosenAnswer)},${attempt.isCorrect},${attempt.timeSpentSec},$isoTime\n"
    }

    fun formatNoteUsageLine(usage: NoteUsageLog): String {
        val openIso = if (usage.rawOpenedAt.isNotBlank()) usage.rawOpenedAt else formatIsoTimestamp(usage.openedAt)
        val closeIso = if (usage.rawClosedAt.isNotBlank()) usage.rawClosedAt else formatIsoTimestamp(usage.closedAt)
        return "${escape(usage.eventId)},${escape(usage.noteId)},${escape(usage.exam)},${escape(usage.subject)},${escape(usage.chapter)},$openIso,$closeIso,${usage.timeSpentSec}\n"
    }

    fun formatVideoUsageLine(usage: VideoUsageLog): String {
        val openIso = if (usage.rawOpenedAt.isNotBlank()) usage.rawOpenedAt else formatIsoTimestamp(usage.openedAt)
        val closeIso = if (usage.rawClosedAt.isNotBlank()) usage.rawClosedAt else formatIsoTimestamp(usage.closedAt)
        return "${escape(usage.eventId)},${escape(usage.videoId)},${escape(usage.exam)},${escape(usage.subject)},${escape(usage.chapter)},$openIso,$closeIso,${usage.timeSpentSec},${usage.startedTest}\n"
    }

    fun formatQuestionLine(question: QuestionItem): String {
        return listOf(
            escape(question.exam),
            escape(question.questionId),
            escape(question.subject),
            escape(question.chapter),
            escape(question.topic),
            escape(question.questionText),
            escape(question.optionA),
            escape(question.optionB),
            escape(question.optionC),
            escape(question.optionD),
            escape(question.questionImage),
            escape(question.optionAImage),
            escape(question.optionBImage),
            escape(question.optionCImage),
            escape(question.optionDImage),
            escape(question.correctAnswer)
        ).joinToString(",") + "\n"
    }

    fun formatNoteLine(note: NoteItem): String {
        return listOf(
            escape(note.noteId),
            escape(note.exam),
            escape(note.subject),
            escape(note.chapter),
            escape(note.topic),
            escape(note.title),
            escape(note.filePath),
            escape(note.noteType)
        ).joinToString(",") + "\n"
    }

    fun formatVideoLine(video: VideoItem): String {
        return listOf(
            escape(video.videoId),
            escape(video.exam),
            escape(video.subject),
            escape(video.chapter),
            escape(video.topic),
            escape(video.title),
            escape(video.videoPath),
            video.durationSec.toString()
        ).joinToString(",") + "\n"
    }

    fun formatNoteQuestionLine(noteId: String, questionId: String): String {
        return "${escape(noteId)},${escape(questionId)}\n"
    }

    fun formatVideoQuestionLine(videoId: String, questionId: String): String {
        return "${escape(videoId)},${escape(questionId)}\n"
    }

    fun escape(data: String): String {
        var str = data.replace("\r", " ").replace("\n", " ")
        if (str.contains(",") || str.contains("\"")) {
            str = str.replace("\"", "\"\"")
            return "\"$str\""
        }
        return str
    }

    fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        var inQuotes = false
        val sb = StringBuilder()
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    sb.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim())
                sb.setLength(0)
            } else {
                sb.append(c)
            }
            i++
        }
        tokens.add(sb.toString().trim())
        return tokens
    }
}
