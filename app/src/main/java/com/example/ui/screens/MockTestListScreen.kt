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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.data.model.MockTestConfig
import com.example.data.model.MockTestResultEntity
import com.example.ui.components.SectionBadge
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.IncorrectRed
import com.example.ui.theme.PoliceGoldDark
import com.example.ui.theme.PoliceGoldLight
import com.example.ui.theme.PoliceNavyDark
import com.example.ui.theme.PoliceNavyPrimary
import com.example.ui.theme.PoliceRedTertiary
import com.example.ui.viewmodel.UPSIViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MockTestListScreen(
    viewModel: UPSIViewModel,
    onStartTest: (MockTestConfig) -> Unit
) {
    val isHindi by viewModel.isBilingualHindi.collectAsState()
    val pastResults by viewModel.mockTestResults.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val testConfigs = listOf(
        MockTestConfig(
            id = "FULL_MOCK_1",
            title = if (isHindi) "UPSI सम्पूर्ण परीक्षा मॉक टेस्ट - 01" else "UPSI Full Mock Test - 01",
            description = if (isHindi) "चारों विषय (हिन्दी, मूल विधि, गणित, रीजनिंग) का वास्तविक परीक्षा पैटर्न" else "All 4 subjects complete official exam simulation",
            questionCount = 15,
            durationMinutes = 20,
            totalMarks = 37.5f
        ),
        MockTestConfig(
            id = "SEC_LAW",
            title = if (isHindi) "मूल विधि एवं संविधान सेक्शनल टेस्ट" else "Mool Vidhi & Constitution Test",
            description = if (isHindi) "IPC धाराएं, CrPC, महिला व बाल अपराध कानून, संवैधानिक अनुच्छेद" else "IPC Sections, CrPC, Special Acts, Constitutional Articles",
            subject = "Law & Constitution",
            questionCount = 10,
            durationMinutes = 12,
            totalMarks = 25f
        ),
        MockTestConfig(
            id = "SEC_HINDI",
            title = if (isHindi) "सामान्य हिन्दी स्पीड टेस्ट" else "General Hindi Speed Test",
            description = if (isHindi) "अलंकार, समास, संधि, मुहावरे, साहित्य और व्याकरण" else "Alankar, Samas, Sandhi, Idioms, Literature",
            subject = "General Hindi",
            questionCount = 10,
            durationMinutes = 10,
            totalMarks = 25f
        ),
        MockTestConfig(
            id = "SEC_NUM",
            title = if (isHindi) "संख्यात्मक योग्यता (गणित) टेस्ट" else "Numerical Ability (Maths) Test",
            description = if (isHindi) "प्रतिशत, लाभ-हानि, समय-कार्य, साधारण व चक्रवृद्धि ब्याज" else "Percentage, Profit/Loss, Time & Work, Interest",
            subject = "Numerical & Mental Ability",
            questionCount = 8,
            durationMinutes = 15,
            totalMarks = 20f
        ),
        MockTestConfig(
            id = "SEC_REA",
            title = if (isHindi) "मानसिक अभिरुचि व रीजनिंग टेस्ट" else "Mental Aptitude & Reasoning Test",
            description = if (isHindi) "पुलिस दृष्टिकोण, दिशा परीक्षण, रक्त संबंध, कोडिंग-डिकोडिंग" else "Police aptitude, direction test, blood relations",
            subject = "Mental Aptitude & Reasoning",
            questionCount = 8,
            durationMinutes = 12,
            totalMarks = 20f
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("mock_test_list_screen")
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PoliceNavyPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(if (isHindi) "उपलब्ध मॉक टेस्ट" else "Available Tests", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_available_tests")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(if (isHindi) "पिछले परिणाम (${pastResults.size})" else "History (${pastResults.size})", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_test_history")
            )
        }

        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Info banner on UPSI Cutoff Rule
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EEF8))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = PoliceNavyPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isHindi) "UPSI कट-ऑफ नियम: प्रत्येक विषय में न्यूनतम 35% तथा कुल 50% अंक प्राप्त करना अनिवार्य है।" else "UPSI Cutoff Rule: Minimum 35% in each section and 50% overall required to qualify.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = PoliceNavyDark,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                items(testConfigs) { config ->
                    MockTestCard(
                        config = config,
                        isHindi = isHindi,
                        onStart = { onStartTest(config) }
                    )
                }
            }
        } else {
            if (pastResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isHindi) "अभी तक कोई मॉक टेस्ट नहीं दिया" else "No mock tests attempted yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isHindi) "अपनी तैयारी परखने के लिए पहला टेस्ट शुरू करें।" else "Take a test to test your speed and accuracy.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pastResults) { result ->
                        PastResultCard(result = result, isHindi = isHindi)
                    }
                }
            }
        }
    }
}

@Composable
private fun MockTestCard(
    config: MockTestConfig,
    isHindi: Boolean,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mock_card_${config.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (config.subject != null) {
                    SectionBadge(subject = config.subject)
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PoliceGoldLight.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "FULL MOCK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PoliceGoldDark
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${config.durationMinutes} min",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = config.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = config.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(
                            text = "${config.questionCount}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isHindi) "प्रश्न" else "Questions",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    Column {
                        Text(
                            text = "%.1f".format(config.totalMarks),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isHindi) "पूर्णांक" else "Marks",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(containerColor = PoliceNavyPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("start_test_btn_${config.id}")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isHindi) "टेस्ट दें" else "Start")
                }
            }
        }
    }
}

@Composable
private fun PastResultCard(
    result: MockTestResultEntity,
    isHindi: Boolean
) {
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(result.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (result.isPassed) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ) {
                    Text(
                        text = if (result.isPassed) (if (isHindi) "उत्तीर्ण (PASSED)" else "PASSED")
                        else (if (isHindi) "असफल (FAILED)" else "FAILED"),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (result.isPassed) CorrectGreen else IncorrectRed
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "%.1f / %.1f".format(result.score, result.maxScore),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (isHindi) "प्राप्तांक" else "Score",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Column {
                    Text(
                        text = "%.0f%%".format(result.accuracyPercent),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (result.accuracyPercent >= 60f) CorrectGreen else PoliceRedTertiary
                        )
                    )
                    Text(
                        text = if (isHindi) "सटीकता" else "Accuracy",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Column {
                    Text(
                        text = "${result.correctCount}/${result.totalQuestions}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (isHindi) "सही / कुल" else "Correct",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = result.remarks,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
