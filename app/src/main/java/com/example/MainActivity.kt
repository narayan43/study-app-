package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
            MyApplicationTheme {
                ExamPrepMainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ExamPrepMainApp(viewModel: ExamPrepViewModel) {
    var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    val isFolderLinked by viewModel.isFolderLinked.collectAsState()
    val activeTestQuestions by viewModel.activeTestQuestions.collectAsState()
    val drillFilter by viewModel.drillFilter.collectAsState()
    val scope = rememberCoroutineScope()

    // SAF folder picker launcher
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            viewModel.linkDataFolder(uri)
        }
    }

    // Back handling: If active test is playing, return to current tab
    BackHandler(enabled = activeTestQuestions.isNotEmpty()) {
        viewModel.startTestSlice(TestSliceSource.DrillDown(drillFilter.copy(exam = null)))
        // Clears test questions
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
                    onPickFolder = {
                        folderPickerLauncher.launch(null)
                    },
                    onReloadData = {
                        scope.launch { viewModel.reloadData() }
                    }
                )
            }
        },
        bottomBar = {
            if (activeTestQuestions.isEmpty()) {
                ExamPrepBottomBar(
                    currentTab = currentTab,
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
                    onBack = {
                        viewModel.startTestSlice(TestSliceSource.DrillDown(drillFilter.copy(exam = null)))
                    },
                    onOpenNoteReader = { note ->
                        viewModel.openNote(note)
                        currentTab = AppTab.NOTES
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
                        DrillDownSelector(
                            title = "Test: Choose Exam & Chapter",
                            exams = viewModel.listExams(),
                            currentFilter = drillFilter,
                            getSubjects = { viewModel.listSubjects(it) },
                            getChapters = { ex, sub -> viewModel.listChapters(ex, sub) },
                            getTopics = { ex, sub, ch -> viewModel.listTopics(ex, sub, ch) },
                            onFilterChanged = { viewModel.setFilter(it) },
                            onSliceReady = { filter ->
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
                                viewModel.setFilter(filter)
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
