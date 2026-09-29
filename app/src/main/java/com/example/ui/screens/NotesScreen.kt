package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Quiz
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DrillDownFilter
import com.example.data.model.NoteItem
import com.example.data.model.TestSliceSource
import com.example.ui.components.DrillDownSelector
import com.example.ui.viewmodel.ExamPrepViewModel
import kotlinx.coroutines.delay

@Composable
fun NotesScreen(
    viewModel: ExamPrepViewModel,
    onStartTestSlice: (TestSliceSource) -> Unit
) {
    val notesFilter by viewModel.notesFilter.collectAsState()
    val activeNote by viewModel.activeNote.collectAsState()
    val activeNoteContent by viewModel.activeNoteContent.collectAsState()

    if (activeNote != null) {
        NoteReaderView(
            note = activeNote!!,
            content = activeNoteContent,
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
    } else {
        if (notesFilter.exam == null) {
            DrillDownSelector(
                title = "Notes: Choose Exam or Subject",
                exams = viewModel.listExams(),
                currentFilter = notesFilter,
                getSubjects = { viewModel.listSubjects(it) },
                getChapters = { ex, sub -> viewModel.listChapters(ex, sub) },
                getTopics = { ex, sub, ch -> viewModel.listTopics(ex, sub, ch) },
                onFilterChanged = { viewModel.setNotesFilter(it) },
                onSliceReady = { viewModel.setNotesFilter(it) }
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

                        OutlinedButton(
                            onClick = {
                                viewModel.setNotesFilter(DrillDownFilter())
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
                                }
                            )
                        }
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
    onTestChapter: () -> Unit
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

                Text(
                    text = note.noteType,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteReaderView(
    note: NoteItem,
    content: String,
    onClose: (openedAt: Long, timeSpentSec: Int) -> Unit,
    onTestQuestionsFromThisNote: () -> Unit,
    onTestWholeChapter: () -> Unit
) {
    val openedAt = remember { System.currentTimeMillis() }
    var readSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            readSeconds++
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = note.title,
                            maxLines = 1,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "${note.chapter} • ${readSeconds / 60}m ${readSeconds % 60}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onClose(openedAt, readSeconds) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onClose(openedAt, readSeconds)
                            onTestQuestionsFromThisNote()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test this note", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            onClose(openedAt, readSeconds)
                            onTestWholeChapter()
                        },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Test whole chapter", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
