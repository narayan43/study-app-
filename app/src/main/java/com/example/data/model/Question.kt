package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Subject(val displayName: String, val hindiName: String) {
    GENERAL_HINDI("General Hindi", "सामान्य हिन्दी"),
    LAW_CONSTITUTION("Law & Constitution", "मूल विधि एवं संविधान / सामान्य ज्ञान"),
    NUMERICAL_ABILITY("Numerical & Mental Ability", "संख्यात्मक एवं मानसिक योग्यता"),
    REASONING("Mental Aptitude & Reasoning", "मानसिक अभिरुचि, बुद्धिलब्धि एवं तार्किक परीक्षा")
}

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey
    val id: String,
    val subject: String,
    val topic: String,
    val questionHindi: String,
    val questionEnglish: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String, // "A", "B", "C", "D"
    val explanation: String,
    val difficulty: String = "Medium", // Easy, Medium, Hard
    val isPYQ: Boolean = false,
    val pyqYear: String = "",
    val isBookmarked: Boolean = false
)
