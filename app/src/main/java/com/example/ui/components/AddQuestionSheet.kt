package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DrillDownFilter
import com.example.data.model.QuestionItem
import com.example.ui.theme.EasySolid
import com.example.ui.viewmodel.ExamPrepViewModel
import kotlinx.coroutines.launch

@Composable
fun AddQuestionSheet(
    lockedFilter: DrillDownFilter,
    viewModel: ExamPrepViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Hierarchy inputs (editable only if not locked)
    var inputExam by remember { mutableStateOf(lockedFilter.exam ?: "") }
    var inputSubject by remember { mutableStateOf(lockedFilter.subject ?: "") }
    var inputChapter by remember { mutableStateOf(lockedFilter.chapter ?: "") }
    var inputTopic by remember { mutableStateOf(lockedFilter.topic ?: "") }

    // Question body inputs
    var questionText by remember { mutableStateOf("") }
    var optionA by remember { mutableStateOf("") }
    var optionB by remember { mutableStateOf("") }
    var optionC by remember { mutableStateOf("") }
    var optionD by remember { mutableStateOf("") }
    var correctAnswer by remember { mutableStateOf("A") }

    // Optional Images
    var showImagesSection by remember { mutableStateOf(false) }
    var questionImage by remember { mutableStateOf("") }
    var optionAImage by remember { mutableStateOf("") }
    var optionBImage by remember { mutableStateOf("") }
    var optionCImage by remember { mutableStateOf("") }
    var optionDImage by remember { mutableStateOf("") }

    // Optional Linking
    var selectedNoteId by remember { mutableStateOf<String?>(null) }
    var selectedVideoId by remember { mutableStateOf<String?>(null) }
    var noteMenuExpanded by remember { mutableStateOf(false) }
    var videoMenuExpanded by remember { mutableStateOf(false) }

    val effectiveExam = lockedFilter.exam ?: inputExam.trim()
    val effectiveSubject = lockedFilter.subject ?: inputSubject.trim()
    val effectiveChapter = lockedFilter.chapter ?: inputChapter.trim()
    val effectiveTopic = inputTopic.trim().ifBlank { effectiveChapter }

    // Filter available notes and videos for linking by current exam/subject/chapter
    val availableNotes = remember(effectiveExam, effectiveSubject, effectiveChapter) {
        if (effectiveExam.isNotBlank()) {
            viewModel.notesForSlice(
                DrillDownFilter(
                    exam = effectiveExam,
                    subject = effectiveSubject.ifBlank { null },
                    chapter = effectiveChapter.ifBlank { null }
                )
            )
        } else emptyList()
    }

    val availableVideos = remember(effectiveExam, effectiveSubject, effectiveChapter) {
        if (effectiveExam.isNotBlank()) {
            viewModel.videosForSlice(
                DrillDownFilter(
                    exam = effectiveExam,
                    subject = effectiveSubject.ifBlank { null },
                    chapter = effectiveChapter.ifBlank { null }
                )
            )
        } else emptyList()
    }

    val isSaveEnabled = effectiveExam.isNotBlank() &&
            effectiveSubject.isNotBlank() &&
            effectiveChapter.isNotBlank() &&
            questionText.trim().isNotBlank() &&
            optionA.trim().isNotBlank() &&
            optionB.trim().isNotBlank() &&
            optionC.trim().isNotBlank() &&
            optionD.trim().isNotBlank() &&
            correctAnswer in listOf("A", "B", "C", "D")

    AddContentSheetChrome(
        title = "Add Question",
        lockedFilter = lockedFilter,
        isSaveEnabled = isSaveEnabled,
        onDismiss = onDismiss,
        onSave = {
            scope.launch {
                val qId = viewModel.nextQuestionId()
                val question = QuestionItem(
                    exam = effectiveExam,
                    questionId = qId,
                    subject = effectiveSubject,
                    chapter = effectiveChapter,
                    topic = effectiveTopic,
                    questionText = questionText.trim(),
                    optionA = optionA.trim(),
                    optionB = optionB.trim(),
                    optionC = optionC.trim(),
                    optionD = optionD.trim(),
                    questionImage = questionImage.trim(),
                    optionAImage = optionAImage.trim(),
                    optionBImage = optionBImage.trim(),
                    optionCImage = optionCImage.trim(),
                    optionDImage = optionDImage.trim(),
                    correctAnswer = correctAnswer
                )

                viewModel.appendQuestion(question)

                val noteId = selectedNoteId
                if (noteId != null) {
                    viewModel.appendNoteLink(noteId, qId)
                }

                val videoId = selectedVideoId
                if (videoId != null) {
                    viewModel.appendVideoLink(videoId, qId)
                }

                val linkMsg = when {
                    noteId != null && videoId != null -> " (linked to $noteId / $videoId)"
                    noteId != null -> " (linked to $noteId)"
                    videoId != null -> " (linked to $videoId)"
                    else -> ""
                }

                Toast.makeText(context, "Created $qId$linkMsg", Toast.LENGTH_SHORT).show()
                onDismiss()
            }
        }
    ) {
        // Tree Lock: Unlocked hierarchy fields
        if (lockedFilter.exam == null) {
            OutlinedTextField(
                value = inputExam,
                onValueChange = { inputExam = it },
                label = { Text("Exam Name *") },
                placeholder = { Text("e.g. UPSI, SSC, UP_POLICE") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (lockedFilter.subject == null) {
            OutlinedTextField(
                value = inputSubject,
                onValueChange = { inputSubject = it },
                label = { Text("Subject Name *") },
                placeholder = { Text("e.g. Law, General Hindi, GK") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (lockedFilter.chapter == null) {
            OutlinedTextField(
                value = inputChapter,
                onValueChange = { inputChapter = it },
                label = { Text("Chapter Name *") },
                placeholder = { Text("e.g. Fundamental Rights, IPC Offences") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = inputTopic,
            onValueChange = { inputTopic = it },
            label = { Text("Topic (Optional)") },
            placeholder = { Text("Defaults to chapter name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Question Text (Required)
        OutlinedTextField(
            value = questionText,
            onValueChange = { questionText = it },
            label = { Text("Question Text *") },
            placeholder = { Text("Enter the question statement...") },
            minLines = 2,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Options A, B, C, D (All Required)
        Text(
            text = "Answer Options (All 4 Required)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = optionA,
            onValueChange = { optionA = it },
            label = { Text("Option A *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = optionB,
            onValueChange = { optionB = it },
            label = { Text("Option B *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = optionC,
            onValueChange = { optionC = it },
            label = { Text("Option C *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = optionD,
            onValueChange = { optionD = it },
            label = { Text("Option D *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Correct Answer Selector (A / B / C / D)
        Text(
            text = "Correct Answer",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("A", "B", "C", "D").forEach { opt ->
                FilterChip(
                    selected = correctAnswer == opt,
                    onClick = { correctAnswer = opt },
                    label = { Text("Option $opt", fontWeight = FontWeight.Bold) },
                    leadingIcon = if (correctAnswer == opt) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EasySolid,
                        selectedLabelColor = MaterialTheme.colorScheme.surface,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Optional Linking Section (Note & Video)
        Text(
            text = "Connect Links (Optional)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Link Note Selector
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { noteMenuExpanded = true },
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Note, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        val noteLabel = selectedNoteId?.let { id ->
                            val n = availableNotes.find { it.noteId == id }
                            "[$id] ${n?.title ?: "Note"}"
                        } ?: "Link to Note (None selected)"
                        Text(noteLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            DropdownMenu(
                expanded = noteMenuExpanded,
                onDismissRequest = { noteMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("None (Do not link)") },
                    onClick = {
                        selectedNoteId = null
                        noteMenuExpanded = false
                    }
                )
                availableNotes.forEach { note ->
                    DropdownMenuItem(
                        text = { Text("[${note.noteId}] ${note.title}") },
                        onClick = {
                            selectedNoteId = note.noteId
                            noteMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Link Video Selector
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { videoMenuExpanded = true },
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        val videoLabel = selectedVideoId?.let { id ->
                            val v = availableVideos.find { it.videoId == id }
                            "[$id] ${v?.title ?: "Video"}"
                        } ?: "Link to Video (None selected)"
                        Text(videoLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            DropdownMenu(
                expanded = videoMenuExpanded,
                onDismissRequest = { videoMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("None (Do not link)") },
                    onClick = {
                        selectedVideoId = null
                        videoMenuExpanded = false
                    }
                )
                availableVideos.forEach { video ->
                    DropdownMenuItem(
                        text = { Text("[${video.videoId}] ${video.title}") },
                        onClick = {
                            selectedVideoId = video.videoId
                            videoMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Optional Images Collapsible Section
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { showImagesSection = !showImagesSection },
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Image Attachments (Optional)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    imageVector = if (showImagesSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
        }

        AnimatedVisibility(visible = showImagesSection) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = questionImage,
                    onValueChange = { questionImage = it },
                    label = { Text("Question Image Path / URL") },
                    placeholder = { Text("e.g. questions/images/q081.png") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optionAImage,
                    onValueChange = { optionAImage = it },
                    label = { Text("Option A Image Path") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optionBImage,
                    onValueChange = { optionBImage = it },
                    label = { Text("Option B Image Path") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optionCImage,
                    onValueChange = { optionCImage = it },
                    label = { Text("Option C Image Path") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optionDImage,
                    onValueChange = { optionDImage = it },
                    label = { Text("Option D Image Path") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
