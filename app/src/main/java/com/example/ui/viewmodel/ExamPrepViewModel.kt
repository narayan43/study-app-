package com.example.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.DashboardStats
import com.example.data.model.DrillDownFilter
import com.example.data.model.NoteItem
import com.example.data.model.QuestionItem
import com.example.data.model.TestSliceSource
import com.example.data.model.VideoItem
import com.example.data.repository.DataService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExamPrepViewModel(
    private val dataService: DataService
) : ViewModel() {

    private val _drillFilter = MutableStateFlow(DrillDownFilter())
    val drillFilter: StateFlow<DrillDownFilter> = _drillFilter.asStateFlow()

    private val _dashboardStats = MutableStateFlow(DashboardStats())
    val dashboardStats: StateFlow<DashboardStats> = _dashboardStats.asStateFlow()

    private val _activeTestQuestions = MutableStateFlow<List<QuestionItem>>(emptyList())
    val activeTestQuestions: StateFlow<List<QuestionItem>> = _activeTestQuestions.asStateFlow()

    private val _activeTestSource = MutableStateFlow<TestSliceSource?>(null)
    val activeTestSource: StateFlow<TestSliceSource?> = _activeTestSource.asStateFlow()

    private val _activeNote = MutableStateFlow<NoteItem?>(null)
    val activeNote: StateFlow<NoteItem?> = _activeNote.asStateFlow()

    private val _activeNoteContent = MutableStateFlow("")
    val activeNoteContent: StateFlow<String> = _activeNoteContent.asStateFlow()

    private val _activeVideo = MutableStateFlow<VideoItem?>(null)
    val activeVideo: StateFlow<VideoItem?> = _activeVideo.asStateFlow()

    private val _folderPath = MutableStateFlow(dataService.getCurrentFolderDisplay())
    val folderPath: StateFlow<String> = _folderPath.asStateFlow()

    private val _isFolderLinked = MutableStateFlow(dataService.isFolderLinked())
    val isFolderLinked: StateFlow<Boolean> = _isFolderLinked.asStateFlow()

    init {
        viewModelScope.launch {
            reloadData()
        }
    }

    fun setFilter(filter: DrillDownFilter) {
        _drillFilter.value = filter
    }

    suspend fun reloadData() {
        dataService.reloadData()
        _dashboardStats.value = dataService.dashboardStats()
        _folderPath.value = dataService.getCurrentFolderDisplay()
        _isFolderLinked.value = dataService.isFolderLinked()
    }

    fun linkDataFolder(uri: Uri) {
        viewModelScope.launch {
            dataService.linkDataFolder(uri)
            reloadData()
        }
    }

    fun resetToDemoData() {
        viewModelScope.launch {
            dataService.resetToDefaultData()
            reloadData()
        }
    }

    fun listExams(): List<String> = dataService.listExams()
    fun listSubjects(exam: String?): List<String> = dataService.listSubjects(exam)
    fun listChapters(exam: String?, subject: String?): List<String> = dataService.listChapters(exam, subject)
    fun listTopics(exam: String?, subject: String?, chapter: String?): List<String> = dataService.listTopics(exam, subject, chapter)

    fun startTestSlice(source: TestSliceSource) {
        _activeTestSource.value = source
        val questions = when (source) {
            is TestSliceSource.DrillDown -> dataService.questionsFor(source.filter)
            is TestSliceSource.NoteRevision -> dataService.questionsForNote(source.noteId)
            is TestSliceSource.VideoRevision -> dataService.questionsForVideo(source.videoId)
            is TestSliceSource.MistakesRetest -> dataService.getQuestionsByIds(source.questionIds)
        }
        _activeTestQuestions.value = questions
    }

    fun submitAttempt(question: QuestionItem, chosenAnswer: String, isCorrect: Boolean, timeSpentSec: Int) {
        viewModelScope.launch {
            dataService.appendAttempt(question, chosenAnswer, isCorrect, timeSpentSec)
            _dashboardStats.value = dataService.dashboardStats()
        }
    }

    fun openNote(note: NoteItem) {
        _activeNote.value = note
        viewModelScope.launch {
            _activeNoteContent.value = dataService.readNoteContent(note.filePath)
        }
    }

    fun closeNote(note: NoteItem, openedAt: Long, timeSpentSec: Int) {
        viewModelScope.launch {
            dataService.appendNoteUsage(note, openedAt, System.currentTimeMillis(), timeSpentSec)
        }
        _activeNote.value = null
        _activeNoteContent.value = ""
    }

    fun openVideo(video: VideoItem) {
        _activeVideo.value = video
    }

    fun closeVideo(video: VideoItem, openedAt: Long, timeSpentSec: Int, startedTest: Boolean) {
        viewModelScope.launch {
            dataService.appendVideoUsage(video, openedAt, System.currentTimeMillis(), timeSpentSec, startedTest)
        }
        _activeVideo.value = null
    }

    fun notesForSlice(filter: DrillDownFilter): List<NoteItem> = dataService.notesFor(filter)
    fun videosForSlice(filter: DrillDownFilter): List<VideoItem> = dataService.videosFor(filter)
    fun notesForQuestion(questionId: String): List<NoteItem> = dataService.notesForQuestion(questionId)
    fun getMistakesGrouped(): Map<String, Map<String, Map<String, List<String>>>> = dataService.getMistakesGrouped()
}

class ExamPrepViewModelFactory(
    private val dataService: DataService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExamPrepViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExamPrepViewModel(dataService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
