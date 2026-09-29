package com.example.data.repository

import com.example.data.local.AttemptDao
import com.example.data.local.MockTestResultDao
import com.example.data.local.NoteDao
import com.example.data.local.QuestionDao
import com.example.data.local.VideoDao
import com.example.data.local.WeeklyReviewDao
import com.example.data.model.AttemptEntity
import com.example.data.model.MockTestResultEntity
import com.example.data.model.NoteEntity
import com.example.data.model.NoteQuestionCrossRef
import com.example.data.model.NoteUsageEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.VideoEntity
import com.example.data.model.VideoQuestionCrossRef
import com.example.data.model.VideoUsageEntity
import com.example.data.model.WeeklyReviewEntity
import com.example.data.sample.UPSIPreloadedNotesAndVideos
import com.example.data.sample.UPSIPreloadedQuestions
import kotlinx.coroutines.flow.Flow

class UPSIRepository(
    private val questionDao: QuestionDao,
    private val attemptDao: AttemptDao,
    private val weeklyReviewDao: WeeklyReviewDao,
    private val mockTestResultDao: MockTestResultDao,
    private val noteDao: NoteDao,
    private val videoDao: VideoDao
) {
    val allQuestions: Flow<List<QuestionEntity>> = questionDao.getAllQuestions()
    val bookmarkedQuestions: Flow<List<QuestionEntity>> = questionDao.getBookmarkedQuestions()
    val allAttempts: Flow<List<AttemptEntity>> = attemptDao.getAllAttempts()
    val allWeeklyReviews: Flow<List<WeeklyReviewEntity>> = weeklyReviewDao.getAllWeeklyReviews()
    val allMockTestResults: Flow<List<MockTestResultEntity>> = mockTestResultDao.getAllResults()
    val incorrectQuestionIds: Flow<List<String>> = attemptDao.getIncorrectQuestionIds()

    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()
    val allVideos: Flow<List<VideoEntity>> = videoDao.getAllVideos()
    val allNoteUsages: Flow<List<NoteUsageEntity>> = noteDao.getAllNoteUsages()
    val allVideoUsages: Flow<List<VideoUsageEntity>> = videoDao.getAllVideoUsages()

    suspend fun initializePreloadedDataIfNeeded() {
        val count = questionDao.getQuestionCount()
        if (count == 0) {
            questionDao.insertQuestions(UPSIPreloadedQuestions.questions)
            
            // Add a sample weekly review matching weekly_reviews.csv
            val sampleReview = WeeklyReviewEntity(
                weekStart = "2026-09-15",
                writtenOn = "2026-09-21",
                learned = "Mastered IPC Sections 300-304B (Murder vs Culpable Homicide & Dowry Death), Fundamental Rights Writs (Habeas Corpus & Mandamus), and Hindi Alankar (Yamak & Rupak).",
                willChange = "Need to practice 20 reasoning blood relation questions daily. Reduce time per math question to under 60 seconds.",
                proudOf = "Achieved 82% in Mool Vidhi sectional mock and maintained a 7-day consistent study streak.",
                questionsAttemptedCount = 65,
                accuracyPercent = 82f
            )
            weeklyReviewDao.insertWeeklyReview(sampleReview)

            // Insert preloaded notes, videos and cross links
            noteDao.insertNotes(UPSIPreloadedNotesAndVideos.notes)
            videoDao.insertVideos(UPSIPreloadedNotesAndVideos.videos)
            noteDao.insertNoteQuestions(UPSIPreloadedNotesAndVideos.noteQuestionLinks)
            videoDao.insertVideoQuestions(UPSIPreloadedNotesAndVideos.videoQuestionLinks)
        }
    }

    suspend fun getQuestionsBySubject(subject: String): Flow<List<QuestionEntity>> {
        return questionDao.getQuestionsBySubject(subject)
    }

    suspend fun toggleBookmark(question: QuestionEntity) {
        questionDao.updateBookmark(question.id, !question.isBookmarked)
    }

    suspend fun recordAttempt(attempt: AttemptEntity) {
        attemptDao.insertAttempt(attempt)
    }

    suspend fun saveWeeklyReview(review: WeeklyReviewEntity): Long {
        return if (review.id == 0L) {
            weeklyReviewDao.insertWeeklyReview(review)
        } else {
            weeklyReviewDao.updateWeeklyReview(review)
            review.id
        }
    }

    suspend fun deleteWeeklyReview(review: WeeklyReviewEntity) {
        weeklyReviewDao.deleteWeeklyReview(review)
    }

    suspend fun saveMockTestResult(result: MockTestResultEntity) {
        mockTestResultDao.insertResult(result)
    }

    suspend fun insertImportedQuestions(questions: List<QuestionEntity>) {
        questionDao.insertQuestions(questions)
    }

    suspend fun insertImportedReviews(reviews: List<WeeklyReviewEntity>) {
        weeklyReviewDao.insertWeeklyReviews(reviews)
    }

    suspend fun getAllAttemptsList(): List<AttemptEntity> {
        return attemptDao.getAllAttemptsList()
    }

    suspend fun getAllWeeklyReviewsList(): List<WeeklyReviewEntity> {
        return weeklyReviewDao.getAllWeeklyReviewsList()
    }

    // Notes and Videos Linking Operations
    suspend fun toggleNoteBookmark(note: NoteEntity) {
        noteDao.updateBookmark(note.id, !note.isBookmarked)
    }

    suspend fun recordNoteUsage(noteId: String, timeSpentSec: Int, isCompleted: Boolean) {
        noteDao.insertNoteUsage(
            NoteUsageEntity(
                noteId = noteId,
                timeSpentSec = timeSpentSec,
                isCompleted = isCompleted
            )
        )
    }

    suspend fun recordVideoUsage(videoId: String, watchTimeSec: Int, lastPositionSec: Int, isCompleted: Boolean) {
        videoDao.insertVideoUsage(
            VideoUsageEntity(
                videoId = videoId,
                watchTimeSec = watchTimeSec,
                lastPositionSec = lastPositionSec,
                isCompleted = isCompleted
            )
        )
    }

    suspend fun getQuestionIdsForNote(noteId: String): List<String> {
        return noteDao.getQuestionIdsForNote(noteId)
    }

    suspend fun getNoteIdsForQuestion(questionId: String): List<String> {
        return noteDao.getNoteIdsForQuestion(questionId)
    }

    suspend fun getQuestionIdsForVideo(videoId: String): List<String> {
        return videoDao.getQuestionIdsForVideo(videoId)
    }

    suspend fun getVideoIdsForQuestion(questionId: String): List<String> {
        return videoDao.getVideoIdsForQuestion(questionId)
    }

    suspend fun insertImportedNotes(notes: List<NoteEntity>) {
        noteDao.insertNotes(notes)
    }

    suspend fun insertImportedVideos(videos: List<VideoEntity>) {
        videoDao.insertVideos(videos)
    }

    suspend fun insertNoteQuestionLinks(links: List<NoteQuestionCrossRef>) {
        noteDao.insertNoteQuestions(links)
    }

    suspend fun insertVideoQuestionLinks(links: List<VideoQuestionCrossRef>) {
        videoDao.insertVideoQuestions(links)
    }

    suspend fun getAllNoteQuestionLinks(): List<NoteQuestionCrossRef> {
        return noteDao.getAllNoteQuestionLinks()
    }

    suspend fun getAllVideoQuestionLinks(): List<VideoQuestionCrossRef> {
        return videoDao.getAllVideoQuestionLinks()
    }
}
