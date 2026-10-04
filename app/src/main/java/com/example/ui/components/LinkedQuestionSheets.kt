package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuestionItem
import com.example.ui.viewmodel.ExamPrepViewModel
import kotlinx.coroutines.launch

/**
 * Question Bank Column Input:
 * - Shows distinct values from questions.csv in a dropdown.
 * - Allows typing a new value if not in the list.
 * - Allows leaving empty.
 */
@Composable
fun QuestionBankColumnField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    choices: List<String>,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                placeholder = { Text(placeholder, fontSize = 12.sp) },
                singleLine = true,
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (value.isNotBlank()) {
                            IconButton(
                                onClick = { onValueChange("") },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear $label",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = { expanded = !expanded },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Show $label choices"
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 240.dp)
            ) {
                if (choices.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No existing values in questions.csv", style = MaterialTheme.typography.bodySmall) },
                        onClick = { expanded = false }
                    )
                } else {
                    choices.forEach { choice ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(choice, style = MaterialTheme.typography.bodyMedium)
                                    if (choice.equals(value.trim(), ignoreCase = true)) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onValueChange(choice)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Step 1 & Step 2 & Step 3:
 * Import Questions CSV or ZIP for an open reel or open note.
 * Links automatically to the open reel or open note.
 * Tree columns come from question bank (questions.csv) and are NOT autofilled from reel/note.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkedQuestionImportSheet(
    targetType: String, // "Reel" or "Note"
    targetTitle: String,
    targetId: String,
    viewModel: ExamPrepViewModel,
    onDismiss: () -> Unit,
    onSuccess: (count: Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Tree columns from question bank: NOT autofilled from reel/note!
    var exam by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var chapter by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }

    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val examChoices = remember { viewModel.getQuestionBankExams() }
    val subjectChoices = remember { viewModel.getQuestionBankSubjects() }
    val chapterChoices = remember { viewModel.getQuestionBankChapters() }
    val topicChoices = remember { viewModel.getQuestionBankTopics() }

    fun processImport(uri: Uri, isZip: Boolean) {
        scope.launch {
            isProcessing = true
            errorMessage = null
            try {
                val linkedNoteId = if (targetType.equals("Note", ignoreCase = true)) targetId else null
                val linkedVideoId = if (targetType.equals("Reel", ignoreCase = true)) targetId else null

                val importedCount = viewModel.importQuestionsForLinkedEntity(
                    sourceUri = uri,
                    isZip = isZip,
                    overrideExam = exam.trim().ifBlank { null },
                    overrideSubject = subject.trim().ifBlank { null },
                    overrideChapter = chapter.trim().ifBlank { null },
                    overrideTopic = topic.trim().ifBlank { null },
                    linkedNoteId = linkedNoteId,
                    linkedVideoId = linkedVideoId
                )

                Toast.makeText(
                    context,
                    "Imported $importedCount questions and linked to $targetType \"$targetTitle\"!",
                    Toast.LENGTH_LONG
                ).show()

                onSuccess(importedCount)
                onDismiss()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to import questions"
            } finally {
                isProcessing = false
            }
        }
    }

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            processImport(uri, isZip = false)
        }
    }

    val zipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            processImport(uri, isZip = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.UploadFile,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Import Questions to $targetType",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Linked to: $targetTitle",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Guidance banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Link,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Automatic Link: $targetType [$targetId]",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Fill only the columns you want to force. Leave empty to keep values from the imported CSV.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Tree Columns (from Question Bank)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Step 1: Form fields for tree columns
            QuestionBankColumnField(
                label = "Exam",
                value = exam,
                onValueChange = { exam = it },
                choices = examChoices,
                placeholder = "Leave empty to keep CSV value"
            )

            Spacer(modifier = Modifier.height(8.dp))

            QuestionBankColumnField(
                label = "Subject",
                value = subject,
                onValueChange = { subject = it },
                choices = subjectChoices,
                placeholder = "Leave empty to keep CSV value"
            )

            Spacer(modifier = Modifier.height(8.dp))

            QuestionBankColumnField(
                label = "Chapter",
                value = chapter,
                onValueChange = { chapter = it },
                choices = chapterChoices,
                placeholder = "Leave empty to keep CSV value"
            )

            Spacer(modifier = Modifier.height(8.dp))

            QuestionBankColumnField(
                label = "Topic",
                value = topic,
                onValueChange = { topic = it },
                choices = topicChoices,
                placeholder = "Leave empty to keep CSV value"
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isProcessing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Importing and linking questions...", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                // Step 2: Import CSV or ZIP
                Button(
                    onClick = { csvLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pick questions.csv", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { zipLauncher.launch("application/zip") },
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pick questions.zip (with images)", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Step 4: Add Single Question for an open reel or open note.
 * Uses the same question bank column form (select existing or type new).
 * Does NOT autofill from the reel or note.
 * On save, appends row to questions/questions.csv and links to open reel or note.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkedSingleQuestionSheet(
    targetType: String, // "Reel" or "Note"
    targetTitle: String,
    targetId: String,
    viewModel: ExamPrepViewModel,
    onDismiss: () -> Unit,
    onSuccess: (questionId: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Tree columns from question bank: NOT autofilled from reel/note!
    var exam by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var chapter by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }

    // Question content fields
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

    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val examChoices = remember { viewModel.getQuestionBankExams() }
    val subjectChoices = remember { viewModel.getQuestionBankSubjects() }
    val chapterChoices = remember { viewModel.getQuestionBankChapters() }
    val topicChoices = remember { viewModel.getQuestionBankTopics() }

    val isSaveEnabled = exam.trim().isNotBlank() &&
            subject.trim().isNotBlank() &&
            chapter.trim().isNotBlank() &&
            questionText.trim().isNotBlank() &&
            optionA.trim().isNotBlank() &&
            optionB.trim().isNotBlank() &&
            optionC.trim().isNotBlank() &&
            optionD.trim().isNotBlank() &&
            !isProcessing

    fun saveQuestion() {
        if (!isSaveEnabled) return
        scope.launch {
            isProcessing = true
            errorMessage = null
            try {
                val effectiveTopic = topic.trim().ifBlank { chapter.trim() }
                val newQuestion = QuestionItem(
                    exam = exam.trim(),
                    questionId = "", // Will be assigned automatically with unique ID
                    subject = subject.trim(),
                    chapter = chapter.trim(),
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

                val linkedNoteId = if (targetType.equals("Note", ignoreCase = true)) targetId else null
                val linkedVideoId = if (targetType.equals("Reel", ignoreCase = true)) targetId else null

                val created = viewModel.addSingleQuestionForLinkedEntity(
                    question = newQuestion,
                    linkedNoteId = linkedNoteId,
                    linkedVideoId = linkedVideoId
                )

                Toast.makeText(
                    context,
                    "Added ${created.questionId} and linked to $targetType \"$targetTitle\"!",
                    Toast.LENGTH_SHORT
                ).show()

                onSuccess(created.questionId)
                onDismiss()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to add question"
            } finally {
                isProcessing = false
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Add Single Question to $targetType",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Linked to: $targetTitle",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Guidance badge
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Automatic Link: $targetType [$targetId]. Select existing question bank value or type new.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Tree Columns (from Question Bank)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Step 1: Form fields for tree columns (Not autofilled from reel/note!)
            QuestionBankColumnField(
                label = "Exam *",
                value = exam,
                onValueChange = { exam = it },
                choices = examChoices,
                placeholder = "Select existing or type new..."
            )

            Spacer(modifier = Modifier.height(8.dp))

            QuestionBankColumnField(
                label = "Subject *",
                value = subject,
                onValueChange = { subject = it },
                choices = subjectChoices,
                placeholder = "Select existing or type new..."
            )

            Spacer(modifier = Modifier.height(8.dp))

            QuestionBankColumnField(
                label = "Chapter *",
                value = chapter,
                onValueChange = { chapter = it },
                choices = chapterChoices,
                placeholder = "Select existing or type new..."
            )

            Spacer(modifier = Modifier.height(8.dp))

            QuestionBankColumnField(
                label = "Topic (optional)",
                value = topic,
                onValueChange = { topic = it },
                choices = topicChoices,
                placeholder = "Select existing or type new (defaults to chapter)..."
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Question Text
            OutlinedTextField(
                value = questionText,
                onValueChange = { questionText = it },
                label = { Text("Question Text *") },
                placeholder = { Text("Enter question...") },
                minLines = 2,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Options (All 4 Required)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
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

            Text(
                text = "Correct Answer",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("A", "B", "C", "D").forEach { opt ->
                    FilterChip(
                        selected = correctAnswer == opt,
                        onClick = { correctAnswer = opt },
                        label = { Text(opt, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Optional Image Section
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
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
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

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { saveQuestion() },
                enabled = isSaveEnabled,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saving...")
                } else {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save & Link to $targetType", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
