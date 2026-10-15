package com.example.data.scheduler

import com.example.data.model.AttemptLog
import com.example.data.model.ReviewStateItem

/**
 * Pure Kotlin spaced-repetition review scheduler with zero Android dependencies.
 *
 * Consecutive correct streak drives intervals:
 * - 1 consecutive correct -> 1 day
 * - 2 consecutive correct -> 3 days
 * - 3 consecutive correct -> 7 days
 * - >=4 consecutive correct -> 14 days
 * - Wrong answer -> streak resets to 0, due in 10 minutes
 * - Skip -> preserves streak, marks skipped, due in 10 minutes
 */
object ReviewScheduler {

    const val DUE_SOON_MILLIS = 10L * 60 * 1000L // 10 minutes
    const val ONE_DAY_MILLIS = 24L * 60 * 60 * 1000L

    fun getNextIntervalMillis(consecutiveCorrect: Int): Long {
        return when (consecutiveCorrect) {
            1 -> 1L * ONE_DAY_MILLIS
            2 -> 3L * ONE_DAY_MILLIS
            3 -> 7L * ONE_DAY_MILLIS
            else -> if (consecutiveCorrect >= 4) 14L * ONE_DAY_MILLIS else DUE_SOON_MILLIS
        }
    }

    fun onAttempt(
        existing: ReviewStateItem?,
        questionId: String,
        isCorrect: Boolean,
        now: Long,
        isoTimestamp: String
    ): ReviewStateItem {
        val qId = questionId.trim()
        val timesAttempted = (existing?.timesAttempted ?: 0) + 1
        val timesCorrect = (existing?.timesCorrect ?: 0) + (if (isCorrect) 1 else 0)
        val timesWrong = (existing?.timesWrong ?: 0) + (if (!isCorrect) 1 else 0)
        val timesSkipped = existing?.timesSkipped ?: 0
        val lastResult = if (isCorrect) "correct" else "wrong"

        val nextStreak = if (isCorrect) {
            (existing?.consecutiveCorrect ?: 0) + 1
        } else {
            0
        }

        val nextDue = if (isCorrect) {
            now + getNextIntervalMillis(nextStreak)
        } else {
            now + DUE_SOON_MILLIS
        }

        return ReviewStateItem(
            questionId = qId,
            timesAttempted = timesAttempted,
            timesCorrect = timesCorrect,
            timesWrong = timesWrong,
            timesSkipped = timesSkipped,
            lastResult = lastResult,
            lastAttemptAt = now,
            rawLastAttemptAt = isoTimestamp,
            nextDueAt = nextDue,
            rawNextDueAt = "",
            consecutiveCorrect = nextStreak
        )
    }

    fun onSkip(
        existing: ReviewStateItem?,
        questionId: String,
        now: Long,
        isoTimestamp: String
    ): ReviewStateItem {
        val qId = questionId.trim()
        val timesAttempted = existing?.timesAttempted ?: 0
        val timesCorrect = existing?.timesCorrect ?: 0
        val timesWrong = existing?.timesWrong ?: 0
        val timesSkipped = (existing?.timesSkipped ?: 0) + 1
        val lastResult = "skipped"
        val streak = existing?.consecutiveCorrect ?: 0
        val nextDue = now + DUE_SOON_MILLIS

        return ReviewStateItem(
            questionId = qId,
            timesAttempted = timesAttempted,
            timesCorrect = timesCorrect,
            timesWrong = timesWrong,
            timesSkipped = timesSkipped,
            lastResult = lastResult,
            lastAttemptAt = now,
            rawLastAttemptAt = isoTimestamp,
            nextDueAt = nextDue,
            rawNextDueAt = "",
            consecutiveCorrect = streak
        )
    }

    fun reconcileFromAttempts(
        attempts: List<AttemptLog>,
        existingSkippedCount: Int = 0
    ): ReviewStateItem? {
        if (attempts.isEmpty()) return null
        val sorted = attempts.sortedBy { it.timestamp }
        val qId = sorted.first().questionId.trim()
        var currentStreak = 0
        var timesCorrect = 0
        var timesWrong = 0

        for (attempt in sorted) {
            if (attempt.isCorrect == 1) {
                timesCorrect++
                currentStreak++
            } else {
                timesWrong++
                currentStreak = 0
            }
        }

        val latest = sorted.last()
        val lastResult = if (latest.isCorrect == 1) "correct" else "wrong"
        val nextDue = if (lastResult == "correct") {
            latest.timestamp + getNextIntervalMillis(currentStreak)
        } else {
            latest.timestamp + DUE_SOON_MILLIS
        }

        return ReviewStateItem(
            questionId = qId,
            timesAttempted = sorted.size,
            timesCorrect = timesCorrect,
            timesWrong = timesWrong,
            timesSkipped = existingSkippedCount,
            lastResult = lastResult,
            lastAttemptAt = latest.timestamp,
            rawLastAttemptAt = latest.rawTimestamp,
            nextDueAt = nextDue,
            rawNextDueAt = "",
            consecutiveCorrect = currentStreak
        )
    }
}
