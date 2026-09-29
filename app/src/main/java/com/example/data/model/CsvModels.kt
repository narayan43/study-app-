package com.example.data.model

data class QuestionItem(
    val exam: String,
    val questionId: String,
    val subject: String,
    val chapter: String,
    val topic: String,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val questionImage: String = "",
    val optionAImage: String = "",
    val optionBImage: String = "",
    val optionCImage: String = "",
    val optionDImage: String = "",
    val correctAnswer: String
)

data class NoteItem(
    val noteId: String,
    val exam: String,
    val subject: String,
    val chapter: String,
    val topic: String,
    val title: String,
    val filePath: String,
    val noteType: String
)

data class VideoItem(
    val videoId: String,
    val exam: String,
    val subject: String,
    val chapter: String,
    val topic: String,
    val title: String,
    val videoPath: String,
    val durationSec: Int
)

data class NoteQuestionLink(
    val noteId: String,
    val questionId: String
)

data class VideoQuestionLink(
    val videoId: String,
    val questionId: String
)

data class AttemptLog(
    val attemptId: String,
    val questionId: String,
    val exam: String,
    val subject: String,
    val chapter: String,
    val chosenAnswer: String,
    val isCorrect: Int,
    val timeSpentSec: Int,
    val timestamp: Long,
    val rawTimestamp: String = ""
)

data class NoteUsageLog(
    val eventId: String,
    val noteId: String,
    val exam: String,
    val subject: String,
    val chapter: String,
    val openedAt: Long,
    val closedAt: Long,
    val timeSpentSec: Int,
    val rawOpenedAt: String = "",
    val rawClosedAt: String = ""
)

data class VideoUsageLog(
    val eventId: String,
    val videoId: String,
    val exam: String,
    val subject: String,
    val chapter: String,
    val openedAt: Long,
    val closedAt: Long,
    val timeSpentSec: Int,
    val startedTest: Int,
    val rawOpenedAt: String = "",
    val rawClosedAt: String = ""
)

data class DrillDownFilter(
    val exam: String? = null,
    val subject: String? = null,
    val chapter: String? = null,
    val topic: String? = null
)

sealed class TestSliceSource {
    data class DrillDown(val filter: DrillDownFilter) : TestSliceSource()
    data class NoteRevision(val noteId: String, val noteTitle: String, val returnToNote: Boolean = true) : TestSliceSource()
    data class VideoRevision(val videoId: String, val videoTitle: String, val returnToReel: Boolean = true) : TestSliceSource()
    data class MistakesRetest(val exam: String, val subject: String, val chapter: String, val questionIds: List<String>) : TestSliceSource()
}

data class DailyAttemptStat(
    val date: String,
    val count: Int,
    val correctCount: Int,
    val accuracyPercent: Float
)

data class DashboardStats(
    val totalQuestions: Int = 0,
    val totalAttempts: Int = 0,
    val attemptedToday: Int = 0,
    val streakDays: Int = 0,
    val overallAvgTimeSec: Int? = null,
    val todayAccuracyPercent: Float? = null,
    val weakChapters: List<Pair<String, Float>> = emptyList(), // "Exam • Subject • Chapter" -> accuracy
    val strongChapters: List<Pair<String, Float>> = emptyList(),
    val dailyAttempts: List<DailyAttemptStat> = emptyList(),
    val chapterStats: List<ChapterStatItem> = emptyList()
)

data class ChapterStatItem(
    val exam: String,
    val subject: String,
    val chapter: String,
    val totalAttempts: Int,
    val accuracyPercent: Float,
    val avgTimeSec: Int
) {
    val fullPathLabel: String get() = "$exam • $subject • $chapter"
}
