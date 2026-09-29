package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NoteItem
import com.example.data.model.QuestionItem
import com.example.data.model.TestSliceSource
import com.example.ui.viewmodel.ExamPrepViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestPlayerScreen(
    viewModel: ExamPrepViewModel,
    onBack: () -> Unit,
    onOpenNoteReader: (NoteItem) -> Unit
) {
    val questions by viewModel.activeTestQuestions.collectAsState()
    val sliceSource by viewModel.activeTestSource.collectAsState()

    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableIntStateOf(0) }
    var showLinkedNotes by remember { mutableStateOf(false) }

    val safeIndex = if (questions.isEmpty()) 0 else currentIndex.coerceIn(0, questions.size - 1)
    val currentQuestion = questions.getOrNull(safeIndex)

    // Timer per question
    LaunchedEffect(safeIndex, isSubmitted) {
        if (!isSubmitted) {
            timerSeconds = 0
            while (true) {
                delay(1000)
                timerSeconds++
            }
        }
    }

    // Reset option and panel when question index changes
    LaunchedEffect(safeIndex) {
        selectedOption = null
        isSubmitted = false
        showLinkedNotes = false
    }

    val linkedNotes = remember(currentQuestion) {
        if (currentQuestion != null) viewModel.notesForQuestion(currentQuestion.questionId) else emptyList()
    }

    val sliceTitle = when (val src = sliceSource) {
        is TestSliceSource.NoteRevision -> "Note Test: ${src.noteTitle}"
        is TestSliceSource.VideoRevision -> "Video Test: ${src.videoTitle}"
        is TestSliceSource.MistakesRetest -> "Mistakes Retest (${src.chapter})"
        is TestSliceSource.DrillDown -> {
            listOfNotNull(src.filter.exam, src.filter.subject, src.filter.chapter).joinToString(" • ")
        }
        null -> "Test Player"
    }

    if (questions.isEmpty() || currentQuestion == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(sliceTitle) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No questions found for this slice.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBack) {
                        Text("Return")
                    }
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = sliceTitle,
                            maxLines = 1,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Question ${safeIndex + 1} of ${questions.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFBBDEFB)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${timerSeconds}s",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D47A1),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (safeIndex > 0) currentIndex = safeIndex - 1
                        },
                        enabled = safeIndex > 0
                    ) {
                        Text("Previous")
                    }

                    if (!isSubmitted) {
                        Button(
                            onClick = {
                                if (selectedOption != null) {
                                    val isCorrect = selectedOption!!.equals(currentQuestion.correctAnswer, ignoreCase = true)
                                    viewModel.submitAttempt(currentQuestion, selectedOption!!, isCorrect, timerSeconds)
                                    isSubmitted = true
                                }
                            },
                            enabled = selectedOption != null,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
                        ) {
                            Text("Submit Answer")
                        }
                    } else {
                        Button(
                            onClick = {
                                if (safeIndex < questions.size - 1) {
                                    currentIndex = safeIndex + 1
                                } else {
                                    onBack()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
                        ) {
                            Text(if (safeIndex < questions.size - 1) "Next Question" else "Finish Test")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Chapter and Topic badge
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE3F2FD))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = currentQuestion.chapter,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D47A1)
                        )
                    }

                    if (currentQuestion.topic.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = currentQuestion.topic,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Question Text
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = currentQuestion.questionText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Missing file = show path, do not crash
                        if (currentQuestion.questionImage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF5F5F5))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Image Asset: ${currentQuestion.questionImage}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // 4 Options
            val options = listOf(
                Triple("A", currentQuestion.optionA, currentQuestion.optionAImage),
                Triple("B", currentQuestion.optionB, currentQuestion.optionBImage),
                Triple("C", currentQuestion.optionC, currentQuestion.optionCImage),
                Triple("D", currentQuestion.optionD, currentQuestion.optionDImage)
            )

            items(options) { (key, optText, optImage) ->
                val isSelected = selectedOption == key
                val isCorrect = key.equals(currentQuestion.correctAnswer, ignoreCase = true)

                val backgroundColor = when {
                    isSubmitted && isCorrect -> Color(0xFFE8F5E9) // Green for correct answer
                    isSubmitted && isSelected && !isCorrect -> Color(0xFFFFEBEE) // Red for wrong chosen
                    isSelected -> Color(0xFFE3F2FD) // Blue selection
                    else -> MaterialTheme.colorScheme.surface
                }

                val borderColor = when {
                    isSubmitted && isCorrect -> Color(0xFF2E7D32)
                    isSubmitted && isSelected && !isCorrect -> Color(0xFFC62828)
                    isSelected -> Color(0xFF0D47A1)
                    else -> MaterialTheme.colorScheme.outlineVariant
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !isSubmitted) {
                            selectedOption = key
                        },
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    border = BorderStroke(1.5.dp, borderColor)
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
                                .background(borderColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                fontWeight = FontWeight.Bold,
                                color = borderColor
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = optText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (optImage.isNotBlank()) {
                                Text(
                                    text = "Option Image: $optImage",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                        }

                        if (isSubmitted) {
                            if (isCorrect) {
                                Icon(Icons.Default.Check, contentDescription = "Correct", tint = Color(0xFF2E7D32))
                            } else if (isSelected) {
                                Icon(Icons.Default.Close, contentDescription = "Wrong", tint = Color(0xFFC62828))
                            }
                        }
                    }
                }
            }

            // Correct vs Chosen summary after submit
            if (isSubmitted) {
                item {
                    val wasCorrect = selectedOption?.equals(currentQuestion.correctAnswer, ignoreCase = true) == true
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (wasCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (wasCorrect) "Correct Answer! (+1)" else "Incorrect! Correct answer is Option ${currentQuestion.correctAnswer}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (wasCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                            Text(
                                text = "Chosen: Option $selectedOption • Correct: Option ${currentQuestion.correctAnswer} • Time: ${timerSeconds}s",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }

            // TOGGLE PANEL BELOW the same question: notes linked via note_questions
            if (linkedNotes.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showLinkedNotes = !showLinkedNotes },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = Color(0xFF0D47A1),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Linked Notes for this Question (${linkedNotes.size})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0D47A1)
                                    )
                                }
                                Icon(
                                    imageVector = if (showLinkedNotes) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = Color(0xFF0D47A1)
                                )
                            }

                            AnimatedVisibility(visible = showLinkedNotes) {
                                Column(
                                    modifier = Modifier.padding(top = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    linkedNotes.forEach { note ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onOpenNoteReader(note) },
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = note.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                    Text(text = "${note.chapter} • ${note.filePath}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                                }
                                                Button(
                                                    onClick = { onOpenNoteReader(note) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
                                                ) {
                                                    Text("Read", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
