package com.example.data.model

data class LawArticle(
    val id: String,
    val category: LawCategory,
    val titleHindi: String,
    val titleEnglish: String,
    val keyPointsHindi: String,
    val keyPointsEnglish: String,
    val punishmentOrProvision: String,
    val landmarkCaseOrNote: String
)

enum class LawCategory(val displayName: String, val hindiName: String) {
    IPC("Indian Penal Code (IPC)", "भारतीय दण्ड संहिता"),
    CRPC("Code of Criminal Procedure (CrPC)", "दण्ड प्रक्रिया संहिता"),
    CONSTITUTION("Constitution of India", "भारत का संविधान"),
    SPECIAL_ACTS("Special Acts & Police Rules", "विशेष अधिनियम व पुलिस व्यवस्था")
}
