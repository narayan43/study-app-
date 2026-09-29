package com.example.ui.screens

import android.net.Uri
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
import androidx.compose.material.icons.filled.SmartDisplay
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.NoteItem
import com.example.data.model.QuestionItem
import com.example.data.model.TestSliceSource
import com.example.data.model.VideoItem
import com.example.ui.theme.AppBackgroundDark
import com.example.ui.theme.EasySolid
import com.example.ui.theme.EasySolidDark
import com.example.ui.theme.EasyTint
import com.example.ui.theme.EasyTintDark
import com.example.ui.theme.HardSolid
import com.example.ui.theme.HardSolidDark
import com.example.ui.theme.HardTint
import com.example.ui.theme.HardTintDark
import com.example.ui.theme.OnEasy
import com.example.ui.theme.OnEasyDark
import com.example.ui.theme.OnHard
import com.example.ui.theme.OnHardDark
import com.example.ui.viewmodel.ExamPrepViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestPlayerScreen(
    viewModel: ExamPrepViewModel,
    onBack: (TestSliceSource?) -> Unit,
    onOpenNoteReader: (NoteItem) -> Unit,
    onOpenReel: (VideoItem) -> Unit
) {
    val questions by viewModel.activeTestQuestions.collectAsState()
    val sliceSource by viewModel.activeTestSource.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableIntStateOf(0) }
    var showLinkedNotes by remember { mutableStateOf(false) }

    val safeIndex = if (questions.isEmpty()) 0 else currentIndex.coerceIn(0, questions.size - 1)
    val currentQuestion = questions.getOrNull(safeIndex)

    // Timer per question (starts on show, stops on submit)
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

    val linkedVideos = remember(currentQuestion) {
        if (currentQuestion != null) viewModel.videosForQuestion(currentQuestion.questionId) else emptyList()
    }

    val sliceTitle = when (val src = sliceSource) {
        is TestSliceSource.NoteRevision -> "Note Revision: ${src.noteTitle}"
        is TestSliceSource.VideoRevision -> "Video Revision: ${src.videoTitle}"
        is TestSliceSource.MistakesRetest -> "Mistakes Retest: ${src.chapter}"
        is TestSliceSource.DrillDown -> {
            val list = listOfNotNull(src.filter.exam, src.filter.subject, src.filter.chapter, src.filter.topic)
            if (list.isEmpty()) "All Questions" else list.joinToString(" • ")
        }
        null -> "Test Player"
    }

    if (questions.isEmpty() || currentQuestion == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(sliceTitle) },
                    navigationIcon = {
                        IconButton(onClick = { onBack(sliceSource) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No questions found for this slice.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onBack(sliceSource) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
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
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Question ${safeIndex + 1} of ${questions.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onBack(sliceSource) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${timerSeconds}s",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous: outline, Divider border, TextPrimary (not 30% white)
                    OutlinedButton(
                        onClick = {
                            if (safeIndex > 0) currentIndex = safeIndex - 1
                        },
                        enabled = safeIndex > 0,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Text("Previous", fontWeight = FontWeight.SemiBold)
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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Submit Answer", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (safeIndex < questions.size - 1) {
                                    currentIndex = safeIndex + 1
                                } else {
                                    onBack(sliceSource)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(if (safeIndex < questions.size - 1) "Next Question" else "Finish Test", fontWeight = FontWeight.Bold)
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
            // Chapter and Topic badge (Chips: Divider border, TextSecondary)
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "${currentQuestion.exam} • ${currentQuestion.chapter}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (currentQuestion.topic.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = currentQuestion.topic,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Question Card: Surface, TextPrimary, Divider border
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = currentQuestion.questionText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Render Question Image if non-blank
                        if (currentQuestion.questionImage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            val imgUri = viewModel.resolveMediaUri(currentQuestion.questionImage)
                            if (imgUri != null) {
                                AsyncImage(
                                    model = imgUri,
                                    contentDescription = "Question Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Text(
                                    text = "Image: ${currentQuestion.questionImage} (file not found)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

                // High contrast state logic
                val backgroundColor = when {
                    isSubmitted && isCorrect -> if (isDarkTheme) EasyTintDark else EasyTint
                    isSubmitted && isSelected && !isCorrect -> if (isDarkTheme) HardTintDark else HardTint
                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    else -> MaterialTheme.colorScheme.surface
                }

                val borderColor = when {
                    isSubmitted && isCorrect -> if (isDarkTheme) EasySolidDark else EasySolid
                    isSubmitted && isSelected && !isCorrect -> if (isDarkTheme) HardSolidDark else HardSolid
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline
                }

                val contentTextColor = when {
                    isSubmitted && isCorrect -> if (isDarkTheme) OnEasyDark else OnEasy
                    isSubmitted && isSelected && !isCorrect -> if (isDarkTheme) OnHardDark else OnHard
                    else -> MaterialTheme.colorScheme.onSurface
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !isSubmitted) {
                            selectedOption = key
                        },
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    border = BorderStroke(if (isSelected || isSubmitted && isCorrect) 1.5.dp else 1.dp, borderColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                    color = contentTextColor
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = optText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = contentTextColor,
                                modifier = Modifier.weight(1f)
                            )

                            if (isSubmitted) {
                                if (isCorrect) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Correct",
                                        tint = if (isDarkTheme) EasySolidDark else EasySolid
                                    )
                                } else if (isSelected) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Wrong",
                                        tint = if (isDarkTheme) HardSolidDark else HardSolid
                                    )
                                }
                            }
                        }

                        if (optImage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val optUri = viewModel.resolveMediaUri(optImage)
                            if (optUri != null) {
                                AsyncImage(
                                    model = optUri,
                                    contentDescription = "Option $key Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Text(
                                    text = "Option Image: $optImage (file not found)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Feedback banner: “Incorrect! Correct option is A”
            if (isSubmitted) {
                item {
                    val wasCorrect = selectedOption?.equals(currentQuestion.correctAnswer, ignoreCase = true) == true
                    val bannerBg = if (wasCorrect) {
                        if (isDarkTheme) EasyTintDark else EasyTint
                    } else {
                        if (isDarkTheme) HardTintDark else HardTint
                    }
                    val bannerHeadingColor = if (wasCorrect) {
                        if (isDarkTheme) OnEasyDark else OnEasy
                    } else {
                        if (isDarkTheme) OnHardDark else OnHard
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = bannerBg),
                        border = BorderStroke(1.dp, if (wasCorrect) EasySolid else HardSolid)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (wasCorrect) "Correct Answer!" else "Incorrect! Correct option is ${currentQuestion.correctAnswer}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = bannerHeadingColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Chosen: Option $selectedOption • Correct: Option ${currentQuestion.correctAnswer} • Time: ${timerSeconds}s",
                                style = MaterialTheme.typography.bodySmall,
                                color = bannerHeadingColor.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // Revision Resources row: Surface, TextPrimary title, TextSecondary count
            if (isSubmitted && (linkedNotes.isNotEmpty() || linkedVideos.isNotEmpty())) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Revision Resources",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${linkedNotes.size} Notes, ${linkedVideos.size} Reels)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = if (showLinkedNotes) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            AnimatedVisibility(visible = showLinkedNotes) {
                                Column(
                                    modifier = Modifier.padding(top = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Linked Notes list
                                    linkedNotes.forEach { note ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { onOpenNoteReader(note) },
                                            color = MaterialTheme.colorScheme.background,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = note.title,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "Note: ${note.filePath}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Button(
                                                    onClick = { onOpenNoteReader(note) },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary,
                                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                ) {
                                                    Text("Read", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    // Related Reels list
                                    linkedVideos.forEach { video ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { onOpenReel(video) },
                                            color = MaterialTheme.colorScheme.background,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = video.title,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "Reel: ${video.videoPath}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Button(
                                                    onClick = { onOpenReel(video) },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary,
                                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                ) {
                                                    Icon(Icons.Default.SmartDisplay, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Watch", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
