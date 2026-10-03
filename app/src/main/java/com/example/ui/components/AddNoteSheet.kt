package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DrillDownFilter
import com.example.data.model.NoteItem
import com.example.ui.viewmodel.ExamPrepViewModel
import kotlinx.coroutines.launch

@Composable
fun AddNoteSheet(
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

    // Note body inputs
    var title by remember { mutableStateOf("") }
    var noteType by remember { mutableStateOf("markdown") }
    var noteContent by remember { mutableStateOf("") }

    // Picked external file
    var pickedFileUri by remember { mutableStateOf<Uri?>(null) }
    var pickedFileName by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pickedFileUri = uri
            val path = uri.lastPathSegment ?: uri.toString()
            pickedFileName = path.substringAfterLast("/").substringAfterLast(":")
            if (pickedFileName?.endsWith(".pdf", ignoreCase = true) == true) {
                noteType = "pdf"
            } else if (pickedFileName?.endsWith(".html", ignoreCase = true) == true || pickedFileName?.endsWith(".htm", ignoreCase = true) == true) {
                noteType = "html"
            } else if (pickedFileName?.endsWith(".md", ignoreCase = true) == true) {
                noteType = "markdown"
            } else if (pickedFileName?.endsWith(".txt", ignoreCase = true) == true) {
                noteType = "txt"
            }
        }
    }

    val rawExam = lockedFilter.exam ?: inputExam.trim()
    val effectiveExam = viewModel.findCanonicalMatch(rawExam, viewModel.listExams())

    val rawSubject = lockedFilter.subject ?: inputSubject.trim()
    val effectiveSubject = viewModel.findCanonicalMatch(rawSubject, viewModel.listSubjects(effectiveExam))

    val rawChapter = lockedFilter.chapter ?: inputChapter.trim()
    val effectiveChapter = viewModel.findCanonicalMatch(rawChapter, viewModel.listChapters(effectiveExam, effectiveSubject))

    val rawTopic = inputTopic.trim().ifBlank { effectiveChapter }
    val effectiveTopic = viewModel.findCanonicalMatch(rawTopic, viewModel.listTopics(effectiveExam, effectiveSubject, effectiveChapter))

    val isSaveEnabled = effectiveExam.isNotBlank() &&
            effectiveSubject.isNotBlank() &&
            effectiveChapter.isNotBlank() &&
            title.trim().isNotBlank()

    AddContentSheetChrome(
        title = "Add Note",
        lockedFilter = lockedFilter,
        isSaveEnabled = isSaveEnabled,
        onDismiss = onDismiss,
        onSave = {
            scope.launch {
                val pickedUri = pickedFileUri
                if (pickedUri != null && !viewModel.isFolderLinked.value) {
                    Toast.makeText(context, "No Data folder linked. Please link a Data folder first.", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val noteId = viewModel.nextNoteId()
                val relFilePath = if (pickedUri != null) {
                    try {
                        viewModel.moveNoteFileToData(pickedUri)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error moving note file: ${e.message}", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                } else {
                    val ext = when (noteType) {
                        "pdf" -> "pdf"
                        "txt" -> "txt"
                        "html" -> "html"
                        else -> "md"
                    }
                    val path = "notes/files/$noteId.$ext"
                    val finalContent = if (noteContent.isNotBlank()) {
                        noteContent.trim()
                    } else if (noteType == "html") {
                        "<!DOCTYPE html>\n<html>\n<head>\n  <meta charset=\"utf-8\">\n  <title>${title.trim()}</title>\n</head>\n<body>\n  <h2>${title.trim()}</h2>\n  <p>Study notes for <strong>$effectiveChapter</strong> ($effectiveSubject).</p>\n</body>\n</html>"
                    } else {
                        "# ${title.trim()}\n\nStudy notes for $effectiveChapter ($effectiveSubject)."
                    }
                    viewModel.writeNoteFileContent(path, finalContent)
                    path
                }

                val newNote = NoteItem(
                    noteId = noteId,
                    exam = effectiveExam,
                    subject = effectiveSubject,
                    chapter = effectiveChapter,
                    topic = effectiveTopic,
                    title = title.trim(),
                    filePath = relFilePath,
                    noteType = noteType
                )

                viewModel.appendNoteRow(newNote)
                Toast.makeText(context, "Created $noteId", Toast.LENGTH_SHORT).show()
                onDismiss()
            }
        }
    ) {
        // Master CSV Hierarchy Pickers (Cascading, Searchable Dropdown with Explicit Add New)
        HierarchyPicker(
            lockedFilter = lockedFilter,
            availableExams = viewModel.listExams(),
            getSubjects = { viewModel.listSubjects(it) },
            getChapters = { ex, sub -> viewModel.listChapters(ex, sub) },
            getTopics = { ex, sub, ch -> viewModel.listTopics(ex, sub, ch) },
            selectedExam = inputExam,
            onExamChange = { inputExam = it },
            selectedSubject = inputSubject,
            onSubjectChange = { inputSubject = it },
            selectedChapter = inputChapter,
            onChapterChange = { inputChapter = it },
            selectedTopic = inputTopic,
            onTopicChange = { inputTopic = it },
            showTopic = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Note Body: Title
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Note Title *") },
            placeholder = { Text("e.g. Important Articles & Case Laws") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Note Type Selection
        Text(
            text = "Note Format / Type",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("markdown", "txt", "pdf", "html").forEach { type ->
                FilterChip(
                    selected = noteType == type,
                    onClick = { noteType = type },
                    label = { Text(type.uppercase(), fontSize = 12.sp) },
                    leadingIcon = if (noteType == type) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // File Source (Pick from phone OR create blank note)
        Text(
            text = "File Attachment / Content",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))

        if (pickedFileUri != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = pickedFileName ?: "Selected file",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "File will be saved under notes/files/",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = {
                        pickedFileUri = null
                        pickedFileName = null
                    }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Remove file",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        filePickerLauncher.launch(arrayOf("text/*", "application/pdf"))
                    },
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pick File from Phone", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = noteContent,
                onValueChange = { noteContent = it },
                label = { Text("Initial Content (or leave blank for template)") },
                placeholder = { Text("Write markdown or text summary here...") },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
