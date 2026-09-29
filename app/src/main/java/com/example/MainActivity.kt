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
import com.example.ui.components.AppTab
import com.example.ui.components.DrillDownSelector
import com.example.ui.components.ExamPrepBottomBar
import com.example.ui.components.ExamPrepTopBar
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
    var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    val isFolderLinked by viewModel.isFolderLinked.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val activeTestQuestions by viewModel.activeTestQuestions.collectAsState()
    val activeTestSource by viewModel.activeTestSource.collectAsState()
    val testFilter by viewModel.testFilter.collectAsState()
    val scope = rememberCoroutineScope()

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
                    }
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
                        // Gap 5 & 6: Shared drill-down to pick exam/subject/chapter for Test slice
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
                            }
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
