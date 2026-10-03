package com.example.ui.components

import android.content.Context
import android.media.MediaMetadataRetriever
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DrillDownFilter
import com.example.data.model.VideoItem
import com.example.ui.viewmodel.ExamPrepViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AddVideoSheet(
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

    // Video body inputs
    var title by remember { mutableStateOf("") }
    var durationSecStr by remember { mutableStateOf("60") }

    // Source selection: "phone" (pick video) or "path" (URL / local relative path)
    var videoSourceType by remember { mutableStateOf("phone") }
    var manualPath by remember { mutableStateOf("") }

    // Picked external video
    var pickedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var pickedVideoName by remember { mutableStateOf<String?>(null) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pickedVideoUri = uri
            val seg = uri.lastPathSegment ?: uri.toString()
            pickedVideoName = seg.substringAfterLast("/").substringAfterLast(":")
            scope.launch {
                val probed = withContext(Dispatchers.IO) {
                    probeVideoDuration(context, uri)
                }
                durationSecStr = probed.toString()
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
    val effectiveDuration = durationSecStr.trim().toIntOrNull()?.coerceAtLeast(1) ?: 60

    val existingTitles = remember(effectiveExam, effectiveSubject, effectiveChapter, effectiveTopic) {
        viewModel.listVideoTitles(
            effectiveExam.ifBlank { null },
            effectiveSubject.ifBlank { null },
            effectiveChapter.ifBlank { null },
            effectiveTopic.ifBlank { null }
        )
    }

    val hasValidSource = if (videoSourceType == "phone") {
        pickedVideoUri != null
    } else {
        manualPath.trim().isNotBlank()
    }

    val isSaveEnabled = effectiveExam.isNotBlank() &&
            effectiveSubject.isNotBlank() &&
            effectiveChapter.isNotBlank() &&
            title.trim().isNotBlank() &&
            hasValidSource

    AddContentSheetChrome(
        title = "Add Reel Video",
        lockedFilter = lockedFilter,
        isSaveEnabled = isSaveEnabled,
        onDismiss = onDismiss,
        onSave = {
            scope.launch {
                val pickedUri = pickedVideoUri
                if (videoSourceType == "phone" && pickedUri != null && !viewModel.isFolderLinked.value) {
                    Toast.makeText(context, "No Data folder linked. Please link a Data folder first.", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val videoId = viewModel.nextVideoId()
                val targetPath = if (videoSourceType == "phone" && pickedUri != null) {
                    try {
                        viewModel.moveVideoFileToData(pickedUri)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error moving video file: ${e.message}", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                } else {
                    manualPath.trim()
                }

                val newVideo = VideoItem(
                    videoId = videoId,
                    exam = effectiveExam,
                    subject = effectiveSubject,
                    chapter = effectiveChapter,
                    topic = effectiveTopic,
                    title = title.trim(),
                    videoPath = targetPath,
                    durationSec = effectiveDuration
                )

                viewModel.appendVideoRow(newVideo)
                Toast.makeText(context, "Created $videoId", Toast.LENGTH_SHORT).show()
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

        // Existing Video Titles quick-picker (when available in master CSV)
        if (existingTitles.isNotEmpty()) {
            Text(
                text = "Existing Titles for this Topic",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(existingTitles.size) { idx ->
                    val t = existingTitles[idx]
                    FilterChip(
                        selected = title.equals(t, ignoreCase = true),
                        onClick = { title = t },
                        label = { Text(t, fontSize = 11.sp, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Video Body: Title
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Video Title *") },
            placeholder = { Text("e.g. Important Sections of IPC Explained") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Video Source Selector (Phone vs URL/Path)
        Text(
            text = "Video Media Source",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = videoSourceType == "phone",
                onClick = { videoSourceType = "phone" },
                label = { Text("Pick from Phone") },
                leadingIcon = { Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                )
            )

            FilterChip(
                selected = videoSourceType == "path",
                onClick = { videoSourceType = "path" },
                label = { Text("Enter URL / Path") },
                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (videoSourceType == "phone") {
            if (pickedVideoUri != null) {
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
                                Icons.Default.VideoFile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = pickedVideoName ?: "Selected Video",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Will be copied to videos/files/",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = {
                            pickedVideoUri = null
                            pickedVideoName = null
                        }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear file",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick = {
                        videoPickerLauncher.launch(arrayOf("video/*"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Choose Video File from Device")
                }
            }
        } else {
            OutlinedTextField(
                value = manualPath,
                onValueChange = { manualPath = it },
                label = { Text("Video URL or Local Path *") },
                placeholder = { Text("e.g. https://example.com/video.mp4 or videos/files/lecture.mp4") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Duration (in seconds)
        OutlinedTextField(
            value = durationSecStr,
            onValueChange = { durationSecStr = it.filter { ch -> ch.isDigit() } },
            label = { Text("Duration (seconds)") },
            placeholder = { Text("60") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun probeVideoDuration(context: Context, uri: Uri): Int {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val timeMsStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()
        val timeMs = timeMsStr?.toLongOrNull() ?: 60000L
        (timeMs / 1000L).coerceAtLeast(1L).toInt()
    } catch (_: Exception) {
        60
    }
}
