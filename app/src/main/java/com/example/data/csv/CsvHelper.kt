package com.example.data.csv

import com.example.data.model.AttemptEntity
import com.example.data.model.NoteEntity
import com.example.data.model.NoteQuestionCrossRef
import com.example.data.model.NoteUsageEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.VideoEntity
import com.example.data.model.VideoQuestionCrossRef
import com.example.data.model.VideoUsageEntity
import com.example.data.model.WeeklyReviewEntity

object CsvHelper {

    // --- CSV Templates matching the study repository Data/ folder ---
    const val QUESTIONS_TEMPLATE = "id,subject,topic,questionHindi,questionEnglish,optionA,optionB,optionC,optionD,correctOption,explanation,difficulty,isPYQ,pyqYear"
    const val NOTES_TEMPLATE = "id,title,subject,topic,contentMarkdown,readTimeMin,tags,isBookmarked,createdAt"
    const val VIDEOS_TEMPLATE = "id,title,subject,topic,durationSec,instructor,description,tags,videoUrlOrPath"
    const val NOTE_QUESTIONS_TEMPLATE = "note_id,question_id"
    const val VIDEO_QUESTIONS_TEMPLATE = "video_id,question_id"
    const val ATTEMPTS_TEMPLATE = "question_id,chosen_option,is_correct,time_taken_seconds,attempted_at,mode"
    const val NOTES_USAGE_TEMPLATE = "note_id,time_spent_sec,is_completed,timestamp"
    const val VIDEO_USAGE_TEMPLATE = "video_id,watch_time_sec,last_position_sec,is_completed,timestamp"
    const val WEEKLY_REVIEWS_TEMPLATE = "week_start,written_on,learned,will_change,proud_of"

    fun exportQuestionsToCsv(questions: List<QuestionEntity>): String {
        val sb = StringBuilder()
        sb.append(QUESTIONS_TEMPLATE).append("\n")
        for (q in questions) {
            sb.append(escape(q.id)).append(",")
            sb.append(escape(q.subject)).append(",")
            sb.append(escape(q.topic)).append(",")
            sb.append(escape(q.questionHindi)).append(",")
            sb.append(escape(q.questionEnglish)).append(",")
            sb.append(escape(q.optionA)).append(",")
            sb.append(escape(q.optionB)).append(",")
            sb.append(escape(q.optionC)).append(",")
            sb.append(escape(q.optionD)).append(",")
            sb.append(escape(q.correctOption)).append(",")
            sb.append(escape(q.explanation)).append(",")
            sb.append(escape(q.difficulty)).append(",")
            sb.append(q.isPYQ).append(",")
            sb.append(escape(q.pyqYear)).append("\n")
        }
        return sb.toString()
    }

    fun exportAttemptsToCsv(attempts: List<AttemptEntity>): String {
        val sb = StringBuilder()
        sb.append(ATTEMPTS_TEMPLATE).append("\n")
        for (a in attempts) {
            sb.append(escape(a.questionId)).append(",")
            sb.append(escape(a.chosenOption)).append(",")
            sb.append(a.isCorrect).append(",")
            sb.append(a.timeTakenSeconds).append(",")
            sb.append(a.attemptedAt).append(",")
            sb.append(escape(a.mode)).append("\n")
        }
        return sb.toString()
    }

    fun exportWeeklyReviewsToCsv(reviews: List<WeeklyReviewEntity>): String {
        val sb = StringBuilder()
        sb.append(WEEKLY_REVIEWS_TEMPLATE).append("\n")
        for (r in reviews) {
            sb.append(escape(r.weekStart)).append(",")
            sb.append(escape(r.writtenOn)).append(",")
            sb.append(escape(r.learned)).append(",")
            sb.append(escape(r.willChange)).append(",")
            sb.append(escape(r.proudOf)).append("\n")
        }
        return sb.toString()
    }

    fun exportNotesToCsv(notes: List<NoteEntity>): String {
        val sb = StringBuilder()
        sb.append(NOTES_TEMPLATE).append("\n")
        for (n in notes) {
            sb.append(escape(n.id)).append(",")
            sb.append(escape(n.title)).append(",")
            sb.append(escape(n.subject)).append(",")
            sb.append(escape(n.topic)).append(",")
            sb.append(escape(n.contentMarkdown)).append(",")
            sb.append(n.readTimeMin).append(",")
            sb.append(escape(n.tags)).append(",")
            sb.append(n.isBookmarked).append(",")
            sb.append(escape(n.createdAt)).append("\n")
        }
        return sb.toString()
    }

    fun exportVideosToCsv(videos: List<VideoEntity>): String {
        val sb = StringBuilder()
        sb.append(VIDEOS_TEMPLATE).append("\n")
        for (v in videos) {
            sb.append(escape(v.id)).append(",")
            sb.append(escape(v.title)).append(",")
            sb.append(escape(v.subject)).append(",")
            sb.append(escape(v.topic)).append(",")
            sb.append(v.durationSec).append(",")
            sb.append(escape(v.instructor)).append(",")
            sb.append(escape(v.description)).append(",")
            sb.append(escape(v.tags)).append(",")
            sb.append(escape(v.videoUrlOrPath)).append("\n")
        }
        return sb.toString()
    }

    fun exportNoteQuestionsToCsv(links: List<NoteQuestionCrossRef>): String {
        val sb = StringBuilder()
        sb.append(NOTE_QUESTIONS_TEMPLATE).append("\n")
        for (l in links) {
            sb.append(escape(l.noteId)).append(",")
            sb.append(escape(l.questionId)).append("\n")
        }
        return sb.toString()
    }

