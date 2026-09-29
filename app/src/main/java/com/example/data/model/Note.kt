package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val subject: String,
    val topic: String,
    val contentMarkdown: String,
    val readTimeMin: Int = 10,
    val tags: String = "",
    val isBookmarked: Boolean = false,
    val createdAt: String = "2026-09-28"
)

@Entity(tableName = "note_questions", primaryKeys = ["noteId", "questionId"])
data class NoteQuestionCrossRef(
    val noteId: String,
    val questionId: String
)

@Entity(tableName = "notes_usage")
data class NoteUsageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteId: String,
    val timeSpentSec: Int,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
