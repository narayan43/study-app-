package com.example

import com.example.data.csv.CsvHelper
import com.example.data.model.QuestionItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvCorrectnessTest {

    @Test
    fun testParseCsvRows_withCommasAndQuotes() {
        val qMark = "\""
        val csv = CsvHelper.QUESTIONS_HEADER + "\n" +
            "UPSI,Q101,Law,IPC,Offences,\"What is \"\"theft\"\", defined under Sec 378?\",\"Option \"\"A\"\"\",\"Option, B\",Option C,Option D,,,,,,A\n"

        val questions = CsvHelper.parseQuestions(csv)
        assertEquals(1, questions.size)
        val q = questions[0]
        assertEquals("What is \"theft\", defined under Sec 378?", q.questionText)
        assertEquals("Option \"A\"", q.optionA)
        assertEquals("Option, B", q.optionB)
    }

    @Test
    fun testParseCsvRows_preservesNewlinesInsideQuotes() {
        val multiLineQuestion = "Section 300 IPC explains murder.\nException 1 relates to grave provocation.\nWhich applies?"
        val csv = CsvHelper.QUESTIONS_HEADER + "\n" +
            "UPSI,Q102,Law,IPC,Offences,\"$multiLineQuestion\",Opt A,Opt B,Opt C,Opt D,,,,,,B\n"

        val questions = CsvHelper.parseQuestions(csv)
        assertEquals(1, questions.size)
        assertEquals(multiLineQuestion, questions[0].questionText)
    }

    @Test
    fun testEscape_preservesNewlinesWithoutReplacingWithSpace() {
        val textWithNewlines = "Line 1\nLine 2\r\nLine 3"
        val escaped = CsvHelper.escape(textWithNewlines)
        assertTrue(escaped.startsWith("\"") && escaped.endsWith("\""))
        assertTrue(escaped.contains("\n"))

        val singleRowCsv = "field1,$escaped,field3"
        val rows = CsvHelper.parseCsvRows(singleRowCsv)
        assertEquals(1, rows.size)
        assertEquals("field1", rows[0][0])
        assertEquals(textWithNewlines.trim(), rows[0][1])
        assertEquals("field3", rows[0][2])
    }

    @Test
    fun testParseCsvRows_crlfLineEndings() {
        val csv = CsvHelper.QUESTIONS_HEADER + "\r\nUPSI,Q103,Law,IPC,Offences,CRLF test,A,B,C,D,,,,,,C\r\n"
        val questions = CsvHelper.parseQuestions(csv)
        assertEquals(1, questions.size)
        assertEquals("CRLF test", questions[0].questionText)
        assertEquals("C", questions[0].correctAnswer)
    }

    @Test
    fun testHindiAndUnicodeText() {
        val hindiQuestion = "भारतीय दंड संहिता (IPC) की धारा 300 किससे संबंधित है?"
        val hindiOptA = "हत्या (Murder)"
        val hindiOptB = "चोरी (Theft)"
        val hindiOptC = "लूट (Robbery)"
        val hindiOptD = "धोखाधड़ी (Cheating)"

        val q = QuestionItem(
            exam = "यूपी एसआई (UPSI)",
            questionId = "Q_HINDI",
            subject = "विधि (Law)",
            chapter = "आईपीसी (IPC)",
            topic = "मानव शरीर के विरुद्ध अपराध",
            questionText = hindiQuestion,
            optionA = hindiOptA,
            optionB = hindiOptB,
            optionC = hindiOptC,
            optionD = hindiOptD,
            questionImage = "",
            optionAImage = "",
            optionBImage = "",
            optionCImage = "",
            optionDImage = "",
            correctAnswer = "A"
        )

        val formattedLine = CsvHelper.formatQuestionLine(q)
        val fullCsv = "${CsvHelper.QUESTIONS_HEADER}\n$formattedLine"
        val parsed = CsvHelper.parseQuestions(fullCsv)

        assertEquals(1, parsed.size)
        assertEquals("यूपी एसआई (UPSI)", parsed[0].exam)
        assertEquals(hindiQuestion, parsed[0].questionText)
        assertEquals(hindiOptA, parsed[0].optionA)
        assertEquals("A", parsed[0].correctAnswer)
    }

    @Test
    fun testEmptyFields_preservedCorrectly() {
        val csv = CsvHelper.QUESTIONS_HEADER + "\n" +
            "UPSI,Q104,Law,IPC,,Empty topic question,A,B,C,D,,,,,,D\n"

        val parsed = CsvHelper.parseQuestions(csv)
        assertEquals(1, parsed.size)
        assertEquals("", parsed[0].topic)
        assertEquals("", parsed[0].questionImage)
        assertEquals("D", parsed[0].correctAnswer)
    }

    @Test
    fun testLegacyShortRows_11ColumnsWithoutImageUrls() {
        val legacyHeader = "exam,question_id,subject,chapter,topic,question_text,option_a,option_b,option_c,option_d,correct_answer"
        val legacyCsv = "$legacyHeader\nUPSI,Q_LEGACY,Law,IPC,Preamble,Legacy question?,OptA,OptB,OptC,OptD,A\n"

        val parsed = CsvHelper.parseQuestions(legacyCsv)
        assertEquals(1, parsed.size)
        assertEquals("Q_LEGACY", parsed[0].questionId)
        assertEquals("Legacy question?", parsed[0].questionText)
        assertEquals("", parsed[0].questionImage)
        assertEquals("A", parsed[0].correctAnswer)
    }

    @Test
    fun testValidateQuestionsHeader_tightTokenChecking() {
        assertTrue(CsvHelper.validateQuestionsHeader(CsvHelper.QUESTIONS_HEADER))

        val reordered = "exam,subject,chapter,topic,question_id,question_text,option_a,option_b,option_c,option_d,correct_answer"
        assertTrue(CsvHelper.validateQuestionsHeader(reordered))

        val deceptiveHeader = "exam_date,old_question_id_ref,question_text_summary,number_of_correct_answers"
        assertFalse(CsvHelper.validateQuestionsHeader(deceptiveHeader))

        val missingCorrectAnswer = "exam,question_id,subject,chapter,topic,question_text,option_a,option_b,option_c,option_d"
        assertFalse(CsvHelper.validateQuestionsHeader(missingCorrectAnswer))
    }
}
