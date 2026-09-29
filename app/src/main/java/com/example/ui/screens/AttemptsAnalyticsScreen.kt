package com.example.ui.screens

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttemptEntity
import com.example.data.model.QuestionEntity
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
fun AttemptsAnalyticsScreen(
    viewModel: UPSIViewModel,
    onPracticeQuestion: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isHindi by viewModel.isBilingualHindi.collectAsState()
    val attempts by viewModel.attempts.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val incorrectIds by viewModel.incorrectQuestionIds.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showExportDialog by remember { mutableStateOf(false) }

    val totalAttempts = attempts.size
    val correctAttempts = attempts.count { it.isCorrect }
    val incorrectAttempts = totalAttempts - correctAttempts
    val overallAccuracy = if (totalAttempts > 0) (correctAttempts.toFloat() / totalAttempts) * 100f else 0f

    val qMap = remember(questions) { questions.associateBy { it.id } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("attempts_analytics_screen")
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PoliceNavyPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(if (isHindi) "प्रदर्शन विश्लेषण" else "Overview", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_analytics_overview")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(if (isHindi) "गलतियां (${incorrectIds.size})" else "Mistakes (${incorrectIds.size})", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_analytics_mistakes")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text(if (isHindi) "प्रयास सूची" else "Attempts CSV", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_analytics_raw")
            )
        }

        when (selectedTab) {
            0 -> OverviewTab(
                totalAttempts = totalAttempts,
                correctAttempts = correctAttempts,
                incorrectAttempts = incorrectAttempts,
                overallAccuracy = overallAccuracy,
                isHindi = isHindi,
                viewModel = viewModel,
                onExportCsv = { showExportDialog = true }
            )
            1 -> MistakesNotebookTab(
                incorrectIds = incorrectIds,
                qMap = qMap,
                isHindi = isHindi,
                onReAttempt = { qId -> onPracticeQuestion(qId) }
            )
            2 -> RawAttemptsTab(
                attempts = attempts,
                qMap = qMap,
                isHindi = isHindi,
                onExportCsv = { showExportDialog = true }
            )
        }
    }

    if (showExportDialog) {
        val csvData = viewModel.getAttemptsCsvExport()
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("attempts.csv Export", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = if (isHindi) "attempts.csv फाइल का डेटा तैयार है। आप इसे कॉपी कर सकते हैं।" else "Your attempts.csv is ready to export/copy:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        LazyColumn(modifier = Modifier.padding(8.dp)) {
                            item {
                                Text(
                                    text = csvData,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, lineHeight = 14.sp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(csvData))
                        Toast.makeText(context, "attempts.csv क्लिपबोर्ड पर कॉपी हो गया!", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PoliceNavyPrimary)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isHindi) "कॉपी करें" else "Copy CSV")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text(if (isHindi) "बंद करें" else "Close")
                }
            }
        )
    }
}

