package com.example

import com.example.data.csv.CsvHelper
import com.example.data.model.AttemptEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.WeeklyReviewEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testWeeklyReviewsCsvExportAndParse() {
        val originalReview = WeeklyReviewEntity(
            weekStart = "2026-09-21",
            writtenOn = "2026-09-26",
            learned = "Mastered IPC Section 302 and 304B",
            willChange = "Practice 20 math questions daily",
            proudOf = "85% in mock test"
        )
        val csv = CsvHelper.exportWeeklyReviewsToCsv(listOf(originalReview))
        assertTrue(csv.contains("week_start,written_on,learned,will_change,proud_of"))
        assertTrue(csv.contains("2026-09-21"))

        val parsed = CsvHelper.parseWeeklyReviewsFromCsv(csv)
        assertEquals(1, parsed.size)
        assertEquals("2026-09-21", parsed[0].weekStart)
        assertEquals("Mastered IPC Section 302 and 304B", parsed[0].learned)
    }

    @Test
    fun testQuestionsCsvExportAndParse() {
        val question = QuestionEntity(
            id = "Q_TEST_1",
            subject = "General Hindi",
            topic = "Alankar",
            questionHindi = "कनक कनक ते सौगुनी",
            questionEnglish = "Kanak kanak",
            optionA = "यमक",
            optionB = "श्लेष",
            optionC = "रूपक",
            optionD = "उपमा",
            correctOption = "A",
            explanation = "यमक अलंकार"
        )
        val csv = CsvHelper.exportQuestionsToCsv(listOf(question))
        assertTrue(csv.contains("id,subject,topic"))
        assertTrue(csv.contains("Q_TEST_1"))

        val parsed = CsvHelper.parseQuestionsFromCsv(csv)
        assertEquals(1, parsed.size)
        assertEquals("Q_TEST_1", parsed[0].id)
        assertEquals("A", parsed[0].correctOption)
    }

    @Test
    fun testAttemptsCsvExport() {
        val attempt = AttemptEntity(
            questionId = "Q_TEST_1",
            chosenOption = "A",
            isCorrect = true,
            timeTakenSeconds = 25,
            attemptedAt = 1700000000000L,
            mode = "PRACTICE"
        )
        val csv = CsvHelper.exportAttemptsToCsv(listOf(attempt))
        assertTrue(csv.contains("question_id,chosen_option,is_correct"))
        assertTrue(csv.contains("Q_TEST_1,A,true,25"))
    }
}
