package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weekly_reviews")
data class WeeklyReviewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weekStart: String, // e.g. "2026-09-21"
    val writtenOn: String, // e.g. "2026-09-26"
    val learned: String,
    val willChange: String,
    val proudOf: String,
    val questionsAttemptedCount: Int = 0,
    val accuracyPercent: Float = 0f
)
