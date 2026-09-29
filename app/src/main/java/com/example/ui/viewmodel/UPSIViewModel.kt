package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.csv.CsvHelper
import com.example.data.model.AttemptEntity
import com.example.data.model.MockQuestionState
import com.example.data.model.MockTestConfig
import com.example.data.model.MockTestResultEntity
import com.example.data.model.NoteEntity
import com.example.data.model.NoteUsageEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.VideoEntity
import com.example.data.model.VideoUsageEntity
import com.example.data.model.WeeklyReviewEntity
import com.example.data.repository.UPSIRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UPSIViewModel(
    private val repository: UPSIRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.initializePreloadedDataIfNeeded()
        }
    }

    val questions: StateFlow<List<QuestionEntity>> = repository.allQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attempts: StateFlow<List<AttemptEntity>> = repository.allAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyReviews: StateFlow<List<WeeklyReviewEntity>> = repository.allWeeklyReviews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mockTestResults: StateFlow<List<MockTestResultEntity>> = repository.allMockTestResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incorrectQuestionIds: StateFlow<List<String>> = repository.incorrectQuestionIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val videos: StateFlow<List<VideoEntity>> = repository.allVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val noteUsages: StateFlow<List<NoteUsageEntity>> = repository.allNoteUsages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val videoUsages: StateFlow<List<VideoUsageEntity>> = repository.allVideoUsages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedNote = MutableStateFlow<NoteEntity?>(null)
    val selectedNote: StateFlow<NoteEntity?> = _selectedNote.asStateFlow()

    private val _selectedVideo = MutableStateFlow<VideoEntity?>(null)
    val selectedVideo: StateFlow<VideoEntity?> = _selectedVideo.asStateFlow()

    private val _linkedQuestionsForNote = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val linkedQuestionsForNote: StateFlow<List<QuestionEntity>> = _linkedQuestionsForNote.asStateFlow()

    private val _linkedQuestionsForVideo = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val linkedQuestionsForVideo: StateFlow<List<QuestionEntity>> = _linkedQuestionsForVideo.asStateFlow()

    fun selectNote(note: NoteEntity?) {
        _selectedNote.value = note
        if (note != null) {
            viewModelScope.launch {
                val qIds = repository.getQuestionIdsForNote(note.id)
                _linkedQuestionsForNote.value = questions.value.filter { it.id in qIds }
            }
        } else {
            _linkedQuestionsForNote.value = emptyList()
        }
    }

    fun selectVideo(video: VideoEntity?) {
        _selectedVideo.value = video
        if (video != null) {
            viewModelScope.launch {
                val qIds = repository.getQuestionIdsForVideo(video.id)
                _linkedQuestionsForVideo.value = questions.value.filter { it.id in qIds }
            }
        } else {
            _linkedQuestionsForVideo.value = emptyList()
        }
    }

    fun toggleNoteBookmark(note: NoteEntity) {
        viewModelScope.launch {
            repository.toggleNoteBookmark(note)
        }
    }

    fun recordNoteUsage(noteId: String, timeSpentSec: Int, completed: Boolean) {
        viewModelScope.launch {
            repository.recordNoteUsage(noteId, timeSpentSec, completed)
        }
    }

    fun recordVideoUsage(videoId: String, watchTimeSec: Int, lastPositionSec: Int, completed: Boolean) {
        viewModelScope.launch {
            repository.recordVideoUsage(videoId, watchTimeSec, lastPositionSec, completed)
        }
    }

    suspend fun getNoteIdsForQuestion(questionId: String): List<String> {
        return repository.getNoteIdsForQuestion(questionId)
    }

    suspend fun getVideoIdsForQuestion(questionId: String): List<String> {
        return repository.getVideoIdsForQuestion(questionId)
    }

    fun getNotesCsvExport(): String = CsvHelper.exportNotesToCsv(notes.value)
    fun getVideosCsvExport(): String = CsvHelper.exportVideosToCsv(videos.value)

    fun importNotesFromCsv(csvText: String): Int {
        val parsed = CsvHelper.parseNotesFromCsv(csvText)
        if (parsed.isNotEmpty()) {
            viewModelScope.launch { repository.insertImportedNotes(parsed) }
        }
        return parsed.size
    }

    fun importVideosFromCsv(csvText: String): Int {
        val parsed = CsvHelper.parseVideosFromCsv(csvText)
        if (parsed.isNotEmpty()) {
            viewModelScope.launch { repository.insertImportedVideos(parsed) }
        }
        return parsed.size
    }

    fun importNoteQuestionsFromCsv(csvText: String): Int {
        val parsed = CsvHelper.parseNoteQuestionsFromCsv(csvText)
        if (parsed.isNotEmpty()) {
            viewModelScope.launch { repository.insertNoteQuestionLinks(parsed) }
        }
        return parsed.size
    }

    fun importVideoQuestionsFromCsv(csvText: String): Int {
        val parsed = CsvHelper.parseVideoQuestionsFromCsv(csvText)
        if (parsed.isNotEmpty()) {
            viewModelScope.launch { repository.insertVideoQuestionLinks(parsed) }
        }
        return parsed.size
    }

    // Language toggle: Hindi (Primary) or English
    private val _isBilingualHindi = MutableStateFlow(true)
    val isBilingualHindi: StateFlow<Boolean> = _isBilingualHindi.asStateFlow()

    fun toggleLanguage() {
        _isBilingualHindi.value = !_isBilingualHindi.value
    }

    // --- Practice State ---
    private val _practiceSubjectFilter = MutableStateFlow<String?>(null)
    val practiceSubjectFilter: StateFlow<String?> = _practiceSubjectFilter.asStateFlow()

    private val _practiceBookmarkOnly = MutableStateFlow(false)
    val practiceBookmarkOnly: StateFlow<Boolean> = _practiceBookmarkOnly.asStateFlow()

    private val _practiceMistakesOnly = MutableStateFlow(false)
    val practiceMistakesOnly: StateFlow<Boolean> = _practiceMistakesOnly.asStateFlow()

    fun setPracticeFilter(subject: String?, bookmarksOnly: Boolean = false, mistakesOnly: Boolean = false) {
        _practiceSubjectFilter.value = subject
        _practiceBookmarkOnly.value = bookmarksOnly
        _practiceMistakesOnly.value = mistakesOnly
    }

    val filteredPracticeQuestions: StateFlow<List<QuestionEntity>> = combine(
        questions,
        practiceSubjectFilter,
        practiceBookmarkOnly,
        practiceMistakesOnly,
        incorrectQuestionIds
    ) { all, subject, bookmarks, mistakes, incorrectIds ->
        var list = all
        if (subject != null) {
            list = list.filter { it.subject.equals(subject, ignoreCase = true) }
        }
        if (bookmarks) {
            list = list.filter { it.isBookmarked }
        }
        if (mistakes) {
            list = list.filter { incorrectIds.contains(it.id) }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Practice selections: questionId -> (selectedOption, isAnswerSubmitted)
    private val _practiceAnswers = MutableStateFlow<Map<String, Pair<String, Boolean>>>(emptyMap())
    val practiceAnswers: StateFlow<Map<String, Pair<String, Boolean>>> = _practiceAnswers.asStateFlow()

    fun selectPracticeAnswer(question: QuestionEntity, option: String) {
        val current = _practiceAnswers.value.toMutableMap()
        val isAlreadySubmitted = current[question.id]?.second ?: false
        if (!isAlreadySubmitted) {
            val isCorrect = option.equals(question.correctOption, ignoreCase = true)
            current[question.id] = Pair(option, true)
            _practiceAnswers.value = current

            // Record attempt
            viewModelScope.launch {
                repository.recordAttempt(
                    AttemptEntity(
                        questionId = question.id,
                        chosenOption = option,
                        isCorrect = isCorrect,
                        timeTakenSeconds = 15,
                        mode = "PRACTICE"
                    )
                )
            }
        }
    }

    fun toggleBookmark(question: QuestionEntity) {
        viewModelScope.launch {
            repository.toggleBookmark(question)
        }
    }

    // --- Mock Test State ---
    private val _activeMockConfig = MutableStateFlow<MockTestConfig?>(null)
    val activeMockConfig: StateFlow<MockTestConfig?> = _activeMockConfig.asStateFlow()

    private val _activeMockQuestions = MutableStateFlow<List<MockQuestionState>>(emptyList())
    val activeMockQuestions: StateFlow<List<MockQuestionState>> = _activeMockQuestions.asStateFlow()

    private val _currentMockIndex = MutableStateFlow(0)
    val currentMockIndex: StateFlow<Int> = _currentMockIndex.asStateFlow()

    private val _mockTimeRemainingSeconds = MutableStateFlow(0)
    val mockTimeRemainingSeconds: StateFlow<Int> = _mockTimeRemainingSeconds.asStateFlow()

    private var mockTimerJob: Job? = null

    private val _latestMockResult = MutableStateFlow<MockTestResultEntity?>(null)
    val latestMockResult: StateFlow<MockTestResultEntity?> = _latestMockResult.asStateFlow()

    fun startMockTest(config: MockTestConfig) {
        val allQ = questions.value
        val pool = if (config.subject != null) {
            allQ.filter { it.subject.equals(config.subject, ignoreCase = true) }
        } else {
            allQ
        }

        val testQuestions = if (pool.size <= config.questionCount) {
            pool.shuffled()
        } else {
            pool.shuffled().take(config.questionCount)
        }

        _activeMockConfig.value = config
        _activeMockQuestions.value = testQuestions.mapIndexed { index, q ->
            MockQuestionState(question = q, isVisited = (index == 0))
        }
        _currentMockIndex.value = 0
        _mockTimeRemainingSeconds.value = config.durationMinutes * 60

        mockTimerJob?.cancel()
        mockTimerJob = viewModelScope.launch {
            while (_mockTimeRemainingSeconds.value > 0) {
                delay(1000)
                _mockTimeRemainingSeconds.value -= 1
            }
            submitMockTest()
        }
    }

    fun goToMockQuestion(index: Int) {
        if (index in 0 until _activeMockQuestions.value.size) {
            _currentMockIndex.value = index
            val list = _activeMockQuestions.value.toMutableList()
            list[index] = list[index].copy(isVisited = true)
            _activeMockQuestions.value = list
        }
    }

    fun selectMockOption(option: String) {
        val index = _currentMockIndex.value
        val list = _activeMockQuestions.value.toMutableList()
        if (index in list.indices) {
            val current = list[index]
            val updated = if (current.selectedOption == option) {
                current.copy(selectedOption = null) // Deselect
            } else {
                current.copy(selectedOption = option)
            }
            list[index] = updated
            _activeMockQuestions.value = list
        }
    }

    fun toggleFlagCurrentMockQuestion() {
        val index = _currentMockIndex.value
        val list = _activeMockQuestions.value.toMutableList()
        if (index in list.indices) {
            val current = list[index]
            list[index] = current.copy(isFlagged = !current.isFlagged)
            _activeMockQuestions.value = list
        }
    }

    fun submitMockTest() {
        mockTimerJob?.cancel()
        val config = _activeMockConfig.value ?: return
        val list = _activeMockQuestions.value
        if (list.isEmpty()) return

        var correctCount = 0
        var incorrectCount = 0
        var unattemptedCount = 0

        val totalTimeSeconds = (config.durationMinutes * 60) - _mockTimeRemainingSeconds.value

        for (item in list) {
            val sel = item.selectedOption
            if (sel == null) {
                unattemptedCount++
            } else if (sel.equals(item.question.correctOption, ignoreCase = true)) {
                correctCount++
                viewModelScope.launch {
                    repository.recordAttempt(
                        AttemptEntity(
                            questionId = item.question.id,
                            chosenOption = sel,
                            isCorrect = true,
                            timeTakenSeconds = totalTimeSeconds / list.size.coerceAtLeast(1),
                            mode = "MOCK_TEST",
                            mockTestId = config.id
                        )
                    )
                }
            } else {
                incorrectCount++
                viewModelScope.launch {
                    repository.recordAttempt(
                        AttemptEntity(
                            questionId = item.question.id,
                            chosenOption = sel,
                            isCorrect = false,
                            timeTakenSeconds = totalTimeSeconds / list.size.coerceAtLeast(1),
                            mode = "MOCK_TEST",
                            mockTestId = config.id
                        )
                    )
                }
            }
        }

        val marksPerQuestion = config.totalMarks / config.questionCount
        val totalScore = (correctCount * marksPerQuestion).coerceAtLeast(0f)
        val accuracy = if (correctCount + incorrectCount > 0) {
            (correctCount.toFloat() / (correctCount + incorrectCount)) * 100f
        } else 0f

        // UPSI qualification requirement: 50% overall
        val isPassed = (totalScore / config.totalMarks) >= 0.50f

        val remarks = if (isPassed) {
            "शाबाश! आपने कट-ऑफ पार कर लिया है। शारीरिक दक्षता व रिवीजन जारी रखें।"
        } else {
            "कट-ऑफ से थोड़ा दूर। कमजोर विषयों (Mool Vidhi / Maths) का सघन अभ्यास करें।"
        }

        val result = MockTestResultEntity(
            id = "RES_${System.currentTimeMillis()}",
            title = config.title,
            timestamp = System.currentTimeMillis(),
            totalQuestions = config.questionCount,
            attemptedCount = correctCount + incorrectCount,
            correctCount = correctCount,
            incorrectCount = incorrectCount,
            unattemptedCount = unattemptedCount,
            score = totalScore,
            maxScore = config.totalMarks,
            accuracyPercent = accuracy,
            timeSpentSeconds = totalTimeSeconds,
            isPassed = isPassed,
            remarks = remarks
        )

        _latestMockResult.value = result
        viewModelScope.launch {
            repository.saveMockTestResult(result)
        }
    }

    // --- Weekly Reviews CRUD (weekly_reviews.csv) ---
    fun addOrUpdateWeeklyReview(
        id: Long = 0,
        weekStart: String,
        learned: String,
        willChange: String,
        proudOf: String
    ) {
        viewModelScope.launch {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val review = WeeklyReviewEntity(
                id = id,
                weekStart = weekStart,
                writtenOn = todayStr,
                learned = learned,
                willChange = willChange,
                proudOf = proudOf,
                questionsAttemptedCount = attempts.value.size,
                accuracyPercent = calculateOverallAccuracy()
            )
            repository.saveWeeklyReview(review)
        }
    }

    fun deleteWeeklyReview(review: WeeklyReviewEntity) {
        viewModelScope.launch {
            repository.deleteWeeklyReview(review)
        }
    }

    // --- Analytics helpers ---
    fun calculateOverallAccuracy(): Float {
        val list = attempts.value
        if (list.isEmpty()) return 0f
        val correct = list.count { it.isCorrect }
        return (correct.toFloat() / list.size) * 100f
    }

    fun getAccuracyBySubject(subject: String): Float {
        val qMap = questions.value.associateBy { it.id }
        val subjectAttempts = attempts.value.filter { qMap[it.questionId]?.subject?.equals(subject, ignoreCase = true) == true }
        if (subjectAttempts.isEmpty()) return 0f
        val correct = subjectAttempts.count { it.isCorrect }
        return (correct.toFloat() / subjectAttempts.size) * 100f
    }

    // --- CSV Import / Export Helpers ---
    fun getQuestionsCsvExport(): String {
        return CsvHelper.exportQuestionsToCsv(questions.value)
    }

    fun getAttemptsCsvExport(): String {
        return CsvHelper.exportAttemptsToCsv(attempts.value)
    }

    fun getWeeklyReviewsCsvExport(): String {
        return CsvHelper.exportWeeklyReviewsToCsv(weeklyReviews.value)
    }

    fun importQuestionsFromCsv(csvText: String): Int {
        val parsed = CsvHelper.parseQuestionsFromCsv(csvText)
        if (parsed.isNotEmpty()) {
            viewModelScope.launch {
                repository.insertImportedQuestions(parsed)
            }
        }
        return parsed.size
    }

    fun importWeeklyReviewsFromCsv(csvText: String): Int {
        val parsed = CsvHelper.parseWeeklyReviewsFromCsv(csvText)
        if (parsed.isNotEmpty()) {
            viewModelScope.launch {
                repository.insertImportedReviews(parsed)
            }
        }
        return parsed.size
    }
}

class UPSIViewModelFactory(
    private val repository: UPSIRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UPSIViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UPSIViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
