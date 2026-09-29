package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.local.AppDatabase
import com.example.data.repository.UPSIRepository
import com.example.ui.components.AppTab
import com.example.ui.components.UPSIBottomBar
import com.example.ui.components.UPSITopBar
import com.example.ui.screens.AttemptsAnalyticsScreen
import com.example.ui.screens.CsvDataManagementScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LawGuideScreen
import com.example.ui.screens.MockTestExamScreen
import com.example.ui.screens.MockTestListScreen
import com.example.ui.screens.MockTestResultScreen
import com.example.ui.screens.NoteDetailReaderScreen
import com.example.ui.screens.NotesHubScreen
import com.example.ui.screens.PracticeScreen
import com.example.ui.screens.VideoHubScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.screens.WeeklyReviewsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.UPSIViewModel
import com.example.ui.viewmodel.UPSIViewModelFactory

enum class AppSubScreen {
    MAIN,
    EXAM,
    EXAM_RESULT,
    LAW_GUIDE,
    CSV_MANAGER,
    NOTE_READER,
    VIDEO_PLAYER
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = UPSIRepository(
            questionDao = database.questionDao(),
            attemptDao = database.attemptDao(),
            weeklyReviewDao = database.weeklyReviewDao(),
            mockTestResultDao = database.mockTestResultDao(),
            noteDao = database.noteDao(),
            videoDao = database.videoDao()
        )
        val factory = UPSIViewModelFactory(repository)

