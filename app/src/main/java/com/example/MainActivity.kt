package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.model.DrillDownFilter
import com.example.data.model.TestSliceSource
import com.example.data.repository.DataService
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.example.data.model.QuestionItem
import com.example.ui.components.AddContentSheetChrome
import com.example.ui.components.AddQuestionSheet
import com.example.ui.components.AppTab
import com.example.ui.components.DrillDownSelector
import com.example.ui.components.ExamPrepBottomBar
import com.example.ui.components.ExamPrepTopBar
import com.example.ui.components.UpdateDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MistakesScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.ReelsScreen
import com.example.ui.screens.TestPlayerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ExamPrepViewModel
import com.example.ui.viewmodel.ExamPrepViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dataService = DataService(applicationContext)
        val factory = ExamPrepViewModelFactory(dataService)

        setContent {
            val viewModel: ExamPrepViewModel by viewModels { factory }
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                ExamPrepMainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ExamPrepMainApp(viewModel: ExamPrepViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    val isFolderLinked by viewModel.isFolderLinked.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val activeTestQuestions by viewModel.activeTestQuestions.collectAsState()
    val activeTestSource by viewModel.activeTestSource.collectAsState()
    val testFilter by viewModel.testFilter.collectAsState()
    val scope = rememberCoroutineScope()

    var showAddTestQuestionSheet by remember { mutableStateOf(false) }
    var showQuestionAuthoringMenu by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    val questionCsvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    if (!viewModel.isFolderLinked.value) {
                        Toast.makeText(context, "No Data folder linked. Please link a Data folder first.", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                    val count = viewModel.importAndMergeQuestionsCsv(uri, overrideFilter = testFilter)
                    Toast.makeText(context, "Moved to Data/questions/ & merged $count questions into questions.csv", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val questionZipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val (count, remapped) = viewModel.importQuestionsFromZip(uri, overrideFilter = testFilter)
                    val msg = if (remapped > 0) {
                        "Imported $count questions from ZIP ($remapped remapped)"
                    } else {
                        "Imported $count questions from ZIP"
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error importing ZIP: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    if (showQuestionAuthoringMenu) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showQuestionAuthoringMenu = false },
            title = { Text("Questions Authoring & Import", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                showQuestionAuthoringMenu = false
                                showAddTestQuestionSheet = true
                            },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Create Single Question", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Enter question, 4 options, and optional links", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                showQuestionAuthoringMenu = false
                                questionCsvPickerLauncher.launch(arrayOf("text/*", "text/comma-separated-values", "application/csv"))
                            },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Import questions.csv", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Validate header & auto-remap colliding IDs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                showQuestionAuthoringMenu = false
                                questionZipPickerLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream"))
                            },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Import ZIP (CSV + Images)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Extract questions.csv and questions/images/", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { showQuestionAuthoringMenu = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddTestQuestionSheet) {
        AddQuestionSheet(
            lockedFilter = DrillDownFilter(),
            initialFilter = testFilter,
            viewModel = viewModel,
            onDismiss = { showAddTestQuestionSheet = false }
        )
    }

    if (showUpdateDialog) {
        UpdateDialog(
            onDismiss = { showUpdateDialog = false }
        )
    }

    // SAF folder picker launcher
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            viewModel.linkDataFolder(uri)
        }
    }

    // Gap 6: Handle Back button from active test slice
    BackHandler(enabled = activeTestQuestions.isNotEmpty()) {
        val src = activeTestSource
        viewModel.clearActiveTest()
        when (src) {
            is TestSliceSource.NoteRevision -> currentTab = AppTab.NOTES
            is TestSliceSource.VideoRevision -> currentTab = AppTab.REELS
            is TestSliceSource.MistakesRetest -> currentTab = AppTab.MISTAKES
            is TestSliceSource.DrillDown -> currentTab = AppTab.TEST
            is TestSliceSource.DashboardSlice -> currentTab = AppTab.DASHBOARD
            null -> {}
        }
    }

    // Back handling: Secondary tabs back to Dashboard
    BackHandler(enabled = activeTestQuestions.isEmpty() && currentTab != AppTab.DASHBOARD) {
        currentTab = AppTab.DASHBOARD
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (activeTestQuestions.isEmpty()) {
                ExamPrepTopBar(
                    isFolderLinked = isFolderLinked,
                    isDarkTheme = isDarkTheme,
                    onPickFolder = {
                        folderPickerLauncher.launch(null)
                    },
                    onReloadData = {
                        scope.launch { viewModel.reloadData() }
                    },
                    onToggleTheme = {
                        viewModel.toggleTheme()
                    },
                    onCheckUpdate = { showUpdateDialog = true }
                )
            }
        },
        bottomBar = {
            if (activeTestQuestions.isEmpty()) {
                ExamPrepBottomBar(
                    currentTab = currentTab,
                    isDarkTheme = isDarkTheme,
                    onTabSelected = { currentTab = it }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // When an active test slice is running, present the Test Player immediately
            if (activeTestQuestions.isNotEmpty()) {
                TestPlayerScreen(
                    viewModel = viewModel,
                    onBack = { src ->
                        viewModel.clearActiveTest()
                        when (src) {
                            is TestSliceSource.NoteRevision -> currentTab = AppTab.NOTES
                            is TestSliceSource.VideoRevision -> currentTab = AppTab.REELS
                            is TestSliceSource.MistakesRetest -> currentTab = AppTab.MISTAKES
                            is TestSliceSource.DrillDown -> currentTab = AppTab.TEST
                            is TestSliceSource.DashboardSlice -> currentTab = AppTab.DASHBOARD
                            null -> {}
                        }
                    },
                    onOpenNoteReader = { note ->
                        viewModel.clearActiveTest()
                        viewModel.openNote(note)
                        currentTab = AppTab.NOTES
                    },
                    onOpenReel = { video ->
                        viewModel.clearActiveTest()
                        viewModel.setReelsFilter(
                            DrillDownFilter(exam = video.exam, subject = video.subject, chapter = video.chapter)
                        )
                        currentTab = AppTab.REELS
                    }
                )
            } else {
                when (currentTab) {
                    AppTab.DASHBOARD -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            onPickFolder = { folderPickerLauncher.launch(null) }
                        )
                    }

                    AppTab.TEST -> {
                        // Shared drill-down to pick exam/subject/chapter for Test slice
                        BackHandler(enabled = testFilter.exam != null) {
                            when {
                                testFilter.chapter != null -> viewModel.setTestFilter(testFilter.copy(chapter = null, topic = null))
                                testFilter.subject != null -> viewModel.setTestFilter(testFilter.copy(subject = null, chapter = null, topic = null))
                                else -> viewModel.setTestFilter(DrillDownFilter())
                            }
                        }
                        DrillDownSelector(
                            title = "Test: Choose Exam or Subject",
                            exams = viewModel.listExams(),
                            currentFilter = testFilter,
                            getSubjects = { viewModel.listSubjects(it) },
                            getChapters = { ex, sub -> viewModel.listChapters(ex, sub) },
                            getTopics = { ex, sub, ch -> viewModel.listTopics(ex, sub, ch) },
                            onFilterChanged = { viewModel.setTestFilter(it) },
                            onSliceReady = { filter ->
                                viewModel.setTestFilter(filter)
                                viewModel.startTestSlice(TestSliceSource.DrillDown(filter))
                            },
                            onAddClicked = { showQuestionAuthoringMenu = true }
                        )
                    }

                    AppTab.NOTES -> {
                        NotesScreen(
                            viewModel = viewModel,
                            onStartTestSlice = { source ->
                                viewModel.startTestSlice(source)
                            }
                        )
                    }

                    AppTab.MISTAKES -> {
                        MistakesScreen(
                            viewModel = viewModel,
                            onStartTestSlice = { source ->
                                viewModel.startTestSlice(source)
                            },
                            onOpenNotesForChapter = { filter ->
                                viewModel.setNotesFilter(filter)
                                currentTab = AppTab.NOTES
                            }
                        )
                    }

                    AppTab.REELS -> {
                        ReelsScreen(
                            viewModel = viewModel,
                            onStartTestSlice = { source ->
                                viewModel.startTestSlice(source)
                            }
                        )
                    }
                }
            }
        }
    }
}
