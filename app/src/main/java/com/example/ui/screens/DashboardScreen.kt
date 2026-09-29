package com.example.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subject
import com.example.ui.components.AppTab
import com.example.ui.components.StatCard
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.PoliceGoldDark
import com.example.ui.theme.PoliceGoldLight
import com.example.ui.theme.PoliceGoldSecondary
import com.example.ui.theme.PoliceNavyDark
import com.example.ui.theme.PoliceNavyLight
import com.example.ui.theme.PoliceNavyPrimary
import com.example.ui.theme.PoliceRedTertiary
import com.example.ui.viewmodel.UPSIViewModel

@Composable
fun DashboardScreen(
    viewModel: UPSIViewModel,
    onNavigateTab: (AppTab) -> Unit,
    onStartSubjectPractice: (String) -> Unit,
    onOpenLawHandbook: () -> Unit
) {
    val isHindi by viewModel.isBilingualHindi.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val attempts by viewModel.attempts.collectAsState()
    val reviews by viewModel.weeklyReviews.collectAsState()
    val incorrectIds by viewModel.incorrectQuestionIds.collectAsState()

    val totalQuestionsCount = questions.size
    val totalAttemptsCount = attempts.size
    val correctAttemptsCount = attempts.count { it.isCorrect }
    val overallAccuracy = if (totalAttemptsCount > 0) {
        (correctAttemptsCount.toFloat() / totalAttemptsCount) * 100f
    } else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- UP Police SI Mission Hero Card ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_mission_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PoliceNavyPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(PoliceNavyDark, PoliceNavyPrimary, PoliceNavyLight)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PoliceGoldSecondary.copy(alpha = 0.25f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = PoliceGoldLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isHindi) "मिशन 2 सितारे 2026" else "Mission 2 Stars 2026",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = PoliceGoldLight,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isHindi) "उत्तर प्रदेश पुलिस उप-निरीक्षक" else "UP Police Sub-Inspector",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = if (isHindi) "वर्दी का सपना होगा सच, अनुशासन और निरंतर अभ्यास से।" else "Make your dream of the khaki uniform come true with consistent practice.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onNavigateTab(AppTab.PRACTICE) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PoliceGoldLight,
                                    contentColor = PoliceNavyDark
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_practice_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isHindi) "अभ्यास शुरू करें" else "Start Practice",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Button(
                                onClick = { onNavigateTab(AppTab.MOCK_TESTS) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.15f),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hero_mock_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isHindi) "मॉक टेस्ट दें" else "Take Mock",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Quick Stats Row ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = if (isHindi) "प्रयास प्रश्न" else "Attempts",
                    value = "$totalAttemptsCount",
                    icon = Icons.Default.AutoGraph,
                    accentColor = PoliceNavyPrimary,
                    subtitle = if (isHindi) "कुल प्रश्न: $totalQuestionsCount" else "Bank: $totalQuestionsCount",
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = if (isHindi) "सटीकता दर" else "Accuracy",
                    value = "%.0f%%".format(overallAccuracy),
                    icon = Icons.Default.CheckCircle,
                    accentColor = if (overallAccuracy >= 60f) CorrectGreen else PoliceRedTertiary,
                    subtitle = if (isHindi) "सही: $correctAttemptsCount" else "Correct: $correctAttemptsCount",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // --- Subject Sections Grid ---
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isHindi) "विषयवार तैयारी (UPSI 4 खंड)" else "Subjects (4 Sections)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = if (isHindi) "सभी देखें" else "View All",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = PoliceNavyPrimary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.clickable { onNavigateTab(AppTab.PRACTICE) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                SubjectGrid(
                    isHindi = isHindi,
                    onSubjectClick = { onStartSubjectPractice(it) }
                )
            }
        }

        // --- Multi-Modal Study Resources (Notes & Videos) ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTab(AppTab.NOTES) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PoliceNavyPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = PoliceNavyPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isHindi) "स्टडी नोट्स" else "Study Notes",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isHindi) "IPC, CrPC व संविधान" else "Revision summaries",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTab(AppTab.VIDEOS) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PoliceGoldSecondary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = null,
                                tint = PoliceGoldDark,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isHindi) "वीडियो क्लासेस" else "Video Classes",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isHindi) "कांसेप्ट मास्टरक्लास" else "Concept lectures",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // --- Quick Revision & Weekly Review Highlights ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenLawHandbook() },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(PoliceRedTertiary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = PoliceRedTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isHindi) "मूल विधि एवं संविधान पॉकेट गाइड" else "Mool Vidhi & Constitution Guide",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isHindi) "आईपीसी धाराएं, सीआरपीसी, महिला व बाल कानून, मौलिक अधिकार" else "IPC Sections 302, 304B, 498A, CrPC 41, 154, Articles",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // --- Weak Topics / Mistake Notebook Button (if errors exist) ---
        if (incorrectIds.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateTab(AppTab.ANALYTICS) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) "गलती सुधार डायरी (${incorrectIds.size} प्रश्न गलत हुए)" else "Mistakes Notebook (${incorrectIds.size} errors)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFBF360C)
                                )
                            )
                            Text(
                                text = if (isHindi) "इन प्रश्नों को दोबारा हल करके अपनी कमजोरी को ताकत बनाएं" else "Revise and re-attempt to strengthen your weak spots",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF795548),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // --- Weekly Reflection Banner (weekly_reviews.csv) ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateTab(AppTab.WEEKLY_REVIEWS) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(PoliceGoldLight.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = null,
                            tint = PoliceGoldDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isHindi) "साप्ताहिक समीक्षा डायरी (Weekly Reviews)" else "Weekly Study Reflection",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isHindi) "इस सप्ताह क्या सीखा? क्या सुधारेंगे? किस बात पर गर्व है?" else "Track what you learned, will change, and are proud of.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectGrid(
    isHindi: Boolean,
    onSubjectClick: (String) -> Unit
) {
    val subjects = listOf(
        SubjectItem(
            name = "General Hindi",
            hindiName = "सामान्य हिन्दी",
            subtitle = "40 प्रश्न • 100 अंक",
            icon = Icons.Default.Translate,
            accentColor = Color(0xFF1976D2)
        ),
        SubjectItem(
            name = "Law & Constitution",
            hindiName = "मूल विधि व संविधान",
            subtitle = "40 प्रश्न • 100 अंक",
            icon = Icons.Default.Gavel,
            accentColor = Color(0xFFC2185B)
        ),
        SubjectItem(
            name = "Numerical & Mental Ability",
            hindiName = "संख्यात्मक योग्यता (गणित)",
            subtitle = "40 प्रश्न • 100 अंक",
            icon = Icons.Default.Calculate,
            accentColor = Color(0xFF388E3C)
        ),
        SubjectItem(
            name = "Mental Aptitude & Reasoning",
            hindiName = "मानसिक अभिरुचि व रीजनिंग",
            subtitle = "40 प्रश्न • 100 अंक",
            icon = Icons.Default.Psychology,
            accentColor = Color(0xFFF57C00)
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SubjectCard(item = subjects[0], isHindi = isHindi, onClick = { onSubjectClick(subjects[0].name) }, modifier = Modifier.weight(1f))
            SubjectCard(item = subjects[1], isHindi = isHindi, onClick = { onSubjectClick(subjects[1].name) }, modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SubjectCard(item = subjects[2], isHindi = isHindi, onClick = { onSubjectClick(subjects[2].name) }, modifier = Modifier.weight(1f))
            SubjectCard(item = subjects[3], isHindi = isHindi, onClick = { onSubjectClick(subjects[3].name) }, modifier = Modifier.weight(1f))
        }
    }
}

private data class SubjectItem(
    val name: String,
    val hindiName: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
private fun SubjectCard(
    item: SubjectItem,
    isHindi: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("subject_card_${item.name.replace(" ", "_")}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(item.accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isHindi) item.hindiName else item.name,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            )
        }
    }
}
