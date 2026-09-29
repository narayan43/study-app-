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

        // 1. questions.csv (3 exams, 16 chapters, 80 questions)
        val questionsFile = File(questionsDir, "questions.csv")
        if (!questionsFile.exists() || questionsFile.length() == 0L) {
            val sb = StringBuilder()
            sb.append(CsvHelper.QUESTIONS_HEADER).append("\n")

            var qIndex = 1

            // 1. UPSI (10 chapters, 5 questions each = 50 questions)
            val upsiChapters = listOf(
                Triple("Mool Vidhi & Samvidhan", "IPC Offences against Body", "IPC 299-304B"),
                Triple("Mool Vidhi & Samvidhan", "CrPC Police Powers & Arrest", "Section 41"),
                Triple("Mool Vidhi & Samvidhan", "Fundamental Rights & Writs", "Articles 14-32"),
                Triple("Mool Vidhi & Samvidhan", "Special Acts & Cyber Law", "IT Act 2000"),
                Triple("General Hindi", "Vyakaran Sandhi & Samasa", "Swara Sandhi"),
                Triple("General Hindi", "Alankar & Rasa", "Yamak & Veer Rasa"),
                Triple("Numerical Ability", "Percentage & Profit Loss", "Successive Discount"),
                Triple("Numerical Ability", "Compound Interest & Time Work", "CI/SI Formulas"),
                Triple("Mental Aptitude & Reasoning", "Blood Relations & Direction Test", "Coded Relations"),
                Triple("Mental Aptitude & Reasoning", "Syllogism & Coding Decoding", "Statements & Conclusions")
            )

            for ((subject, chapter, topic) in upsiChapters) {
                for (j in 1..5) {
                    val qId = "q_upsi_${qIndex++}"
                    val text = "Question $j on $chapter: Under $topic, which provision applies?"
                    val optA = "Option A regarding $chapter"
                    val optB = "Option B regarding $chapter"
                    val optC = "Option C regarding $chapter"
                    val optD = "Option D regarding $chapter"
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
                Triple("General Ability Test (GAT)", "English Grammar & Synonyms", "Vocabulary")
            )

            for ((subject, chapter, topic) in ndaChapters) {
                for (j in 1..5) {
                    val qId = "q_nda_${qIndex++}"
                    val text = "NDA Question $j on $chapter: What is the core theorem in $topic?"
                    val optA = "Theorem statement A"
                    val optB = "Theorem statement B"
                    val optC = "Theorem statement C"
                    val optD = "Theorem statement D"
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
                    val qId = "q_pet_${qIndex++}"
                    val text = "UPSSSC PET Question $j on $chapter: Key milestone in $topic?"
                    val optA = "Event occurrence A"
                    val optB = "Event occurrence B"
                    val optC = "Event occurrence C"
                    val optD = "Event occurrence D"
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

        // 2. notes.csv & notes/files/*.txt
        val notesFile = File(notesDir, "notes.csv")
        if (!notesFile.exists() || notesFile.length() == 0L) {
            val sb = StringBuilder()
            sb.append(CsvHelper.NOTES_HEADER).append("\n")

            val dummyNotes = listOf(
                Triple("note_upsi_ipc", "UPSI", "Mool Vidhi & Samvidhan to IPC Offences against Body"),
                Triple("note_upsi_crpc", "UPSI", "Mool Vidhi & Samvidhan to CrPC Police Powers & Arrest"),
                Triple("note_upsi_writs", "UPSI", "Mool Vidhi & Samvidhan to Fundamental Rights & Writs"),
                Triple("note_upsi_hindi", "UPSI", "General Hindi to Vyakaran Sandhi & Samasa"),
                Triple("note_nda_trig", "NDA", "Mathematics to Trigonometry & Heights"),
                Triple("note_nda_calc", "NDA", "Mathematics to Calculus & Derivatives"),
                Triple("note_pet_history", "UPSSSC_PET", "General Studies to Indian National Movement")
            )

            for ((noteId, exam, subChap) in dummyNotes) {
                val parts = subChap.split(" to ")
                val subject = parts[0]
                val chapter = parts[1]
                val topic = chapter
                val title = "Study & Revision Guide for $chapter"
                val relPath = "notes/files/$noteId.txt"

                sb.append("$noteId,$exam,\"$subject\",\"$chapter\",\"$topic\",\"$title\",\"$relPath\",markdown\n")

                // Write note text file
                val noteContentFile = File(rootDir, relPath)
                noteContentFile.parentFile?.mkdirs()
                noteContentFile.writeText(
                    """
                    # $title ($exam)
                    Subject: $subject
                    Chapter: $chapter
                    
                    ## Key Concepts & Overview
                    1. High-frequency concepts for $chapter.
                    2. Landmark questions and statutory definitions.
                    3. Practice with the linked questions from the test button below.
                    """.trimIndent()
                )
            }
            notesFile.writeText(sb.toString())
        }

        // 3. videos.csv
        val videosFile = File(videosDir, "videos.csv")
        if (!videosFile.exists() || videosFile.length() == 0L) {
            val sb = StringBuilder()
            sb.append(CsvHelper.VIDEOS_HEADER).append("\n")
            val dummyVideos = listOf(
                listOf("vid_upsi_ipc", "UPSI", "Mool Vidhi & Samvidhan", "IPC Offences against Body", "IPC 299-304B", "IPC Marathon Class", "videos/ipc_class.mp4", "1800"),
                listOf("vid_upsi_crpc", "UPSI", "Mool Vidhi & Samvidhan", "CrPC Police Powers & Arrest", "Section 41", "CrPC Police Investigation", "videos/crpc_class.mp4", "1500"),
                listOf("vid_upsi_hindi", "UPSI", "General Hindi", "Vyakaran Sandhi & Samasa", "Swara Sandhi", "Hindi Grammar Short Tricks", "videos/hindi_class.mp4", "1200"),
                listOf("vid_nda_trig", "NDA", "Mathematics", "Trigonometry & Heights", "Identities", "Trigonometry Super Speed", "videos/trig_class.mp4", "2100"),
                listOf("vid_pet_history", "UPSSSC_PET", "General Studies", "Indian National Movement", "1857 to 1947", "Modern Indian History Overview", "videos/history_class.mp4", "1600")
            )
            for (row in dummyVideos) {
                sb.append("${row[0]},${row[1]},\"${row[2]}\",\"${row[3]}\",\"${row[4]}\",\"${row[5]}\",\"${row[6]}\",${row[7]}\n")
            }
            videosFile.writeText(sb.toString())
        }

        // 4. links/note_questions.csv
        val noteQuestionsFile = File(linksDir, "note_questions.csv")
        if (!noteQuestionsFile.exists() || noteQuestionsFile.length() == 0L) {
            val sb = StringBuilder()
            sb.append(CsvHelper.NOTE_QUESTIONS_HEADER).append("\n")
            sb.append("note_upsi_ipc,q_upsi_1\n")
            sb.append("note_upsi_ipc,q_upsi_2\n")
            sb.append("note_upsi_ipc,q_upsi_3\n")
            sb.append("note_upsi_crpc,q_upsi_6\n")
            sb.append("note_upsi_crpc,q_upsi_7\n")
            sb.append("note_upsi_writs,q_upsi_11\n")
            sb.append("note_upsi_writs,q_upsi_12\n")
            sb.append("note_upsi_hindi,q_upsi_21\n")
            sb.append("note_upsi_hindi,q_upsi_22\n")
            sb.append("note_nda_trig,q_nda_51\n")
            sb.append("note_nda_trig,q_nda_52\n")
            sb.append("note_nda_calc,q_nda_61\n")
            sb.append("note_pet_history,q_pet_71\n")
            sb.append("note_pet_history,q_pet_72\n")
            noteQuestionsFile.writeText(sb.toString())
        }

        // 5. links/video_questions.csv
        val videoQuestionsFile = File(linksDir, "video_questions.csv")
        if (!videoQuestionsFile.exists() || videoQuestionsFile.length() == 0L) {
            val sb = StringBuilder()
            sb.append(CsvHelper.VIDEO_QUESTIONS_HEADER).append("\n")
            sb.append("vid_upsi_ipc,q_upsi_1\n")
            sb.append("vid_upsi_ipc,q_upsi_2\n")
            sb.append("vid_upsi_crpc,q_upsi_6\n")
            sb.append("vid_upsi_hindi,q_upsi_21\n")
            sb.append("vid_nda_trig,q_nda_51\n")
            sb.append("vid_pet_history,q_pet_71\n")
            videoQuestionsFile.writeText(sb.toString())
        }

        // 6. logs/attempts.csv (header only)
        val attemptsFile = File(logsDir, "attempts.csv")
        if (!attemptsFile.exists() || attemptsFile.length() == 0L) {
            attemptsFile.writeText(CsvHelper.ATTEMPTS_HEADER + "\n")
        }

        // 7. logs/notes_usage.csv (header only)
        val notesUsageFile = File(logsDir, "notes_usage.csv")
        if (!notesUsageFile.exists() || notesUsageFile.length() == 0L) {
            notesUsageFile.writeText(CsvHelper.NOTES_USAGE_HEADER + "\n")
        }

        // 8. logs/video_usage.csv (header only)
        val videoUsageFile = File(logsDir, "video_usage.csv")
        if (!videoUsageFile.exists() || videoUsageFile.length() == 0L) {
            videoUsageFile.writeText(CsvHelper.VIDEO_USAGE_HEADER + "\n")
        }
    }
}
