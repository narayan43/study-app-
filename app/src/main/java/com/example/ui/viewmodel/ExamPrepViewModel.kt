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

    // Gap 6: Distinct filter per tab
    private val _testFilter = MutableStateFlow(DrillDownFilter())
    val testFilter: StateFlow<DrillDownFilter> = _testFilter.asStateFlow()

    private val _notesFilter = MutableStateFlow(DrillDownFilter())
    val notesFilter: StateFlow<DrillDownFilter> = _notesFilter.asStateFlow()

    private val _reelsFilter = MutableStateFlow(DrillDownFilter())
    val reelsFilter: StateFlow<DrillDownFilter> = _reelsFilter.asStateFlow()

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

    private val _isDarkTheme = MutableStateFlow(dataService.isDarkTheme())
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        val next = !_isDarkTheme.value
        _isDarkTheme.value = next
        dataService.setDarkTheme(next)
    }

    init {
        viewModelScope.launch {
            reloadData()
        }
    }

    fun setTestFilter(filter: DrillDownFilter) {
        _testFilter.value = filter
    }

    fun setNotesFilter(filter: DrillDownFilter) {
        _notesFilter.value = filter
    }

    fun setReelsFilter(filter: DrillDownFilter) {
        _reelsFilter.value = filter
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

    fun unlinkDataFolder() {
        viewModelScope.launch {
            dataService.unlinkFolder()
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
    fun listVideoTitles(exam: String?, subject: String?, chapter: String?, topic: String?): List<String> =
        dataService.listVideoTitles(exam, subject, chapter, topic)
    fun findCanonicalMatch(input: String, candidates: List<String>): String =
        dataService.findCanonicalMatch(input, candidates)

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

    fun clearActiveTest() {
        _activeTestQuestions.value = emptyList()
        _activeTestSource.value = null
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
    fun videosForQuestion(questionId: String): List<VideoItem> = dataService.videosForQuestion(questionId)
    fun getMistakesGrouped(): Map<String, Map<String, Map<String, List<String>>>> = dataService.getMistakesGrouped()
    fun resolveMediaUri(relPath: String): Uri? = dataService.resolveMediaUri(relPath)

    // Authoring APIs (Step 1)
    fun nextQuestionId(): String = dataService.nextQuestionId()
    fun nextNoteId(): String = dataService.nextNoteId()
    fun nextVideoId(): String = dataService.nextVideoId()

    suspend fun appendQuestion(item: QuestionItem) = dataService.appendQuestion(item)
    suspend fun appendNote(item: NoteItem, content: String? = null, sourceFileUri: Uri? = null) = dataService.appendNote(item, content, sourceFileUri)
    suspend fun appendVideo(item: VideoItem, sourceVideoUri: Uri? = null) = dataService.appendVideo(item, sourceVideoUri)

    suspend fun moveNoteFileToData(sourceUri: Uri): String {
        val destPath = dataService.movePickedFileIntoDataFolder(sourceUri, "notes/files")
        reloadData()
        return destPath
    }

    suspend fun moveVideoFileToData(sourceUri: Uri): String {
        val destPath = dataService.movePickedFileIntoDataFolder(sourceUri, "videos/files")
        reloadData()
        return destPath
    }

    suspend fun appendNoteRow(item: NoteItem) {
        dataService.appendNoteRow(item)
        reloadData()
    }

    suspend fun appendVideoRow(item: VideoItem) {
        dataService.appendVideoRow(item)
        reloadData()
    }

    fun writeNoteFileContent(relPath: String, content: String) {
        dataService.writeNoteFileContent(relPath, content)
    }

    suspend fun importAndMergeQuestionsCsv(
        sourceUri: Uri,
        linkedNoteId: String? = null,
        linkedVideoId: String? = null,
        overrideFilter: DrillDownFilter? = null
    ): Int {
        val noteId = linkedNoteId ?: activeNote.value?.noteId
        val videoId = linkedVideoId ?: activeVideo.value?.videoId
        val count = dataService.importAndMergeQuestionsCsv(sourceUri, noteId, videoId, overrideFilter)
        reloadData()
        return count
    }

    suspend fun deleteNote(noteId: String, filePath: String? = null) {
        dataService.deleteNote(noteId, filePath)
        if (_activeNote.value?.noteId.equals(noteId, ignoreCase = true)) {
            _activeNote.value = null
            _activeNoteContent.value = ""
        }
        reloadData()
    }

    suspend fun deleteVideo(videoId: String, videoPath: String? = null) {
        dataService.deleteVideo(videoId, videoPath)
        if (_activeVideo.value?.videoId.equals(videoId, ignoreCase = true)) {
            _activeVideo.value = null
        }
        reloadData()
    }

    suspend fun deleteQuestion(questionId: String) {
        dataService.deleteQuestion(questionId)
        _activeTestQuestions.value = _activeTestQuestions.value.filterNot { it.questionId.equals(questionId, ignoreCase = true) }
        reloadData()
    }

    suspend fun appendNoteLink(noteId: String, questionId: String) = dataService.appendNoteLink(noteId, questionId)
    suspend fun appendVideoLink(videoId: String, questionId: String) = dataService.appendVideoLink(videoId, questionId)
    fun remapIncomingQuestionIds(rows: List<QuestionItem>) = dataService.remapIncomingQuestionIds(rows)
    fun remapIncomingNoteIds(rows: List<NoteItem>) = dataService.remapIncomingNoteIds(rows)
    suspend fun appendNotesBulk(notes: List<NoteItem>) = dataService.appendNotesBulk(notes)
    suspend fun appendQuestionsBulk(questions: List<QuestionItem>) = dataService.appendQuestionsBulk(questions)
    suspend fun importQuestionsFromCsv(csvText: String): Pair<Int, Int> = dataService.importQuestionsFromCsv(csvText)
    suspend fun importQuestionsFromZip(zipUri: Uri, overrideFilter: DrillDownFilter? = null): Pair<Int, Int> {
        val res = dataService.importQuestionsFromZip(zipUri, overrideFilter)
        reloadData()
        return res
    }
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
