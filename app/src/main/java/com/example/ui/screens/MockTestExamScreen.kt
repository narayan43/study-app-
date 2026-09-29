package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.UPSIViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockTestExamScreen(
    viewModel: UPSIViewModel,
    onTestFinished: () -> Unit,
    onCancelTest: () -> Unit
) {
    val isHindi by viewModel.isBilingualHindi.collectAsState()
    val activeConfig by viewModel.activeMockConfig.collectAsState()
    val mockQuestions by viewModel.activeMockQuestions.collectAsState()
    val currentIndex by viewModel.currentMockIndex.collectAsState()
    val timeRemaining by viewModel.mockTimeRemainingSeconds.collectAsState()

    var showSubmitDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showPaletteSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Handle back button safely
    BackHandler {
        showExitDialog = true
    }

    if (activeConfig == null || mockQuestions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("लोड हो रहा है...")
        }
        return
    }

    val currentItem = mockQuestions.getOrNull(currentIndex) ?: return
    val currentQ = currentItem.question

    val minutes = timeRemaining / 60
    val seconds = timeRemaining % 60
    val isLowTime = timeRemaining <= 120 // < 2 minutes

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("mock_test_exam_screen")
    ) {
        // --- Exam Header Bar ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = PoliceNavyPrimary,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { showExitDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Exit", tint = Color.White)
                    }

                    // Countdown Timer Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isLowTime) IncorrectRed else Color.White.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Timer",
                                tint = if (isLowTime) Color.White else PoliceGoldLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = String.format("%02d:%02d", minutes, seconds),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }

                    // Question Palette Toggle & Submit
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showPaletteSheet = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_open_palette")
                        ) {
                            Icon(imageVector = Icons.Default.GridView, contentDescription = "Question Palette", tint = Color.White)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Button(
                            onClick = { showSubmitDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PoliceGoldLight),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_submit_exam")
                        ) {
                            Text(
                                text = if (isHindi) "जमा करें" else "Submit",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PoliceNavyDark
                                )
                            )
                        }
                    }
                }
            }
        }

        // --- Active Question Content ---
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Question Sub-Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "प्रश्न ${currentIndex + 1} / ${mockQuestions.size}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        SectionBadge(subject = currentQ.subject)
                    }

                    // Flag / Mark for Review Button
                    IconButton(
                        onClick = { viewModel.toggleFlagCurrentMockQuestion() },
                        modifier = Modifier.testTag("btn_flag_question")
                    ) {
                        Icon(
                            imageVector = if (currentItem.isFlagged) Icons.Default.Flag else Icons.Outlined.Flag,
                            contentDescription = "Flag",
                            tint = if (currentItem.isFlagged) WarningOrange else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Question Text Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = currentQ.topic,
                            style = MaterialTheme.typography.labelSmall.copy(color = PoliceGoldDark, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isHindi) currentQ.questionHindi else currentQ.questionEnglish,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                lineHeight = 24.sp
                            )
                        )
                    }
                }
            }

            // Options
            items(
                listOf(
                    "A" to currentQ.optionA,
                    "B" to currentQ.optionB,
                    "C" to currentQ.optionC,
                    "D" to currentQ.optionD
                )
            ) { (key, optText) ->
                val isSelected = currentItem.selectedOption == key

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectMockOption(key) }
                        .testTag("mock_option_$key"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) PoliceNavyPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) PoliceNavyPrimary else MaterialTheme.colorScheme.outlineVariant)
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
                                .background(if (isSelected) PoliceNavyPrimary else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = optText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // --- Bottom Prev / Next Nav ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.goToMockQuestion(currentIndex - 1) },
                    enabled = currentIndex > 0,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("exam_prev_button")
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isHindi) "पिछला" else "Previous")
                }

                Surface(
                    onClick = { showPaletteSheet = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${currentIndex + 1} / ${mockQuestions.size}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Button(
                    onClick = {
                        if (currentIndex < mockQuestions.size - 1) {
                            viewModel.goToMockQuestion(currentIndex + 1)
                        } else {
                            showSubmitDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PoliceNavyPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("exam_next_button")
                ) {
                    Text(if (currentIndex == mockQuestions.size - 1) (if (isHindi) "समाप्त" else "Finish") else (if (isHindi) "अगला" else "Next"))
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    // --- Question Palette Bottom Sheet ---
    if (showPaletteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPaletteSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (isHindi) "प्रश्न तालिका (Question Palette)" else "Question Palette",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PaletteLegendItem(color = CorrectGreen, label = if (isHindi) "उत्तर दिया" else "Answered")
                    PaletteLegendItem(color = WarningOrange, label = if (isHindi) "समीक्षा हेतु" else "Flagged")
                    PaletteLegendItem(color = Color.LightGray, label = if (isHindi) "शेष प्रश्न" else "Unvisited")
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(mockQuestions.size) { idx ->
                        val item = mockQuestions[idx]
                        val isAnswered = item.selectedOption != null
                        val isFlagged = item.isFlagged
                        val isCurrent = idx == currentIndex

                        val bg = when {
                            isFlagged -> WarningOrange
                            isAnswered -> CorrectGreen
                            item.isVisited -> Color(0xFFB0BEC5)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bg)
                                .clickable {
                                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                                        showPaletteSheet = false
                                        viewModel.goToMockQuestion(idx)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${idx + 1}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold,
                                    color = if (isAnswered || isFlagged) Color.White else Color.Black
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // --- Submit Confirmation Dialog ---
    if (showSubmitDialog) {
        val answeredCount = mockQuestions.count { it.selectedOption != null }
        val unansweredCount = mockQuestions.size - answeredCount
        val flaggedCount = mockQuestions.count { it.isFlagged }

        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = {
                Text(
                    text = if (isHindi) "टेस्ट जमा (Submit) करें?" else "Submit Mock Test?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isHindi) "क्या आप निश्चित रूप से अपना टेस्ट सबमिट करना चाहते हैं?" else "Are you sure you want to finish and submit?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("उत्तर दिए (Answered):")
                        Text("$answeredCount", fontWeight = FontWeight.Bold, color = CorrectGreen)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("अनुत्तरित (Unanswered):")
                        Text("$unansweredCount", fontWeight = FontWeight.Bold, color = IncorrectRed)
                    }
                    if (flaggedCount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("समीक्षा हेतु चिह्नित:")
                            Text("$flaggedCount", fontWeight = FontWeight.Bold, color = WarningOrange)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        viewModel.submitMockTest()
                        onTestFinished()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PoliceNavyPrimary),
                    modifier = Modifier.testTag("confirm_submit_exam_button")
                ) {
                    Text(if (isHindi) "हाँ, जमा करें" else "Yes, Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text(if (isHindi) "रद्द करें" else "Cancel")
                }
            }
        )
    }

    // --- Exit Confirmation Dialog ---
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(if (isHindi) "टेस्ट बीच में छोड़ें?" else "Exit Test?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (isHindi) "यदि आप बाहर निकलते हैं, तो आपका वर्तमान टेस्ट रद्द हो जाएगा।"
                    else "Exiting now will cancel this test session."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        onCancelTest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IncorrectRed)
                ) {
                    Text(if (isHindi) "हाँ, बाहर निकलें" else "Exit Test")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(if (isHindi) "टेस्ट जारी रखें" else "Continue Test")
                }
            }
        )
    }
}

@Composable
private fun PaletteLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
    }
}
