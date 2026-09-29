package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class MockTestConfig(
    val id: String,
    val title: String,
    val description: String,
    val subject: String? = null, // null for full mock
    val questionCount: Int,
    val durationMinutes: Int,
    val totalMarks: Float,
    val iconName: String = "quiz"
)

data class MockQuestionState(
    val question: QuestionEntity,
    val selectedOption: String? = null,
    val isFlagged: Boolean = false,
    val isVisited: Boolean = false,
    val timeSpentSeconds: Int = 0
)

@Entity(tableName = "mock_test_results")
data class MockTestResultEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val totalQuestions: Int,
    val attemptedCount: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val unattemptedCount: Int,
    val score: Float,
    val maxScore: Float,
    val accuracyPercent: Float,
    val timeSpentSeconds: Int,
    val isPassed: Boolean, // UPSI criteria: >= 35% in each section and >= 50% overall
    val remarks: String
)
