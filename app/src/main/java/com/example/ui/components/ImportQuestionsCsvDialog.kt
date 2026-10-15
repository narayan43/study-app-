package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.ui.viewmodel.ExamPrepViewModel
import kotlinx.coroutines.launch

@Composable
fun ImportQuestionsCsvDialog(
    overrideFilter: DrillDownFilter? = null,
    viewModel: ExamPrepViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pickedFileUri by remember { mutableStateOf<Uri?>(null) }
    var pickedFileName by remember { mutableStateOf<String?>(null) }
    var noteIdInput by remember { mutableStateOf("") }
    var videoIdInput by remember { mutableStateOf("") }
    var isImporting by remember { mutableStateOf(false) }

    val knownNoteIds = remember { viewModel.getAllKnownNoteIds() }
    val knownVideoIds = remember { viewModel.getAllKnownVideoIds() }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pickedFileUri = uri
            val seg = uri.lastPathSegment ?: uri.toString()
            pickedFileName = seg.substringAfterLast("/").substringAfterLast(":")
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isImporting) onDismiss() },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import questions.csv", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                if (!isImporting) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select a questions.csv to merge. You can optionally assign a note ID or video ID to link each question to that note or reel.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 1. Pick CSV File
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = !isImporting) {
                            filePickerLauncher.launch(arrayOf("text/*", "text/comma-separated-values", "application/csv"))
                        },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AttachFile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = pickedFileName ?: "Tap to choose questions.csv file *",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (pickedFileName != null) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (pickedFileName != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (pickedFileUri != null) {
                            IconButton(
                                onClick = {
                                    pickedFileUri = null
                                    pickedFileName = null
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Remove file", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // 2. Optional note_id
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = noteIdInput,
                        onValueChange = { noteIdInput = it },
                        label = { Text("Assign note_id (Optional)") },
                        placeholder = { Text("e.g. NOTE_001 or type new ID") },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Filled.Note, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = if (noteIdInput.isNotBlank()) {
                            {
                                IconButton(onClick = { noteIdInput = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        } else null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            Text(
                                "Appends link rows in links/note_questions.csv. Note does not have to exist yet.",
                                fontSize = 11.sp
                            )
                        }
                    )

                    if (knownNoteIds.isNotEmpty() && noteIdInput.isBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Suggestions:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            knownNoteIds.take(3).forEach { id ->
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { noteIdInput = id },
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = id,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Optional video_id
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = videoIdInput,
                        onValueChange = { videoIdInput = it },
                        label = { Text("Assign video_id (Optional)") },
                        placeholder = { Text("e.g. VID_001 or type new ID") },
                        leadingIcon = {
                            Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = if (videoIdInput.isNotBlank()) {
                            {
                                IconButton(onClick = { videoIdInput = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        } else null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            Text(
                                "Appends link rows in links/video_questions.csv. Reel does not have to exist yet.",
                                fontSize = 11.sp
                            )
                        }
                    )

                    if (knownVideoIds.isNotEmpty() && videoIdInput.isBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Suggestions:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            knownVideoIds.take(3).forEach { id ->
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { videoIdInput = id },
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = id,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val uri = pickedFileUri
                    if (uri == null) {
                        Toast.makeText(context, "Please choose a CSV file first", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (!viewModel.isFolderLinked.value) {
                        Toast.makeText(context, "No Data folder linked. Please link a Data folder first.", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    isImporting = true
                    scope.launch {
                        try {
                            val cleanNote = noteIdInput.trim().ifBlank { null }
                            val cleanVideo = videoIdInput.trim().ifBlank { null }
                            val count = viewModel.importAndMergeQuestionsCsv(
                                sourceUri = uri,
                                linkedNoteId = cleanNote,
                                linkedVideoId = cleanVideo,
                                overrideFilter = overrideFilter
                            )
                            val linkMsg = when {
                                cleanNote != null && cleanVideo != null -> " (linked to Note: $cleanNote, Video: $cleanVideo)"
                                cleanNote != null -> " (linked to Note: $cleanNote)"
                                cleanVideo != null -> " (linked to Video: $cleanVideo)"
                                else -> ""
                            }
                            Toast.makeText(
                                context,
                                "Moved to Data/questions/ & merged $count questions$linkMsg",
                                Toast.LENGTH_LONG
                            ).show()
                            onDismiss()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isImporting = false
                        }
                    }
                },
                enabled = pickedFileUri != null && !isImporting
            ) {
                if (isImporting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Importing...")
                } else {
                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import Questions")
                }
            }
        },
        dismissButton = {
            if (!isImporting) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
