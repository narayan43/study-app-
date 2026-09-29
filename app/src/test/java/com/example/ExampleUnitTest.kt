package com.example

import com.example.data.csv.CsvHelper
import com.example.data.model.AttemptLog
import com.example.data.model.NoteItem
import com.example.data.model.QuestionItem
import com.example.data.model.VideoItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testQuestionsCsvParse() {
        val csv = """
            ${CsvHelper.QUESTIONS_HEADER}
            UPSI,q1,Law,IPC,Body,What is Section 300?,Murder,Theft,Assault,Extortion,,,,,,A
        """.trimIndent()

        val parsed = CsvHelper.parseQuestions(csv)
        assertEquals(1, parsed.size)
        val q = parsed[0]
        assertEquals("UPSI", q.exam)
        assertEquals("q1", q.questionId)
        assertEquals("Law", q.subject)
        assertEquals("IPC", q.chapter)
        assertEquals("Body", q.topic)
        assertEquals("What is Section 300?", q.questionText)
        assertEquals("Murder", q.optionA)
        assertEquals("A", q.correctAnswer)
    }

    @Test
    fun testNotesCsvParse() {
        val csv = """
            ${CsvHelper.NOTES_HEADER}
            n1,UPSI,Law,IPC,Body,IPC Summary,notes/files/ipc.txt,markdown
        """.trimIndent()

        val parsed = CsvHelper.parseNotes(csv)
        assertEquals(1, parsed.size)
        assertEquals("n1", parsed[0].noteId)
        assertEquals("IPC Summary", parsed[0].title)
        assertEquals("notes/files/ipc.txt", parsed[0].filePath)
    }

    @Test
    fun testVideosCsvParse() {
        val csv = """
            ${CsvHelper.VIDEOS_HEADER}
            v1,UPSI,Law,IPC,Body,IPC Video,videos/ipc.mp4,1200
        """.trimIndent()

        val parsed = CsvHelper.parseVideos(csv)
        assertEquals(1, parsed.size)
        assertEquals("v1", parsed[0].videoId)
        assertEquals(1200, parsed[0].durationSec)
    }

    @Test
    fun testAttemptsFormatAndParse() {
        val log = AttemptLog(
            attemptId = "att_1",
            questionId = "q1",
            exam = "UPSI",
            subject = "Law",
            chapter = "IPC",
            chosenAnswer = "A",
            isCorrect = 1,
            timeSpentSec = 15,
            timestamp = 1700000000000L
        )

        val line = CsvHelper.formatAttemptLine(log)
        val csv = "${CsvHelper.ATTEMPTS_HEADER}\n$line"
        val parsed = CsvHelper.parseAttempts(csv)

        assertEquals(1, parsed.size)
        assertEquals("att_1", parsed[0].attemptId)
        assertEquals("q1", parsed[0].questionId)
        assertEquals(1, parsed[0].isCorrect)
        assertEquals(15, parsed[0].timeSpentSec)
    }

    @Test
    fun testIso8601TimestampParse() {
        val isoStr = "2026-09-27T10:12:00+05:30"
        val parsedMillis = CsvHelper.parseTimestamp(isoStr)
        assertTrue(parsedMillis > 0L)

        val csv = """
            ${CsvHelper.ATTEMPTS_HEADER}
            att_iso,Q001,UPSI,Polity,Preamble,A,1,14,2026-09-27T10:12:00+05:30
        """.trimIndent()
        val parsed = CsvHelper.parseAttempts(csv)
        assertEquals(1, parsed.size)
        assertEquals("att_iso", parsed[0].attemptId)
        assertEquals(parsedMillis, parsed[0].timestamp)
        assertTrue(parsed[0].rawTimestamp.contains("2026-09-27"))
    }
}
