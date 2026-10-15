package com.example

import com.example.data.csv.CsvHelper
import com.example.data.model.AttemptLog
import com.example.data.model.ReviewStateItem
import com.example.data.scheduler.ReviewScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewSchedulerTest {

    private val baseTime = 1700000000000L
    private val isoTime = "2026-10-14T10:00:00+05:30"

    @Test
    fun testFirstAttempt_correct_givesOneDayInterval() {
        val state = ReviewScheduler.onAttempt(
            existing = null,
            questionId = "Q1",
            isCorrect = true,
            now = baseTime,
            isoTimestamp = isoTime
        )

        assertEquals("Q1", state.questionId)
        assertEquals(1, state.timesAttempted)
        assertEquals(1, state.timesCorrect)
        assertEquals(0, state.timesWrong)
        assertEquals(0, state.timesSkipped)
        assertEquals("correct", state.lastResult)
        assertEquals(1, state.consecutiveCorrect)
        assertEquals(baseTime + 1L * 24 * 60 * 60 * 1000L, state.nextDueAt)
    }

    @Test
    fun testFirstAttempt_wrong_givesTenMinuteInterval() {
        val state = ReviewScheduler.onAttempt(
            existing = null,
            questionId = "Q2",
            isCorrect = false,
            now = baseTime,
            isoTimestamp = isoTime
        )

        assertEquals("Q2", state.questionId)
        assertEquals(1, state.timesAttempted)
        assertEquals(0, state.timesCorrect)
        assertEquals(1, state.timesWrong)
        assertEquals(0, state.timesSkipped)
        assertEquals("wrong", state.lastResult)
        assertEquals(0, state.consecutiveCorrect)
        assertEquals(baseTime + 10L * 60 * 1000L, state.nextDueAt)
    }

    @Test
    fun testRepeatedCorrect_drivesIntervalProgression() {
        // Attempt 1: streak 1 -> 1 day
        var state = ReviewScheduler.onAttempt(null, "Q3", true, baseTime, isoTime)
        assertEquals(1, state.consecutiveCorrect)
        assertEquals(baseTime + 1L * 24 * 60 * 60 * 1000L, state.nextDueAt)

        // Attempt 2: streak 2 -> 3 days
        val t2 = baseTime + 1L * 24 * 60 * 60 * 1000L
        state = ReviewScheduler.onAttempt(state, "Q3", true, t2, isoTime)
        assertEquals(2, state.consecutiveCorrect)
        assertEquals(2, state.timesAttempted)
        assertEquals(2, state.timesCorrect)
        assertEquals(t2 + 3L * 24 * 60 * 60 * 1000L, state.nextDueAt)

        // Attempt 3: streak 3 -> 7 days
        val t3 = t2 + 3L * 24 * 60 * 60 * 1000L
        state = ReviewScheduler.onAttempt(state, "Q3", true, t3, isoTime)
        assertEquals(3, state.consecutiveCorrect)
        assertEquals(3, state.timesAttempted)
        assertEquals(3, state.timesCorrect)
        assertEquals(t3 + 7L * 24 * 60 * 60 * 1000L, state.nextDueAt)

        // Attempt 4: streak 4 -> 14 days
        val t4 = t3 + 7L * 24 * 60 * 60 * 1000L
        state = ReviewScheduler.onAttempt(state, "Q3", true, t4, isoTime)
        assertEquals(4, state.consecutiveCorrect)
        assertEquals(4, state.timesAttempted)
        assertEquals(4, state.timesCorrect)
        assertEquals(t4 + 14L * 24 * 60 * 60 * 1000L, state.nextDueAt)
    }

    @Test
    fun testWrongAfterCorrect_resetsStreakToZero_andNextCorrectStartsAtOneDay() {
        // Build 3 consecutive correct answers
        var state = ReviewScheduler.onAttempt(null, "Q4", true, baseTime, isoTime)
        state = ReviewScheduler.onAttempt(state, "Q4", true, baseTime + 1000, isoTime)
        state = ReviewScheduler.onAttempt(state, "Q4", true, baseTime + 2000, isoTime)
        assertEquals(3, state.consecutiveCorrect)
        assertEquals(3, state.timesCorrect)

        // Wrong answer: streak MUST reset to 0, due in 10 minutes
        val wrongTime = baseTime + 3000
        state = ReviewScheduler.onAttempt(state, "Q4", false, wrongTime, isoTime)
        assertEquals(0, state.consecutiveCorrect)
        assertEquals(4, state.timesAttempted)
        assertEquals(3, state.timesCorrect)
        assertEquals(1, state.timesWrong)
        assertEquals("wrong", state.lastResult)
        assertEquals(wrongTime + 10L * 60 * 1000L, state.nextDueAt)

        // Next answer correct: streak becomes 1 (interval = 1 day, NOT 14 days!)
        val recoveryTime = wrongTime + 10L * 60 * 1000L
        state = ReviewScheduler.onAttempt(state, "Q4", true, recoveryTime, isoTime)
        assertEquals(1, state.consecutiveCorrect)
        assertEquals(5, state.timesAttempted)
        assertEquals(4, state.timesCorrect)
        assertEquals(1, state.timesWrong)
        assertEquals("correct", state.lastResult)
        assertEquals(recoveryTime + 1L * 24 * 60 * 60 * 1000L, state.nextDueAt)
    }

    @Test
    fun testSkip_preservesStreak_incrementsSkipped_dueInTenMinutes() {
        val initial = ReviewScheduler.onAttempt(null, "Q5", true, baseTime, isoTime)
        assertEquals(1, initial.consecutiveCorrect)

        val skipTime = baseTime + 5000
        val skipped = ReviewScheduler.onSkip(initial, "Q5", skipTime, isoTime)
        assertEquals(1, skipped.timesAttempted)
        assertEquals(1, skipped.timesCorrect)
        assertEquals(0, skipped.timesWrong)
        assertEquals(1, skipped.timesSkipped)
        assertEquals("skipped", skipped.lastResult)
        assertEquals(1, skipped.consecutiveCorrect) // preserved
        assertEquals(skipTime + 10L * 60 * 1000L, skipped.nextDueAt)
    }

    @Test
    fun testReconcileFromAttempts() {
        val attempts = listOf(
            AttemptLog("a1", "Q6", "UPSI", "Law", "IPC", "A", 1, 10, baseTime),
            AttemptLog("a2", "Q6", "UPSI", "Law", "IPC", "B", 1, 12, baseTime + 10000),
            AttemptLog("a3", "Q6", "UPSI", "Law", "IPC", "C", 0, 15, baseTime + 20000),
            AttemptLog("a4", "Q6", "UPSI", "Law", "IPC", "A", 1, 8, baseTime + 30000)
        )

        val reconciled = ReviewScheduler.reconcileFromAttempts(attempts)
        assertNotNull(reconciled)
        assertEquals(4, reconciled!!.timesAttempted)
        assertEquals(3, reconciled.timesCorrect)
        assertEquals(1, reconciled.timesWrong)
        assertEquals("correct", reconciled.lastResult)
        // Last was correct after wrong, so streak = 1 -> 1 day interval
        assertEquals(1, reconciled.consecutiveCorrect)
        assertEquals(baseTime + 30000 + 1L * 24 * 60 * 60 * 1000L, reconciled.nextDueAt)
    }

    @Test
    fun testLegacyReviewStateCsvCompatibility_loadsWithoutStreakColumn() {
        // Old CSV header with only 8 columns (no consecutive_correct)
        val legacyCsv = """
            question_id,times_attempted,times_correct,times_wrong,times_skipped,last_result,last_attempt_at,next_due_at
            Q_OLD,3,2,1,0,correct,2026-10-14T10:00:00+05:30,2026-10-15T10:00:00+05:30
        """.trimIndent()

        val parsed = CsvHelper.parseReviewState(legacyCsv)
        assertEquals(1, parsed.size)
        val item = parsed["Q_OLD"]
        assertNotNull(item)
        assertEquals("Q_OLD", item!!.questionId)
        assertEquals(3, item.timesAttempted)
        assertEquals(2, item.timesCorrect)
        assertEquals(1, item.timesWrong)
        assertEquals("correct", item.lastResult)
        assertEquals(1, item.consecutiveCorrect) // backward compatible default

        // Format back and verify it formats with 9 columns now
        val formatted = CsvHelper.formatReviewStateLine(item)
        val parts = formatted.trim().split(",")
        assertEquals(9, parts.size)
        assertEquals("1", parts[8])
    }
}
