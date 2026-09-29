package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attempts")
data class AttemptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val questionId: String,
    val chosenOption: String,
    val isCorrect: Boolean,
    val timeTakenSeconds: Int = 0,
    val attemptedAt: Long = System.currentTimeMillis(),
    val mode: String = "PRACTICE", // "PRACTICE" or "MOCK_TEST"
    val mockTestId: String = ""
)