    fun exportVideoQuestionsToCsv(links: List<VideoQuestionCrossRef>): String {
        val sb = StringBuilder()
        sb.append(VIDEO_QUESTIONS_TEMPLATE).append("\n")
        for (l in links) {
            sb.append(escape(l.videoId)).append(",")
            sb.append(escape(l.questionId)).append("\n")
        }
        return sb.toString()
    }

    fun parseNotesFromCsv(csvText: String): List<NoteEntity> {
        val lines = csvText.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()
        val result = mutableListOf<NoteEntity>()
        for (i in 1 until lines.size) {
            val cols = parseCsvLine(lines[i])
            if (cols.size >= 5) {
                result.add(
                    NoteEntity(
                        id = cols.getOrElse(0) { "note_${System.currentTimeMillis()}_$i" },
                        title = cols.getOrElse(1) { "Study Note $i" },
                        subject = cols.getOrElse(2) { "Law & Constitution" },
                        topic = cols.getOrElse(3) { "General" },
                        contentMarkdown = cols.getOrElse(4) { "" },
                        readTimeMin = cols.getOrElse(5) { "10" }.toIntOrNull() ?: 10,
                        tags = cols.getOrElse(6) { "" },
                        isBookmarked = cols.getOrElse(7) { "false" }.toBoolean(),
                        createdAt = cols.getOrElse(8) { "2026-09-28" }
                    )
                )
            }
        }
        return result
    }

    fun parseVideosFromCsv(csvText: String): List<VideoEntity> {
        val lines = csvText.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()
        val result = mutableListOf<VideoEntity>()
        for (i in 1 until lines.size) {
            val cols = parseCsvLine(lines[i])
            if (cols.size >= 4) {
                result.add(
                    VideoEntity(
                        id = cols.getOrElse(0) { "vid_${System.currentTimeMillis()}_$i" },
                        title = cols.getOrElse(1) { "Video Lecture $i" },
                        subject = cols.getOrElse(2) { "Law & Constitution" },
                        topic = cols.getOrElse(3) { "General" },
                        durationSec = cols.getOrElse(4) { "1200" }.toIntOrNull() ?: 1200,
                        instructor = cols.getOrElse(5) { "Police Academy Mentor" },
                        description = cols.getOrElse(6) { "" },
                        tags = cols.getOrElse(7) { "" },
                        videoUrlOrPath = cols.getOrElse(8) { "" }
                    )
                )
            }
        }
        return result
    }

    fun parseNoteQuestionsFromCsv(csvText: String): List<NoteQuestionCrossRef> {
        val lines = csvText.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()
        val result = mutableListOf<NoteQuestionCrossRef>()
        for (i in 1 until lines.size) {
            val cols = parseCsvLine(lines[i])
            if (cols.size >= 2) {
                result.add(NoteQuestionCrossRef(noteId = cols[0].trim(), questionId = cols[1].trim()))
            }
        }
        return result
    }

    fun parseVideoQuestionsFromCsv(csvText: String): List<VideoQuestionCrossRef> {
        val lines = csvText.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()
        val result = mutableListOf<VideoQuestionCrossRef>()
        for (i in 1 until lines.size) {
            val cols = parseCsvLine(lines[i])
            if (cols.size >= 2) {
                result.add(VideoQuestionCrossRef(videoId = cols[0].trim(), questionId = cols[1].trim()))
            }
        }
        return result
    }

    fun parseQuestionsFromCsv(csvText: String): List<QuestionEntity> {
        val lines = csvText.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()
        val result = mutableListOf<QuestionEntity>()

        for (i in 1 until lines.size) {
            val cols = parseCsvLine(lines[i])
            if (cols.size >= 11) {
                result.add(
                    QuestionEntity(
                        id = cols.getOrElse(0) { "imp_q_$i" },
                        subject = cols.getOrElse(1) { "Law & Constitution" },
                        topic = cols.getOrElse(2) { "Imported" },
                        questionHindi = cols.getOrElse(3) { "" },
                        questionEnglish = cols.getOrElse(4) { cols.getOrElse(3) { "" } },
                        optionA = cols.getOrElse(5) { "" },
                        optionB = cols.getOrElse(6) { "" },
                        optionC = cols.getOrElse(7) { "" },
                        optionD = cols.getOrElse(8) { "" },
                        correctOption = cols.getOrElse(9) { "A" }.trim().uppercase(),
                        explanation = cols.getOrElse(10) { "" },
                        difficulty = cols.getOrElse(11) { "Medium" },
                        isPYQ = cols.getOrElse(12) { "false" }.toBoolean(),
                        pyqYear = cols.getOrElse(13) { "" }
                    )
                )
            }
        }
        return result
    }

    fun parseWeeklyReviewsFromCsv(csvText: String): List<WeeklyReviewEntity> {
        val lines = csvText.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()
        val result = mutableListOf<WeeklyReviewEntity>()

        for (i in 1 until lines.size) {
            val cols = parseCsvLine(lines[i])
            if (cols.size >= 5) {
                result.add(
                    WeeklyReviewEntity(
                        weekStart = cols.getOrElse(0) { "" },
                        writtenOn = cols.getOrElse(1) { "" },
                        learned = cols.getOrElse(2) { "" },
                        willChange = cols.getOrElse(3) { "" },
                        proudOf = cols.getOrElse(4) { "" }
                    )
                )
            }
        }
        return result
    }

    private fun escape(data: String): String {
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
        val sb = java.lang.StringBuilder()
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
