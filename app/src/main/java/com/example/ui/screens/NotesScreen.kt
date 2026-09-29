package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Description
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
    val drillFilter by viewModel.drillFilter.collectAsState()
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
        // Shared drill-down or list of notes for current slice
        val notes = viewModel.notesForSlice(drillFilter)
        if (drillFilter.exam == null || drillFilter.subject == null || drillFilter.chapter == null) {
            DrillDownSelector(
                title = "Notes: Choose Exam & Chapter",
                exams = viewModel.listExams(),
                currentFilter = drillFilter,
                getSubjects = { viewModel.listSubjects(it) },
                getChapters = { ex, sub -> viewModel.listChapters(ex, sub) },
                getTopics = { ex, sub, ch -> viewModel.listTopics(ex, sub, ch) },
                onFilterChanged = { viewModel.setFilter(it) },
                onSliceReady = { viewModel.setFilter(it) }
            )
        } else {
            // Notes list for current slice
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Notes: ${drillFilter.chapter}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D47A1)
                            )
                            Text(
                                text = "${drillFilter.exam} • ${drillFilter.subject}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.setFilter(drillFilter.copy(chapter = null))
                            }
                        ) {
                            Text("Change")
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
                            text = "No notes found for this chapter in notes.csv",
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE3F2FD))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = note.chapter,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1)
                    )
                }

                Text(
                    text = note.noteType,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
                ) {
                    Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test from note", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onTestChapter,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Test whole chapter", fontSize = 12.sp)
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
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${note.chapter} • ${readSeconds / 60}m ${readSeconds % 60}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFBBDEFB)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onClose(openedAt, readSeconds) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onClose(openedAt, readSeconds)
                            onTestQuestionsFromThisNote()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test this note")
                    }

                    OutlinedButton(
                        onClick = {
                            onClose(openedAt, readSeconds)
                            onTestWholeChapter()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Test chapter")
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
