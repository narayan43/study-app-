package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val subject: String,
    val topic: String,
    val durationSec: Int = 1200, // e.g., 20 mins
    val instructor: String = "UPSI Police Academy Mentor",
    val description: String = "",
    val tags: String = "",
    val videoUrlOrPath: String = ""
)

@Entity(tableName = "video_questions", primaryKeys = ["videoId", "questionId"])
data class VideoQuestionCrossRef(
    val videoId: String,
    val questionId: String
)

@Entity(tableName = "video_usage")
data class VideoUsageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val videoId: String,
    val watchTimeSec: Int,
    val lastPositionSec: Int = 0,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
