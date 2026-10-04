package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.platform.LocalContext
import com.example.data.csv.CsvHelper
import com.example.data.model.DrillDownFilter
import com.example.data.model.NoteItem
import com.example.data.model.TestSliceSource
import com.example.ui.components.AddContentSheetChrome
import com.example.ui.components.AddNoteSheet
import com.example.ui.components.CircularAddButton
import com.example.ui.components.DrillDownSelector
import com.example.ui.components.LinkedQuestionImportSheet
import com.example.ui.components.LinkedSingleQuestionSheet
import com.example.ui.viewmodel.ExamPrepViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults

@Composable
fun NotesScreen(
    viewModel: ExamPrepViewModel,
    onStartTestSlice: (TestSliceSource) -> Unit
) {
    val context = LocalContext.current
    val notesFilter by viewModel.notesFilter.collectAsState()
    val activeNote by viewModel.activeNote.collectAsState()
    val activeNoteContent by viewModel.activeNoteContent.collectAsState()
    val scope = rememberCoroutineScope()

    var showAddSheet by remember { mutableStateOf(false) }
    var showAddMenu by remember { mutableStateOf(false) }
    var isSliceSelected by remember { mutableStateOf(false) }

    val showNotes = (notesFilter.chapter != null) || isSliceSelected

    BackHandler(enabled = showNotes || notesFilter.exam != null) {
        if (showNotes) {
            isSliceSelected = false
            when {
                notesFilter.chapter != null -> viewModel.setNotesFilter(notesFilter.copy(chapter = null, topic = null))
                notesFilter.subject != null -> viewModel.setNotesFilter(notesFilter.copy(subject = null, chapter = null, topic = null))
                else -> viewModel.setNotesFilter(DrillDownFilter())
            }
        } else if (notesFilter.subject != null) {
            viewModel.setNotesFilter(notesFilter.copy(subject = null, chapter = null, topic = null))
        } else if (notesFilter.exam != null) {
            viewModel.setNotesFilter(DrillDownFilter())
        }
    }

    val csvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val csvContent = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
                    val notes = CsvHelper.parseNotes(csvContent)
                    if (notes.isEmpty()) {
                        Toast.makeText(context, "No valid notes found or invalid notes.csv header", Toast.LENGTH_SHORT).show()
                    } else {
                        val (remapped, idMap) = viewModel.remapIncomingNoteIds(notes)
                        viewModel.appendNotesBulk(remapped)
                        val remappedCount = idMap.count { it.key != it.value }
                        val msg = if (remappedCount > 0) {
                            "Imported ${remapped.size} notes ($remappedCount remapped to prevent collisions)"
                        } else {
                            "Imported ${remapped.size} notes successfully"
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to import notes.csv: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    var noteToDelete by remember { mutableStateOf<NoteItem?>(null) }

    if (noteToDelete != null) {
        val target = noteToDelete!!
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Delete Note?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete \"${target.title}\"?\n\nThis removes its row from notes/notes.csv, unlinks related questions, and deletes the file if it exists. Logs will not be touched.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val n = noteToDelete
                        noteToDelete = null
                        if (n != null) {
                            scope.launch {
                                viewModel.deleteNote(n.noteId, n.filePath)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { noteToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddMenu) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddMenu = false },
            title = { Text("Notes Authoring", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                showAddMenu = false
                                showAddSheet = true
                            },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Create New Note", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Add note into current filter context", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                showAddMenu = false
                                csvPickerLauncher.launch(arrayOf("text/*", "text/comma-separated-values", "application/csv"))
                            },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Import notes.csv", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Merge external CSV (auto-remaps collisions)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { showAddMenu = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddSheet) {
        AddNoteSheet(
            lockedFilter = notesFilter,
            viewModel = viewModel,
            onDismiss = { showAddSheet = false }
        )
    }

    if (activeNote != null) {
        NoteReaderView(
            note = activeNote!!,
            content = activeNoteContent,
            viewModel = viewModel,
            onClose = { openedAt, timeSpentSec ->
                viewModel.closeNote(activeNote!!, openedAt, timeSpentSec)
            },
            onTestQuestionsFromThisNote = {
                onStartTestSlice(TestSliceSource.NoteRevision(activeNote!!.noteId, activeNote!!.title))
            },
            onTestWholeChapter = {
                onStartTestSlice(
                    TestSliceSource.DrillDown(
                        DrillDownFilter(
                            exam = activeNote!!.exam,
                            subject = activeNote!!.subject,
                            chapter = activeNote!!.chapter
                        )
                    )
                )
            }
        )
    } else if (!showNotes) {
        DrillDownSelector(
            title = "Notes: Choose Exam or Subject",
            exams = viewModel.listExams(),
            currentFilter = notesFilter,
            getSubjects = { viewModel.listSubjects(it) },
            getChapters = { ex, sub -> viewModel.listChapters(ex, sub) },
            getTopics = { ex, sub, ch -> viewModel.listTopics(ex, sub, ch) },
            onFilterChanged = {
                viewModel.setNotesFilter(it)
                isSliceSelected = false
            },
            onSliceReady = {
                viewModel.setNotesFilter(it)
                isSliceSelected = true
            },
            onAddClicked = { showAddMenu = true }
        )
    } else {
        val notes = viewModel.notesForSlice(notesFilter)

        val sliceLabel = when {
            notesFilter.chapter != null -> "Chapter: ${notesFilter.chapter}"
            notesFilter.subject != null -> "Subject: ${notesFilter.subject} (All Chapters)"
            else -> "Exam: ${notesFilter.exam} (All Subjects)"
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sliceLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${notes.size} note(s) found in notes.csv",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularAddButton(
                            onClick = { showAddMenu = true },
                            contentDescription = "Add or Import Note"
                        )
                        OutlinedButton(
                            onClick = {
                                isSliceSelected = false
                                when {
                                    notesFilter.chapter != null -> viewModel.setNotesFilter(notesFilter.copy(chapter = null, topic = null))
                                    notesFilter.subject != null -> viewModel.setNotesFilter(notesFilter.copy(subject = null, chapter = null, topic = null))
                                    else -> viewModel.setNotesFilter(DrillDownFilter())
                                }
                            },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("Change Filter", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

                if (notes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No notes found matching this filter in notes.csv",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(notes) { note ->
                            NoteCard(
                                note = note,
                                onOpen = { viewModel.openNote(note) },
                                onTestNote = {
                                    onStartTestSlice(TestSliceSource.NoteRevision(note.noteId, note.title))
                                },
                                onTestChapter = {
                                    onStartTestSlice(
                                        TestSliceSource.DrillDown(
                                            DrillDownFilter(
                                                exam = note.exam,
                                                subject = note.subject,
                                                chapter = note.chapter
                                            )
                                        )
                                    )
                                },
                                onDelete = {
                                    noteToDelete = note
                                }
                            )
                        }
                    }
                }
            }
        }
    }

@Composable
fun NoteCard(
    note: NoteItem,
    onOpen: () -> Unit,
    onTestNote: () -> Unit,
    onTestChapter: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onOpen() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.background,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = "${note.exam} • ${note.chapter}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = note.noteType,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete note",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "File: ${note.filePath}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTestNote,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test from note", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onTestChapter,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Test chapter", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun NoteReaderView(
    note: NoteItem,
    content: String,
    viewModel: ExamPrepViewModel,
    onClose: (openedAt: Long, timeSpentSec: Int) -> Unit,
    onTestQuestionsFromThisNote: () -> Unit,
    onTestWholeChapter: () -> Unit
) {
    val openedAt = remember { System.currentTimeMillis() }
    var readSeconds by remember { mutableIntStateOf(0) }
    var showImportSheet by remember { mutableStateOf(false) }
    var showAddSingleSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            readSeconds++
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onClose(openedAt, readSeconds) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = note.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "${note.chapter} • ${readSeconds / 60}m ${readSeconds % 60}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                    IconButton(
                        onClick = { showImportSheet = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.UploadFile,
                            contentDescription = "Import questions.csv",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { showAddSingleSheet = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add single question",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    // Row 1: Question Actions (Import questions.csv & Add single question)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showImportSheet = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import questions.csv", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = { showAddSingleSheet = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add single question", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Row 2: Test Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                onClose(openedAt, readSeconds)
                                onTestQuestionsFromThisNote()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test this note", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                onClose(openedAt, readSeconds)
                                onTestWholeChapter()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("Test whole chapter", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            val isHtml = note.noteType.equals("html", ignoreCase = true) ||
                note.filePath.endsWith(".html", ignoreCase = true) ||
                note.filePath.endsWith(".htm", ignoreCase = true)
            val isPdf = note.noteType.equals("pdf", ignoreCase = true) ||
                note.filePath.endsWith(".pdf", ignoreCase = true)

            if (isHtml) {
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        android.webkit.WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            setBackgroundColor(android.graphics.Color.WHITE)
                            loadDataWithBaseURL(null, content, "text/html", "utf-8", null)
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL(null, content, "text/html", "utf-8", null)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (isPdf) {
                val mediaUri = remember(note.filePath) { viewModel.resolveMediaUri(note.filePath) }
                if (mediaUri != null) {
                    PdfViewerView(uri = mediaUri, modifier = Modifier.fillMaxSize())
                } else {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(content, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (showImportSheet) {
            LinkedQuestionImportSheet(
                targetType = "Note",
                targetTitle = note.title,
                targetId = note.noteId,
                viewModel = viewModel,
                onDismiss = { showImportSheet = false },
                onSuccess = { count ->
                    showImportSheet = false
                }
            )
        }

        if (showAddSingleSheet) {
            LinkedSingleQuestionSheet(
                targetType = "Note",
                targetTitle = note.title,
                targetId = note.noteId,
                viewModel = viewModel,
                onDismiss = { showAddSingleSheet = false },
                onSuccess = { qId ->
                    showAddSingleSheet = false
                }
            )
        }
    }
}

@Composable
fun PdfViewerView(uri: Uri, modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var pages by remember(uri) { mutableStateOf<List<Bitmap>>(emptyList()) }
    var errorMsg by remember(uri) { mutableStateOf<String?>(null) }
    var isLoading by remember(uri) { mutableStateOf(true) }

    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            try {
                val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                if (pfd != null) {
                    val renderer = android.graphics.pdf.PdfRenderer(pfd)
                    val list = mutableListOf<Bitmap>()
                    val pageCount = renderer.pageCount
                    for (i in 0 until pageCount) {
                        val page = renderer.openPage(i)
                        val w = (page.width * 1.5f).toInt().coerceAtLeast(720)
                        val h = (page.height * 1.5f).toInt().coerceAtLeast(1080)
                        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(bmp)
                        canvas.drawColor(android.graphics.Color.WHITE)
                        page.render(bmp, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()
                        list.add(bmp)
                    }
                    renderer.close()
                    pfd.close()
                    pages = list
                } else {
                    errorMsg = "Unable to open PDF"
                }
            } catch (e: Exception) {
                errorMsg = "Could not render PDF: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    if (isLoading) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(36.dp))
        }
    } else if (errorMsg != null) {
        Box(modifier = modifier.padding(16.dp), contentAlignment = Alignment.Center) {
            Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
        }
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(pages) { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "PDF Page",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
