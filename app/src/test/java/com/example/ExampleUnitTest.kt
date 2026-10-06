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

    @Test
    fun testFormatQuestionLineAndParse() {
        val q = QuestionItem(
            exam = "UPSI",
            questionId = "Q081",
            subject = "Law",
            chapter = "IPC",
            topic = "Offences",
            questionText = "What is murder?",
            optionA = "Section 300",
            optionB = "Section 302",
            optionC = "Section 304",
            optionD = "Section 307",
            questionImage = "",
            optionAImage = "",
            optionBImage = "",
            optionCImage = "",
            optionDImage = "",
            correctAnswer = "A"
        )
        val line = CsvHelper.formatQuestionLine(q)
        val csv = "${CsvHelper.QUESTIONS_HEADER}\n$line"
        val parsed = CsvHelper.parseQuestions(csv)
        assertEquals(1, parsed.size)
        assertEquals("Q081", parsed[0].questionId)
        assertEquals("Section 300", parsed[0].optionA)
        assertEquals("A", parsed[0].correctAnswer)
    }

    @Test
    fun testFormatNoteAndVideoLines() {
        val note = NoteItem("N009", "UPSI", "Law", "IPC", "Offences", "IPC Murder", "notes/files/N009.txt", "markdown")
        val noteLine = CsvHelper.formatNoteLine(note)
        val noteCsv = "${CsvHelper.NOTES_HEADER}\n$noteLine"
        val parsedNotes = CsvHelper.parseNotes(noteCsv)
        assertEquals(1, parsedNotes.size)
        assertEquals("N009", parsedNotes[0].noteId)
        assertEquals("notes/files/N009.txt", parsedNotes[0].filePath)

        val video = VideoItem("V007", "UPSI", "Law", "IPC", "Offences", "IPC Lecture", "videos/files/V007.mp4", 300)
        val videoLine = CsvHelper.formatVideoLine(video)
        val videoCsv = "${CsvHelper.VIDEOS_HEADER}\n$videoLine"
        val parsedVideos = CsvHelper.parseVideos(videoCsv)
        assertEquals(1, parsedVideos.size)
        assertEquals("V007", parsedVideos[0].videoId)
        assertEquals(300, parsedVideos[0].durationSec)
    }

    @Test
    fun testFormatLinks() {
        val noteLinkLine = CsvHelper.formatNoteQuestionLine("N009", "Q081")
        val noteLinkCsv = "${CsvHelper.NOTE_QUESTIONS_HEADER}\n$noteLinkLine"
        val parsedNoteLinks = CsvHelper.parseNoteQuestions(noteLinkCsv)
        assertEquals(1, parsedNoteLinks.size)
        assertEquals("N009", parsedNoteLinks[0].noteId)
        assertEquals("Q081", parsedNoteLinks[0].questionId)

        val videoLinkLine = CsvHelper.formatVideoQuestionLine("V007", "Q081")
        val videoLinkCsv = "${CsvHelper.VIDEO_QUESTIONS_HEADER}\n$videoLinkLine"
        val parsedVideoLinks = CsvHelper.parseVideoQuestions(videoLinkCsv)
        assertEquals(1, parsedVideoLinks.size)
        assertEquals("V007", parsedVideoLinks[0].videoId)
        assertEquals("Q081", parsedVideoLinks[0].questionId)
    }

    @Test
    fun testReviewStateParseAndFormat() {
        val state = com.example.data.model.ReviewStateItem(
            questionId = "Q001",
            timesAttempted = 2,
            timesCorrect = 1,
            timesWrong = 1,
            timesSkipped = 0,
            lastResult = "correct",
            lastAttemptAt = 1760000000000L,
            rawLastAttemptAt = "2026-10-06T10:00:00+05:30",
            nextDueAt = 1760086400000L,
            rawNextDueAt = "2026-10-07T10:00:00+05:30"
        )
        val line = CsvHelper.formatReviewStateLine(state)
        val csv = "${CsvHelper.REVIEW_STATE_HEADER}\n$line"
        val parsedMap = CsvHelper.parseReviewState(csv)
        assertEquals(1, parsedMap.size)
        val parsed = parsedMap["Q001"]
        assertTrue(parsed != null)
        assertEquals("Q001", parsed!!.questionId)
        assertEquals(2, parsed.timesAttempted)
        assertEquals(1, parsed.timesCorrect)
        assertEquals(1, parsed.timesWrong)
        assertEquals(0, parsed.timesSkipped)
        assertEquals("correct", parsed.lastResult)
        assertEquals("2026-10-06T10:00:00+05:30", parsed.rawLastAttemptAt)
        assertEquals("2026-10-07T10:00:00+05:30", parsed.rawNextDueAt)
    }

    @Test
    fun testReviewStateSkip() {
        val state = com.example.data.model.ReviewStateItem(
            questionId = "Q002",
            timesAttempted = 0,
            timesCorrect = 0,
            timesWrong = 0,
            timesSkipped = 1,
            lastResult = "skipped",
            lastAttemptAt = 1760000000000L,
            rawLastAttemptAt = "2026-10-06T10:00:00+05:30",
            nextDueAt = 1760000600000L,
            rawNextDueAt = "2026-10-06T10:10:00+05:30"
        )
        val line = CsvHelper.formatReviewStateLine(state)
        val csv = "${CsvHelper.REVIEW_STATE_HEADER}\n$line"
        val parsedMap = CsvHelper.parseReviewState(csv)
        assertEquals(1, parsedMap.size)
        val parsed = parsedMap["Q002"]
        assertTrue(parsed != null)
        assertEquals(1, parsed!!.timesSkipped)
        assertEquals("skipped", parsed.lastResult)
    }
}