        setContent {
            val viewModel: UPSIViewModel by viewModels { factory }
            MyApplicationTheme {
                UPSIMainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun UPSIMainApp(viewModel: UPSIViewModel) {
    val isHindi by viewModel.isBilingualHindi.collectAsState()
    val selectedNote by viewModel.selectedNote.collectAsState()
    val selectedVideo by viewModel.selectedVideo.collectAsState()
    var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    var subScreen by remember { mutableStateOf(AppSubScreen.MAIN) }

    // Safe BackHandler for sub-screens
    BackHandler(enabled = subScreen != AppSubScreen.MAIN) {
        when (subScreen) {
            AppSubScreen.EXAM -> {
                // Handled internally in MockTestExamScreen
            }
            AppSubScreen.EXAM_RESULT -> {
                subScreen = AppSubScreen.MAIN
                currentTab = AppTab.MOCK_TESTS
            }
            AppSubScreen.LAW_GUIDE, AppSubScreen.CSV_MANAGER -> {
                subScreen = AppSubScreen.MAIN
            }
            AppSubScreen.NOTE_READER -> {
                subScreen = AppSubScreen.MAIN
                currentTab = AppTab.NOTES
            }
            AppSubScreen.VIDEO_PLAYER -> {
                subScreen = AppSubScreen.MAIN
                currentTab = AppTab.VIDEOS
            }
            AppSubScreen.MAIN -> {}
        }
    }

    // Safe BackHandler for secondary tabs back to Dashboard
    BackHandler(enabled = subScreen == AppSubScreen.MAIN && currentTab != AppTab.DASHBOARD) {
        currentTab = AppTab.DASHBOARD
    }

    when (subScreen) {
        AppSubScreen.EXAM -> {
            MockTestExamScreen(
                viewModel = viewModel,
                onTestFinished = {
                    subScreen = AppSubScreen.EXAM_RESULT
                },
                onCancelTest = {
                    subScreen = AppSubScreen.MAIN
                }
            )
        }

        AppSubScreen.EXAM_RESULT -> {
            MockTestResultScreen(
                viewModel = viewModel,
                onBackHome = {
                    subScreen = AppSubScreen.MAIN
                    currentTab = AppTab.DASHBOARD
                },
                onTakeAnotherTest = {
                    subScreen = AppSubScreen.MAIN
                    currentTab = AppTab.MOCK_TESTS
                }
            )
        }

        AppSubScreen.LAW_GUIDE -> {
            LawGuideScreen(
                onBack = { subScreen = AppSubScreen.MAIN },
                isHindi = isHindi
            )
        }

        AppSubScreen.CSV_MANAGER -> {
            CsvDataManagementScreen(
                viewModel = viewModel,
                onBack = { subScreen = AppSubScreen.MAIN }
            )
        }

        AppSubScreen.NOTE_READER -> {
            selectedNote?.let { note ->
                NoteDetailReaderScreen(
                    note = note,
                    viewModel = viewModel,
                    onBack = { subScreen = AppSubScreen.MAIN },
                    onPracticeLinkedQuestions = {
                        viewModel.setPracticeFilter(null)
                        subScreen = AppSubScreen.MAIN
                        currentTab = AppTab.PRACTICE
                    }
                )
            } ?: run {
                subScreen = AppSubScreen.MAIN
            }
        }

        AppSubScreen.VIDEO_PLAYER -> {
            selectedVideo?.let { video ->
                VideoPlayerScreen(
                    video = video,
                    viewModel = viewModel,
                    onBack = { subScreen = AppSubScreen.MAIN },
                    onPracticeLinkedQuestions = {
                        viewModel.setPracticeFilter(null)
                        subScreen = AppSubScreen.MAIN
                        currentTab = AppTab.PRACTICE
                    }
                )
            } ?: run {
                subScreen = AppSubScreen.MAIN
            }
        }

        AppSubScreen.MAIN -> {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    UPSITopBar(
                        isHindi = isHindi,
                        onToggleLanguage = { viewModel.toggleLanguage() },
                        onOpenCsvManager = { subScreen = AppSubScreen.CSV_MANAGER },
                        onOpenLawGuide = { subScreen = AppSubScreen.LAW_GUIDE }
                    )
                },
                bottomBar = {
                    UPSIBottomBar(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it },
                        isHindi = isHindi
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        AppTab.DASHBOARD -> {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateTab = { tab -> currentTab = tab },
                                onStartSubjectPractice = { subject ->
                                    viewModel.setPracticeFilter(subject)
                                    currentTab = AppTab.PRACTICE
                                },
                                onOpenLawHandbook = { subScreen = AppSubScreen.LAW_GUIDE }
                            )
                        }

                        AppTab.PRACTICE -> {
                            PracticeScreen(viewModel = viewModel)
                        }

                        AppTab.NOTES -> {
                            NotesHubScreen(
                                viewModel = viewModel,
                                onOpenNoteReader = { note ->
                                    viewModel.selectNote(note)
                                    subScreen = AppSubScreen.NOTE_READER
                                },
                                onPracticeLinkedQuestions = {
                                    viewModel.setPracticeFilter(null)
                                    currentTab = AppTab.PRACTICE
                                }
                            )
                        }

                        AppTab.VIDEOS -> {
                            VideoHubScreen(
                                viewModel = viewModel,
                                onOpenVideoPlayer = { video ->
                                    viewModel.selectVideo(video)
                                    subScreen = AppSubScreen.VIDEO_PLAYER
                                },
                                onPracticeLinkedQuestions = {
                                    viewModel.setPracticeFilter(null)
                                    currentTab = AppTab.PRACTICE
                                }
                            )
                        }

                        AppTab.MOCK_TESTS -> {
                            MockTestListScreen(
                                viewModel = viewModel,
                                onStartTest = { config ->
                                    viewModel.startMockTest(config)
                                    subScreen = AppSubScreen.EXAM
                                }
                            )
                        }

                        AppTab.ANALYTICS -> {
                            AttemptsAnalyticsScreen(
                                viewModel = viewModel,
                                onPracticeQuestion = { _ ->
                                    viewModel.setPracticeFilter(null)
                                    currentTab = AppTab.PRACTICE
                                }
                            )
                        }

                        AppTab.WEEKLY_REVIEWS -> {
                            WeeklyReviewsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
