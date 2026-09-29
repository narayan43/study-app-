package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuestionEntity
import com.example.ui.components.SectionBadge
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.IncorrectRed
import com.example.ui.theme.PoliceGoldDark
import com.example.ui.theme.PoliceGoldLight
import com.example.ui.theme.PoliceNavyDark
import com.example.ui.theme.PoliceNavyPrimary
import com.example.ui.viewmodel.UPSIViewModel

@Composable
fun PracticeScreen(
    viewModel: UPSIViewModel
) {
    val isHindi by viewModel.isBilingualHindi.collectAsState()
    val practiceQuestions by viewModel.filteredPracticeQuestions.collectAsState()
    val practiceAnswers by viewModel.practiceAnswers.collectAsState()
    val selectedSubject by viewModel.practiceSubjectFilter.collectAsState()
    val bookmarksOnly by viewModel.practiceBookmarkOnly.collectAsState()
    val mistakesOnly by viewModel.practiceMistakesOnly.collectAsState()

    var currentIndex by remember(practiceQuestions) { mutableIntStateOf(0) }

    // Safe bounds check
    val safeIndex = if (practiceQuestions.isEmpty()) 0 else currentIndex.coerceIn(0, practiceQuestions.size - 1)
    val currentQuestion = practiceQuestions.getOrNull(safeIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("practice_screen")
    ) {
        // --- Subject & Status Filter Bar ---
        SubjectFilterRow(
            selectedSubject = selectedSubject,
            bookmarksOnly = bookmarksOnly,
            mistakesOnly = mistakesOnly,
            isHindi = isHindi,
            onSelectFilter = { subj, bkmk, mstk ->
                currentIndex = 0
                viewModel.setPracticeFilter(subj, bkmk, mstk)
            }
        )

        if (practiceQuestions.isEmpty()) {
            EmptyPracticeState(
                isHindi = isHindi,
                onResetFilter = {
                    viewModel.setPracticeFilter(null, false, false)
                }
            )
        } else if (currentQuestion != null) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header info
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "प्रश्न ${safeIndex + 1} / ${practiceQuestions.size}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PoliceNavyPrimary
                                )
                            )
                            if (currentQuestion.isPYQ) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = PoliceGoldLight.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = currentQuestion.pyqYear.ifBlank { "PYQ" },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = PoliceGoldDark,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { viewModel.toggleBookmark(currentQuestion) },
                            modifier = Modifier.testTag("practice_bookmark_toggle")
                        ) {
                            Icon(
                                imageVector = if (currentQuestion.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (currentQuestion.isBookmarked) PoliceGoldDark else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Question Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("practice_question_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                SectionBadge(subject = currentQuestion.subject)
                                Text(
                                    text = currentQuestion.topic,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = if (isHindi) currentQuestion.questionHindi else currentQuestion.questionEnglish,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp
                                )
                            )

                            if (isHindi && currentQuestion.questionEnglish.isNotBlank() && currentQuestion.questionEnglish != currentQuestion.questionHindi) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = currentQuestion.questionEnglish,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // Options
                val answerState = practiceAnswers[currentQuestion.id]
                val selectedOption = answerState?.first
                val isSubmitted = answerState?.second ?: false

                items(
                    listOf(
                        "A" to currentQuestion.optionA,
                        "B" to currentQuestion.optionB,
                        "C" to currentQuestion.optionC,
                        "D" to currentQuestion.optionD
                    )
                ) { (key, text) ->
                    PracticeOptionButton(
                        key = key,
                        text = text,
                        isSelected = selectedOption == key,
                        isSubmitted = isSubmitted,
                        isCorrect = key.equals(currentQuestion.correctOption, ignoreCase = true),
                        onClick = {
                            viewModel.selectPracticeAnswer(currentQuestion, key)
                        }
                    )
                }

                // Explanation Box (shown after answer is clicked)
                if (isSubmitted) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("practice_explanation_card"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedOption.equals(currentQuestion.correctOption, ignoreCase = true))
                                    Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = if (selectedOption.equals(currentQuestion.correctOption, ignoreCase = true))
                                            CorrectGreen else IncorrectRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isHindi) "विस्तृत व्याख्या (हल)" else "Detailed Solution & Notes",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedOption.equals(currentQuestion.correctOption, ignoreCase = true))
                                                CorrectGreen else IncorrectRed
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "सही उत्तर: विकल्प ${currentQuestion.correctOption.uppercase()}",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CorrectGreen
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentQuestion.explanation,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        color = Color(0xFF263238)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Prev / Next
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (safeIndex > 0) currentIndex = safeIndex - 1 },
                        enabled = safeIndex > 0,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("practice_prev_button")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isHindi) "पिछला" else "Previous")
                    }

                    Text(
                        text = "${safeIndex + 1} / ${practiceQuestions.size}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )

                    Button(
                        onClick = {
                            if (safeIndex < practiceQuestions.size - 1) {
                                currentIndex = safeIndex + 1
                            }
                        },
                        enabled = safeIndex < practiceQuestions.size - 1,
                        colors = ButtonDefaults.buttonColors(containerColor = PoliceNavyPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("practice_next_button")
                    ) {
                        Text(if (isHindi) "अगला" else "Next")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectFilterRow(
    selectedSubject: String?,
    bookmarksOnly: Boolean,
    mistakesOnly: Boolean,
    isHindi: Boolean,
    onSelectFilter: (String?, Boolean, Boolean) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedSubject == null && !bookmarksOnly && !mistakesOnly,
                onClick = { onSelectFilter(null, false, false) },
                label = { Text(if (isHindi) "सभी प्रश्न" else "All") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PoliceNavyPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
        item {
            FilterChip(
                selected = selectedSubject == "General Hindi",
                onClick = { onSelectFilter("General Hindi", false, false) },
                label = { Text(if (isHindi) "हिन्दी" else "Hindi") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PoliceNavyPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
        item {
            FilterChip(
                selected = selectedSubject == "Law & Constitution",
                onClick = { onSelectFilter("Law & Constitution", false, false) },
                label = { Text(if (isHindi) "मूल विधि व संविधान" else "Law & Const") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PoliceNavyPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
        item {
            FilterChip(
                selected = selectedSubject == "Numerical & Mental Ability",
                onClick = { onSelectFilter("Numerical & Mental Ability", false, false) },
                label = { Text(if (isHindi) "गणित (Maths)" else "Maths") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PoliceNavyPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
        item {
            FilterChip(
                selected = selectedSubject == "Mental Aptitude & Reasoning",
                onClick = { onSelectFilter("Mental Aptitude & Reasoning", false, false) },
                label = { Text(if (isHindi) "रीजनिंग" else "Reasoning") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PoliceNavyPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
        item {
            FilterChip(
                selected = bookmarksOnly,
                onClick = { onSelectFilter(null, true, false) },
                label = { Text(if (isHindi) "बुकमार्क" else "Bookmarks") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PoliceGoldDark,
                    selectedLabelColor = Color.White
                )
            )
        }
        item {
            FilterChip(
                selected = mistakesOnly,
                onClick = { onSelectFilter(null, false, true) },
                label = { Text(if (isHindi) "गलतियां सुधारें" else "Mistakes") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = IncorrectRed,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun PracticeOptionButton(
    key: String,
    text: String,
    isSelected: Boolean,
    isSubmitted: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        !isSubmitted && isSelected -> PoliceNavyPrimary
        isSubmitted && isCorrect -> CorrectGreen
        isSubmitted && isSelected && !isCorrect -> IncorrectRed
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val containerColor = when {
        !isSubmitted && isSelected -> PoliceNavyPrimary.copy(alpha = 0.08f)
        isSubmitted && isCorrect -> CorrectGreen.copy(alpha = 0.12f)
        isSubmitted && isSelected && !isCorrect -> IncorrectRed.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isSubmitted) { onClick() }
            .testTag("practice_option_$key"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isSelected || (isSubmitted && isCorrect)) 2.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isSubmitted && isCorrect -> CorrectGreen
                            isSubmitted && isSelected && !isCorrect -> IncorrectRed
                            isSelected -> PoliceNavyPrimary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSubmitted && isCorrect) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Correct", tint = Color.White, modifier = Modifier.size(18.dp))
                } else if (isSubmitted && isSelected && !isCorrect) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Incorrect", tint = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text(
                        text = key,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isSelected || (isSubmitted && isCorrect)) FontWeight.Bold else FontWeight.Normal
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EmptyPracticeState(
    isHindi: Boolean,
    onResetFilter: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.FilterAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isHindi) "इस फ़िल्टर में कोई प्रश्न नहीं मिले" else "No questions found for this filter",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isHindi) "कृपया फ़िल्टर बदलें या सभी प्रश्न देखें।" else "Change filter to view more practice questions.",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onResetFilter,
                colors = ButtonDefaults.buttonColors(containerColor = PoliceNavyPrimary)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isHindi) "फ़िल्टर हटाएं" else "Reset Filter")
            }
        }
    }
}
