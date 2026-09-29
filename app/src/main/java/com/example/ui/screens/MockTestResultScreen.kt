package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SectionBadge
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.IncorrectRed
import com.example.ui.theme.PoliceGoldDark
import com.example.ui.theme.PoliceGoldLight
import com.example.ui.theme.PoliceNavyDark
import com.example.ui.theme.PoliceNavyPrimary
import com.example.ui.theme.PoliceRedTertiary
import com.example.ui.viewmodel.UPSIViewModel

@Composable
fun MockTestResultScreen(
    viewModel: UPSIViewModel,
    onBackHome: () -> Unit,
    onTakeAnotherTest: () -> Unit
) {
    val isHindi by viewModel.isBilingualHindi.collectAsState()
    val result by viewModel.latestMockResult.collectAsState()
    val mockQuestions by viewModel.activeMockQuestions.collectAsState()

    if (result == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = onBackHome) {
                Text(if (isHindi) "होम पर जाएं" else "Back to Home")
            }
        }
        return
    }

    val res = result!!

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("mock_test_result_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Score Card Hero Banner ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PoliceNavyPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                if (res.isPassed) listOf(PoliceNavyDark, PoliceNavyPrimary, Color(0xFF1B5E20))
                                else listOf(PoliceNavyDark, PoliceNavyPrimary, PoliceRedTertiary)
                            )
                        )
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (res.isPassed) PoliceGoldLight else Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (res.isPassed) Icons.Default.EmojiEvents else Icons.Default.Refresh,
                                contentDescription = null,
                                tint = if (res.isPassed) PoliceNavyDark else Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = res.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "%.1f / %.1f".format(res.score, res.maxScore),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 36.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (res.isPassed) CorrectGreen else IncorrectRed
                        ) {
                            Text(
                                text = if (res.isPassed) (if (isHindi) "⭐⭐ कट-ऑफ उत्तीर्ण (QUALIFIED) ⭐⭐" else "QUALIFIED")
                                else (if (isHindi) "पुनः अभ्यास करें (NOT QUALIFIED)" else "NEEDS PRACTICE"),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = res.remarks,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }

        // --- Metrics Breakdown Row ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${res.correctCount}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = CorrectGreen)
                        )
                        Text(text = if (isHindi) "सही उत्तर" else "Correct", style = MaterialTheme.typography.labelSmall)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${res.incorrectCount}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = IncorrectRed)
                        )
                        Text(text = if (isHindi) "गलत उत्तर" else "Incorrect", style = MaterialTheme.typography.labelSmall)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${res.unattemptedCount}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(text = if (isHindi) "छोड़े गए" else "Skipped", style = MaterialTheme.typography.labelSmall)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%.0f%%".format(res.accuracyPercent),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (res.accuracyPercent >= 60f) CorrectGreen else PoliceRedTertiary
                            )
                        )
                        Text(text = if (isHindi) "सटीकता" else "Accuracy", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // --- Action Buttons ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onBackHome,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isHindi) "होम" else "Home")
                }

                Button(
                    onClick = onTakeAnotherTest,
                    colors = ButtonDefaults.buttonColors(containerColor = PoliceNavyPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isHindi) "अन्य टेस्ट दें" else "More Tests")
                }
            }
        }

        // --- Question by Question Review Header ---
        item {
            Text(
                text = if (isHindi) "प्रश्नोत्तर विश्लेषण व व्याख्या" else "Detailed Solutions Review",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // --- Questions List ---
        itemsIndexed(mockQuestions) { index, item ->
            val q = item.question
            val userAns = item.selectedOption
            val isCorrect = userAns.equals(q.correctOption, ignoreCase = true)
            val isSkipped = userAns == null

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "प्रश्न ${index + 1}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when {
                                isSkipped -> Color(0xFFEEEEEE)
                                isCorrect -> Color(0xFFE8F5E9)
                                else -> Color(0xFFFFEBEE)
                            }
                        ) {
                            Text(
                                text = when {
                                    isSkipped -> if (isHindi) "अनुत्तरित" else "Skipped"
                                    isCorrect -> if (isHindi) "सही (+2.5)" else "Correct"
                                    else -> if (isHindi) "गलत" else "Incorrect"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isSkipped -> Color.Gray
                                        isCorrect -> CorrectGreen
                                        else -> IncorrectRed
                                    }
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isHindi) q.questionHindi else q.questionEnglish,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row {
                        Text(
                            text = "आपका उत्तर: ${userAns ?: '—'}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isCorrect) CorrectGreen else if (isSkipped) Color.Gray else IncorrectRed
                            )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "सही उत्तर: ${q.correctOption}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = CorrectGreen
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "व्याख्या: ${q.explanation}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