@Composable
private fun OverviewTab(
    totalAttempts: Int,
    correctAttempts: Int,
    incorrectAttempts: Int,
    overallAccuracy: Float,
    isHindi: Boolean,
    viewModel: UPSIViewModel,
    onExportCsv: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // High Level Metrics
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        Text(
                            text = if (isHindi) "समग्र सटीकता (Overall Accuracy)" else "Overall Accuracy",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "%.1f%%".format(overallAccuracy),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (overallAccuracy >= 60f) CorrectGreen else PoliceRedTertiary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { (overallAccuracy / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (overallAccuracy >= 60f) CorrectGreen else PoliceRedTertiary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "$totalAttempts", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(text = if (isHindi) "कुल प्रयास" else "Total Attempts", style = MaterialTheme.typography.labelSmall)
                        }
                        Column {
                            Text(text = "$correctAttempts", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = CorrectGreen))
                            Text(text = if (isHindi) "सही हल" else "Correct", style = MaterialTheme.typography.labelSmall)
                        }
                        Column {
                            Text(text = "$incorrectAttempts", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = IncorrectRed))
                            Text(text = if (isHindi) "गलत हल" else "Incorrect", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Section Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isHindi) "विषयवार पकड़ (Subject Mastery)" else "Subject Breakdown",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SubjectMasteryBar(
                        title = if (isHindi) "सामान्य हिन्दी" else "General Hindi",
                        accuracy = viewModel.getAccuracyBySubject("General Hindi"),
                        color = Color(0xFF1976D2)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SubjectMasteryBar(
                        title = if (isHindi) "मूल विधि व संविधान" else "Law & Constitution",
                        accuracy = viewModel.getAccuracyBySubject("Law & Constitution"),
                        color = Color(0xFFC2185B)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SubjectMasteryBar(
                        title = if (isHindi) "संख्यात्मक योग्यता (Maths)" else "Numerical Ability",
                        accuracy = viewModel.getAccuracyBySubject("Numerical & Mental Ability"),
                        color = Color(0xFF388E3C)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SubjectMasteryBar(
                        title = if (isHindi) "मानसिक अभिरुचि व रीजनिंग" else "Mental Aptitude & Reasoning",
                        accuracy = viewModel.getAccuracyBySubject("Mental Aptitude & Reasoning"),
                        color = Color(0xFFF57C00)
                    )
                }
            }
        }

        // Export attempts.csv card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "attempts.csv",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isHindi) "सभी $totalAttempts प्रयासों का डेटा एक्सपोर्ट करें" else "Export all attempt data matching csv schema",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                        )
                    }

                    Button(
                        onClick = onExportCsv,
                        colors = ButtonDefaults.buttonColors(containerColor = PoliceNavyPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isHindi) "एक्सपोर्ट" else "Export")
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectMasteryBar(title: String, accuracy: Float, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
            Text(text = "%.0f%%".format(accuracy), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = color))
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (accuracy / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun MistakesNotebookTab(
    incorrectIds: List<String>,
    qMap: Map<String, QuestionEntity>,
    isHindi: Boolean,
    onReAttempt: (String) -> Unit
) {
    if (incorrectIds.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = CorrectGreen,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isHindi) "शानदार! कोई गलत प्रश्न नहीं है।" else "Great! No incorrect questions recorded.",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isHindi) "जब भी अभ्यास या टेस्ट में कोई प्रश्न गलत होगा, वह यहाँ दिखेगा।" else "Incorrect attempts in tests or practice will appear here for revision.",
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
            items(incorrectIds) { qId ->
                val q = qMap[qId]
                if (q != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SectionBadge(subject = q.subject)
                                Text(
                                    text = q.topic,
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (isHindi) q.questionHindi else q.questionEnglish,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "सही विकल्प: ${q.correctOption}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = CorrectGreen)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { onReAttempt(q.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = PoliceGoldLight, contentColor = PoliceNavyDark),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(imageVector = Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isHindi) "पुनः अभ्यास करें" else "Re-Attempt", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RawAttemptsTab(
    attempts: List<AttemptEntity>,
    qMap: Map<String, QuestionEntity>,
    isHindi: Boolean,
    onExportCsv: () -> Unit
) {
    if (attempts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(if (isHindi) "अभी तक कोई प्रयास रिकॉर्ड नहीं हुआ" else "No attempts recorded yet")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isHindi) "हाल के प्रयास (${attempts.size})" else "Recent Attempts (${attempts.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Button(
                        onClick = onExportCsv,
                        colors = ButtonDefaults.buttonColors(containerColor = PoliceNavyPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isHindi) "CSV एक्सपोर्ट" else "CSV Export")
                    }
                }
            }

            items(attempts.take(50)) { att ->
                val q = qMap[att.questionId]
                val timeStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(att.attemptedAt))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (att.isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (att.isCorrect) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (att.isCorrect) CorrectGreen else IncorrectRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = q?.questionHindi ?: "प्रश्न ID: ${att.questionId}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                maxLines = 1
                            )
                            Row {
                                Text(
                                    text = "उत्तर: ${att.chosenOption} • ${att.mode}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = timeStr,
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
