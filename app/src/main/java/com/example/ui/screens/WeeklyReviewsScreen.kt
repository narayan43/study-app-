package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.model.WeeklyReviewEntity
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.IncorrectRed
import com.example.ui.theme.PoliceGoldDark
import com.example.ui.theme.PoliceGoldLight
import com.example.ui.theme.PoliceNavyDark
import com.example.ui.theme.PoliceNavyPrimary
import com.example.ui.theme.PoliceRedTertiary
import com.example.ui.viewmodel.UPSIViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun WeeklyReviewsScreen(
    viewModel: UPSIViewModel
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isHindi by viewModel.isBilingualHindi.collectAsState()
    val weeklyReviews by viewModel.weeklyReviews.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var reviewToDelete by remember { mutableStateOf<WeeklyReviewEntity?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("weekly_reviews_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PoliceGoldLight,
                contentColor = PoliceNavyDark,
                modifier = Modifier.testTag("fab_add_weekly_review")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Weekly Review")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Action Row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isHindi) "साप्ताहिक समीक्षा डायरी" else "Weekly Review Journal",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "weekly_reviews.csv • ${weeklyReviews.size} प्रविष्टियां",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Button(
                        onClick = { showExportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PoliceNavyPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_export_reviews_csv")
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isHindi) "CSV एक्सपोर्ट" else "CSV Export")
                    }
                }
            }

            if (weeklyReviews.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (isHindi) "कोई साप्ताहिक समीक्षा दर्ज नहीं है" else "No weekly reviews recorded yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isHindi) "हर सप्ताह के अंत में अपनी पढ़ाई का विश्लेषण करें और नई योजना बनाएं।" else "Reflect on what you learned, what to improve, and what you're proud of.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PoliceNavyPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isHindi) "समीक्षा लिखें" else "Write First Review")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(weeklyReviews) { review ->
                        WeeklyReviewCard(
                            review = review,
                            isHindi = isHindi,
                            onDelete = { reviewToDelete = review }
                        )
                    }
                }
            }
        }
    }

    // --- Add Review Dialog ---
    if (showAddDialog) {
        AddWeeklyReviewDialog(
            isHindi = isHindi,
            onDismiss = { showAddDialog = false },
            onSave = { weekStart, learned, willChange, proudOf ->
                viewModel.addOrUpdateWeeklyReview(
                    weekStart = weekStart,
                    learned = learned,
                    willChange = willChange,
                    proudOf = proudOf
                )
                showAddDialog = false
                Toast.makeText(context, "साप्ताहिक समीक्षा सहेजी गई!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // --- Export CSV Dialog ---
    if (showExportDialog) {
        val csvData = viewModel.getWeeklyReviewsCsvExport()
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("weekly_reviews.csv", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = if (isHindi) "weekly_reviews.csv का डेटा नीचे दिया गया है:" else "Your weekly_reviews.csv content:",
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
                        Toast.makeText(context, "weekly_reviews.csv कॉपी हो गया!", Toast.LENGTH_SHORT).show()
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

    // --- Delete Review Dialog ---
    if (reviewToDelete != null) {
        AlertDialog(
            onDismissRequest = { reviewToDelete = null },
            title = { Text(if (isHindi) "समीक्षा हटाएं?" else "Delete Review?", fontWeight = FontWeight.Bold) },
            text = { Text(if (isHindi) "क्या आप निश्चित रूप से इस सप्ताह की समीक्षा हटाना चाहते हैं?" else "Are you sure you want to delete this review?") },
            confirmButton = {
                Button(
                    onClick = {
                        reviewToDelete?.let { viewModel.deleteWeeklyReview(it) }
                        reviewToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IncorrectRed)
                ) {
                    Text(if (isHindi) "हटाएं" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { reviewToDelete = null }) {
                    Text(if (isHindi) "रद्द करें" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun WeeklyReviewCard(
    review: WeeklyReviewEntity,
    isHindi: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Week Start & Written On dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PoliceNavyPrimary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "सप्ताह: ${review.weekStart}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PoliceNavyPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "लिखित: ${review.writtenOn}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Learned Section
            ReviewFieldRow(
                icon = Icons.Default.Lightbulb,
                iconColor = PoliceGoldDark,
                label = if (isHindi) "क्या सीखा (Learned)" else "Learned",
                text = review.learned
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Will Change Section
            ReviewFieldRow(
                icon = Icons.Default.TrendingUp,
                iconColor = Color(0xFF1976D2),
                label = if (isHindi) "क्या सुधारेंगे (Will Change)" else "Will Change",
                text = review.willChange
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Proud Of Section
            ReviewFieldRow(
                icon = Icons.Default.EmojiEvents,
                iconColor = CorrectGreen,
                label = if (isHindi) "किस बात पर गर्व है (Proud Of)" else "Proud Of",
                text = review.proudOf
            )
        }
    }
}

@Composable
private fun ReviewFieldRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    text: String
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = iconColor)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp)
            )
        }
    }
}

@Composable
private fun AddWeeklyReviewDialog(
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onSave: (weekStart: String, learned: String, willChange: String, proudOf: String) -> Unit
) {
    // Current Monday as default week start
    val defaultWeekStart = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    var weekStart by remember { mutableStateOf(defaultWeekStart) }
    var learned by remember { mutableStateOf("") }
    var willChange by remember { mutableStateOf("") }
    var proudOf by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isHindi) "नयी साप्ताहिक समीक्षा (Weekly Review)" else "New Weekly Review",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = weekStart,
                        onValueChange = { weekStart = it },
                        label = { Text(if (isHindi) "सप्ताह की शुरुआत (YYYY-MM-DD)" else "Week Start Date") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = learned,
                        onValueChange = { learned = it },
                        label = { Text(if (isHindi) "इस सप्ताह क्या सीखा? (Learned)" else "What did you learn this week?") },
                        placeholder = { Text(if (isHindi) "जैसे: IPC धाराएं 302, 304B, हिन्दी अलंकार, प्रतिशत सूत्र..." else "E.g. IPC sections, Alankar, Math formulas...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }

                item {
                    OutlinedTextField(
                        value = willChange,
                        onValueChange = { willChange = it },
                        label = { Text(if (isHindi) "अगले सप्ताह क्या बदलाव करेंगे? (Will Change)" else "What will you change next week?") },
                        placeholder = { Text(if (isHindi) "जैसे: गणित में स्पीड बढ़ाना, रोज़ाना 50 प्रश्न हल करना..." else "E.g. Increase math speed, solve 50 Qs daily...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }

                item {
                    OutlinedTextField(
                        value = proudOf,
                        onValueChange = { proudOf = it },
                        label = { Text(if (isHindi) "किस उपलब्धि पर गर्व है? (Proud Of)" else "What are you proud of?") },
                        placeholder = { Text(if (isHindi) "जैसे: मॉक टेस्ट में 80% अंक प्राप्त किए, लगातार 7 दिन पढ़ाई..." else "E.g. Scored 80% in mock, 7-day study streak...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (learned.isNotBlank() && willChange.isNotBlank() && proudOf.isNotBlank()) {
                        onSave(weekStart, learned, willChange, proudOf)
                    }
                },
                enabled = learned.isNotBlank() && willChange.isNotBlank() && proudOf.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PoliceNavyPrimary),
                modifier = Modifier.testTag("btn_save_weekly_review")
            ) {
                Text(if (isHindi) "सहेजें" else "Save Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isHindi) "रद्द करें" else "Cancel")
            }
        }
    )
}
