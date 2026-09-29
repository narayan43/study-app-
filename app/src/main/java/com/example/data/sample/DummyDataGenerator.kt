package com.example.data.sample

import com.example.data.csv.CsvHelper
import java.io.File

object DummyDataGenerator {

    fun generateDummyDataTree(rootDir: File) {
        val questionsDir = File(rootDir, "questions").apply { mkdirs() }
        val notesDir = File(rootDir, "notes").apply { mkdirs() }
        val notesFilesDir = File(rootDir, "notes/files").apply { mkdirs() }
        val videosDir = File(rootDir, "videos").apply { mkdirs() }
        val linksDir = File(rootDir, "links").apply { mkdirs() }
        val logsDir = File(rootDir, "logs").apply { mkdirs() }

        // 1. questions.csv (3 exams UPSI, NDA, UPSSSC_PET, 16 chapters, 80 questions)
        // IDs: Q001... matching official spec with Q001..Q005 in UPSI Polity Preamble
        val questionsFile = File(questionsDir, "questions.csv")
        if (!questionsFile.exists() || questionsFile.length() == 0L) {
            val sb = StringBuilder()
            sb.append(CsvHelper.QUESTIONS_HEADER).append("\n")

            // 1. UPSI Chapters (10 chapters = 50 questions)
            // Chapter 1: Polity -> Preamble (Q001..Q005)
            sb.append("UPSI,Q001,Polity,Preamble,Philosophy,\"Which words were added to the Preamble by the 42nd Amendment Act?\",Socialist Secular Integrity,Democratic Republic,Sovereignty Justice,Liberty Equality,,,,,,A\n")
            sb.append("UPSI,Q002,Polity,Preamble,Text,\"The Preamble to the Indian Constitution derives its inspiration from the Objective Resolution introduced by whom?\",Jawaharlal Nehru,B. R. Ambedkar,Sardar Patel,Rajendra Prasad,,,,,,A\n")
            sb.append("UPSI,Q003,Polity,Preamble,Status,\"In which landmark case did the Supreme Court hold that the Preamble is an integral part of the Constitution?\",Kesavananda Bharati,Berubari Union,Golaknath,Minerva Mills,,,,,,A\n")
            sb.append("UPSI,Q004,Polity,Preamble,Values,\"Which of the following ideals is mentioned first in the Preamble?\",Justice,Liberty,Equality,Fraternity,,,,,,A\n")
            sb.append("UPSI,Q005,Polity,Preamble,Adoption,\"On which date was the Constitution of India formally adopted by the Constituent Assembly?\",26 November 1949,26 January 1950,15 August 1947,9 December 1946,,,,,,A\n")

            var qNum = 6
            val remainingUpsiChapters = listOf(
                Triple("Mool Vidhi", "IPC Offences Against Body", "IPC 299-304B"),
                Triple("Mool Vidhi", "CrPC Police Powers & Arrest", "Section 41"),
                Triple("Mool Vidhi", "Special Acts & Cyber Law", "IT Act 2000"),
                Triple("Polity", "Fundamental Rights & Writs", "Articles 14-32"),
                Triple("General Hindi", "Vyakaran Sandhi & Samasa", "Swara Sandhi"),
                Triple("General Hindi", "Alankar & Rasa", "Yamak & Veer Rasa"),
                Triple("Numerical Ability", "Percentage & Profit Loss", "Successive Discount"),
                Triple("Numerical Ability", "Compound Interest & Time Work", "CI/SI Formulas"),
                Triple("Reasoning", "Blood Relations & Direction", "Coded Relations")
            )

            for ((subject, chapter, topic) in remainingUpsiChapters) {
                for (j in 1..5) {
                    val qId = String.format("Q%03d", qNum++)
                    val text = "Question on $chapter: In the context of $topic, which statement is legally accurate?"
                    val optA = "Provision A under $chapter"
                    val optB = "Provision B under $chapter"
                    val optC = "Provision C under $chapter"
                    val optD = "Provision D under $chapter"
                    val correct = when (j % 4) {
                        0 -> "A"
                        1 -> "B"
                        2 -> "C"
                        else -> "D"
                    }
                    sb.append("UPSI,$qId,\"$subject\",\"$chapter\",\"$topic\",\"$text\",\"$optA\",\"$optB\",\"$optC\",\"$optD\",\"\",\"\",\"\",\"\",\"\",$correct\n")
                }
            }

            // 2. NDA (4 chapters, 5 questions each = 20 questions)
            val ndaChapters = listOf(
                Triple("Mathematics", "Trigonometry & Heights", "Identities"),
                Triple("Mathematics", "Matrices & Determinants", "Cramer Rule"),
                Triple("Mathematics", "Calculus & Derivatives", "Maxima Minima"),
                Triple("GAT", "English Grammar & Synonyms", "Vocabulary")
            )

            for ((subject, chapter, topic) in ndaChapters) {
                for (j in 1..5) {
                    val qId = String.format("Q%03d", qNum++)
                    val text = "NDA Question on $chapter: Evaluate the mathematical or verbal condition in $topic."
                    val optA = "Theorem result A"
                    val optB = "Theorem result B"
                    val optC = "Theorem result C"
                    val optD = "Theorem result D"
                    val correct = when (j % 4) {
                        0 -> "B"
                        1 -> "A"
                        2 -> "D"
                        else -> "C"
                    }
                    sb.append("NDA,$qId,\"$subject\",\"$chapter\",\"$topic\",\"$text\",\"$optA\",\"$optB\",\"$optC\",\"$optD\",\"\",\"\",\"\",\"\",\"\",$correct\n")
                }
            }

            // 3. UPSSSC_PET (2 chapters, 5 questions each = 10 questions)
            val petChapters = listOf(
                Triple("General Studies", "Indian National Movement", "1857 to 1947"),
                Triple("General Studies", "Indian Geography & Rivers", "River Systems")
            )

            for ((subject, chapter, topic) in petChapters) {
                for (j in 1..5) {
                    val qId = String.format("Q%03d", qNum++)
                    val text = "UPSSSC PET Question on $chapter: Historic milestone or physical feature in $topic."
                    val optA = "Fact A regarding $chapter"
                    val optB = "Fact B regarding $chapter"
                    val optC = "Fact C regarding $chapter"
                    val optD = "Fact D regarding $chapter"
                    val correct = when (j % 4) {
                        0 -> "C"
                        1 -> "D"
                        2 -> "A"
                        else -> "B"
                    }
                    sb.append("UPSSSC_PET,$qId,\"$subject\",\"$chapter\",\"$topic\",\"$text\",\"$optA\",\"$optB\",\"$optC\",\"$optD\",\"\",\"\",\"\",\"\",\"\",$correct\n")
                }
            }

            questionsFile.writeText(sb.toString())
        }

        // 2. notes.csv & notes/files/*
        val notesFile = File(notesDir, "notes.csv")
        if (!notesFile.exists() || notesFile.length() == 0L) {
            val sb = StringBuilder()
            sb.append(CsvHelper.NOTES_HEADER).append("\n")

            // Note N001 for Polity -> Preamble
            sb.append("N001,UPSI,Polity,Preamble,Philosophy,\"Preamble & Constitutional Philosophy\",notes/files/N001.txt,markdown\n")

            // Write note content file for N001
            val n001File = File(rootDir, "notes/files/N001.txt")
            n001File.parentFile?.mkdirs()
            n001File.writeText(
                """
                # Preamble to the Constitution of India (UPSI)
                Subject: Polity | Chapter: Preamble
                
                ### Key Statutory & Judicial Points:
                1. **42nd Constitutional Amendment Act (1976)**:
                   - Added three words: **Socialist**, **Secular**, and **Integrity**.
                2. **Kesavananda Bharati v. State of Kerala (1973)**:
                   - The Supreme Court held that the Preamble is an integral part of the Constitution and can be amended under Article 368 without altering the basic structure.
                3. **Objective Resolution**:
                   - Drafted and moved by Pandit Jawaharlal Nehru on December 13, 1946, adopted on January 22, 1947.
                4. **Adoption**:
                   - Adopted by the Constituent Assembly on 26 November 1949 (National Law Day / Constitution Day).
                """.trimIndent()
            )

            // Other notes matching remaining chapters
            val otherNotes = listOf(
                Triple("N002", "UPSI", "Mool Vidhi to IPC Offences Against Body"),
                Triple("N003", "UPSI", "Mool Vidhi to CrPC Police Powers & Arrest"),
                Triple("N004", "UPSI", "Polity to Fundamental Rights & Writs"),
                Triple("N005", "UPSI", "General Hindi to Vyakaran Sandhi & Samasa"),
                Triple("N006", "NDA", "Mathematics to Trigonometry & Heights"),
                Triple("N007", "NDA", "Mathematics to Calculus & Derivatives"),
                Triple("N008", "UPSSSC_PET", "General Studies to Indian National Movement")
            )

            for ((nId, exam, subChap) in otherNotes) {
                val parts = subChap.split(" to ")
                val sub = parts[0]
                val chap = parts[1]
                val relPath = "notes/files/$nId.txt"
                sb.append("$nId,$exam,\"$sub\",\"$chap\",\"$chap\",\"Study Guide for $chap\",\"$relPath\",markdown\n")

                val f = File(rootDir, relPath)
                f.parentFile?.mkdirs()
                f.writeText(
                    """
                    # Revision Note $nId: $chap ($exam)
                    Subject: $sub
                    
                    Key exam formulas and high-yield rules for $chap.
                    Review the practice questions linked to this note using the test button below.
                    """.trimIndent()
                )
            }
            notesFile.writeText(sb.toString())
        }

        // 3. videos.csv (V001 for Polity -> Preamble, plus others)
        val videosFile = File(videosDir, "videos.csv")
        if (!videosFile.exists() || videosFile.length() == 0L) {
            val sb = StringBuilder()
            sb.append(CsvHelper.VIDEOS_HEADER).append("\n")
            sb.append("V001,UPSI,Polity,Preamble,Philosophy,\"Preamble Landmark Judgments & Terms\",videos/files/V001.mp4,1200\n")
            sb.append("V002,UPSI,Mool Vidhi,IPC Offences Against Body,IPC 299-304B,\"IPC Murder vs Culpable Homicide Masterclass\",videos/files/V002.mp4,1800\n")
            sb.append("V003,UPSI,Mool Vidhi,CrPC Police Powers & Arrest,Section 41,\"CrPC Arrest & Notice Rules\",videos/files/V003.mp4,1500\n")
            sb.append("V004,UPSI,General Hindi,Vyakaran Sandhi & Samasa,Swara Sandhi,\"Hindi Sandhi Super Trick\",videos/files/V004.mp4,1100\n")
            sb.append("V005,NDA,Mathematics,Trigonometry & Heights,Identities,\"NDA Trigonometry Shortcuts\",videos/files/V005.mp4,2100\n")
            sb.append("V006,UPSSSC_PET,General Studies,Indian National Movement,1857 to 1947,\"Indian Freedom Struggle Timeline\",videos/files/V006.mp4,1600\n")
            videosFile.writeText(sb.toString())
        }

        // 4. links/note_questions.csv (Link N001 to all five Q001..Q005!)
        val noteQuestionsFile = File(linksDir, "note_questions.csv")
        if (!noteQuestionsFile.exists() || noteQuestionsFile.length() == 0L) {
            val sb = StringBuilder()
            sb.append(CsvHelper.NOTE_QUESTIONS_HEADER).append("\n")
            sb.append("N001,Q001\n")
            sb.append("N001,Q002\n")
            sb.append("N001,Q003\n")
            sb.append("N001,Q004\n")
            sb.append("N001,Q005\n")
            sb.append("N002,Q006\n")
            sb.append("N002,Q007\n")
            sb.append("N003,Q011\n")
            sb.append("N004,Q016\n")
            sb.append("N005,Q021\n")
            sb.append("N006,Q051\n")
            sb.append("N008,Q071\n")
            noteQuestionsFile.writeText(sb.toString())
        }

        // 5. links/video_questions.csv (Link V001 to all five Q001..Q005!)
        val videoQuestionsFile = File(linksDir, "video_questions.csv")
        if (!videoQuestionsFile.exists() || videoQuestionsFile.length() == 0L) {
            val sb = StringBuilder()
            sb.append(CsvHelper.VIDEO_QUESTIONS_HEADER).append("\n")
            sb.append("V001,Q001\n")
            sb.append("V001,Q002\n")
            sb.append("V001,Q003\n")
            sb.append("V001,Q004\n")
            sb.append("V001,Q005\n")
            sb.append("V002,Q006\n")
            sb.append("V003,Q011\n")
            sb.append("V004,Q021\n")
            sb.append("V005,Q051\n")
            sb.append("V006,Q071\n")
            videoQuestionsFile.writeText(sb.toString())
        }

        // 6. logs/attempts.csv (with sample ISO-8601 timestamps for 2026-09-27, 28, 29 as required by Gap 1!)
        val attemptsFile = File(logsDir, "attempts.csv")
        if (!attemptsFile.exists() || attemptsFile.length() == 0L) {
            val sb = StringBuilder()
            sb.append(CsvHelper.ATTEMPTS_HEADER).append("\n")
            sb.append("att_001,Q001,UPSI,Polity,Preamble,A,1,14,2026-09-27T10:15:00+05:30\n")
            sb.append("att_002,Q002,UPSI,Polity,Preamble,B,0,25,2026-09-27T10:16:30+05:30\n")
            sb.append("att_003,Q003,UPSI,Polity,Preamble,A,1,18,2026-09-28T14:20:00+05:30\n")
            sb.append("att_004,Q004,UPSI,Polity,Preamble,A,1,22,2026-09-28T14:22:10+05:30\n")
            sb.append("att_005,Q005,UPSI,Polity,Preamble,A,1,12,2026-09-29T09:30:00+05:30\n")
            sb.append("att_006,Q006,UPSI,Mool Vidhi,IPC Offences Against Body,C,0,30,2026-09-29T09:35:00+05:30\n")
            attemptsFile.writeText(sb.toString())
        }

        // 7. logs/notes_usage.csv
        val notesUsageFile = File(logsDir, "notes_usage.csv")
        if (!notesUsageFile.exists() || notesUsageFile.length() == 0L) {
            val sb = StringBuilder()
            sb.append(CsvHelper.NOTES_USAGE_HEADER).append("\n")
            sb.append("nu_001,N001,UPSI,Polity,Preamble,2026-09-27T10:00:00+05:30,2026-09-27T10:12:00+05:30,720\n")
            notesUsageFile.writeText(sb.toString())
        }

        // 8. logs/video_usage.csv
        val videoUsageFile = File(logsDir, "video_usage.csv")
        if (!videoUsageFile.exists() || videoUsageFile.length() == 0L) {
            val sb = StringBuilder()
            sb.append(CsvHelper.VIDEO_USAGE_HEADER).append("\n")
            sb.append("vu_001,V001,UPSI,Polity,Preamble,2026-09-27T10:20:00+05:30,2026-09-27T10:40:00+05:30,1200,1\n")
            videoUsageFile.writeText(sb.toString())
        }
    }
}
