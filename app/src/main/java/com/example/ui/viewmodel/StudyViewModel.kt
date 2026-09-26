package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.JarvisDatabase
import com.example.data.model.ExamCountdownEntity
import com.example.data.model.ScheduleItemEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.StudyTaskEntity
import com.example.data.model.SubjectEntity
import com.example.data.repository.JarvisStudyRepository
import com.example.ui.theme.PsychologyTheme
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TimerMode(val title: String, val workMinutes: Int, val breakMinutes: Int) {
    POMODORO("Pomodoro", 25, 5),
    DEEP_FOCUS("Deep Focus", 50, 10),
    SPRINT("Quick Sprint", 15, 3),
    STOPWATCH("Stopwatch", 0, 0)
}

enum class TimerState {
    IDLE, RUNNING, PAUSED, COMPLETED
}

data class DayStudyMetric(
    val dayLabel: String,
    val dayNumber: Int,
    val totalMinutes: Int,
    val isToday: Boolean
)

data class GeneratedStudyPlan(
    val title: String,
    val subject: String,
    val totalDays: Int,
    val estimatedHours: Float,
    val steps: List<PlanStep>
)

data class PlanStep(
    val dayNumber: Int,
    val phase: String,
    val focusArea: String,
    val recommendedMinutes: Int,
    val activeRecallTip: String
)

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: JarvisStudyRepository

    init {
        val db = JarvisDatabase.getInstance(application)
        repository = JarvisStudyRepository(db.jarvisStudyDao())
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
    }

    // --- Psychology Theme Engine ---
    private val _psychologyTheme = MutableStateFlow(PsychologyTheme.COGNITIVE_BLUE)
    val psychologyTheme: StateFlow<PsychologyTheme> = _psychologyTheme.asStateFlow()

    fun setPsychologyTheme(theme: PsychologyTheme) {
        _psychologyTheme.value = theme
    }

    // --- Creator & Security Integrity ---
    val creatorCredit = "Madhav Patare, Founder of royal-pride (The Builder)"

    fun getJarvisIdentityResponse(): String {
        return "I am Jarvis Study, founded and created by Madhav Patare, Founder of royal-pride (The Builder). My mission is to empower your learning with active cognitive psychology themes, optimized study schedules, and absolute local device data privacy."
    }

    // --- Core Data Flows ---
    val subjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSchedule: StateFlow<List<ScheduleItemEntity>> = repository.allScheduleItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<StudyTaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<StudySessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exams: StateFlow<List<ExamCountdownEntity>> = repository.allExams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current calendar day (1=Mon, ..., 7=Sun)
    private val currentDayOfWeek: Int = run {
        val cal = Calendar.getInstance()
        when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    private val _selectedDay = MutableStateFlow(currentDayOfWeek)
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    fun selectDay(day: Int) {
        _selectedDay.value = day
    }

    // Filtered schedule for currently selected day
    val selectedDaySchedule: StateFlow<List<ScheduleItemEntity>> = combine(allSchedule, _selectedDay) { scheduleList, day ->
        scheduleList.filter { it.dayOfWeek == day }.sortedBy { it.startTime }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Study Metrics & Analytics ---
    val todayStudyMinutes: StateFlow<Int> = sessions.combine(_selectedDay) { sessionList, _ ->
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        sessionList.filter { it.timestamp >= todayStart }.sumOf { it.durationMinutes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val currentStreakDays: StateFlow<Int> = sessions.combine(_selectedDay) { sessionList, _ ->
        calculateStreak(sessionList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    // Last 7 days metrics for chart
    val weeklyMetrics: StateFlow<List<DayStudyMetric>> = sessions.combine(_selectedDay) { sessionList, _ ->
        calculateWeeklyMetrics(sessionList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Focus Timer Engine ---
    private val _timerMode = MutableStateFlow(TimerMode.POMODORO)
    val timerMode: StateFlow<TimerMode> = _timerMode.asStateFlow()

    private val _timerState = MutableStateFlow(TimerState.IDLE)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _isBreak = MutableStateFlow(false)
    val isBreak: StateFlow<Boolean> = _isBreak.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(TimerMode.POMODORO.workMinutes * 60)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _totalDurationSeconds = MutableStateFlow(TimerMode.POMODORO.workMinutes * 60)
    val totalDurationSeconds: StateFlow<Int> = _totalDurationSeconds.asStateFlow()

    private val _timerSubject = MutableStateFlow("Data Structures & Algorithms")
    val timerSubject: StateFlow<String> = _timerSubject.asStateFlow()

    private val _stopwatchSeconds = MutableStateFlow(0)
    val stopwatchSeconds: StateFlow<Int> = _stopwatchSeconds.asStateFlow()

    private var timerJob: Job? = null

    fun setTimerMode(mode: TimerMode) {
        if (_timerState.value == TimerState.RUNNING) return
        _timerMode.value = mode
        _isBreak.value = false
        if (mode == TimerMode.STOPWATCH) {
            _stopwatchSeconds.value = 0
            _remainingSeconds.value = 0
            _totalDurationSeconds.value = 0
        } else {
            val totalSec = mode.workMinutes * 60
            _remainingSeconds.value = totalSec
            _totalDurationSeconds.value = totalSec
        }
        _timerState.value = TimerState.IDLE
    }

    fun setTimerSubject(subject: String) {
        _timerSubject.value = subject
    }

    fun startTimer() {
        if (_timerState.value == TimerState.RUNNING) return
        _timerState.value = TimerState.RUNNING

        timerJob = viewModelScope.launch {
            while (_timerState.value == TimerState.RUNNING) {
                delay(1000)
                if (_timerMode.value == TimerMode.STOPWATCH) {
                    _stopwatchSeconds.value += 1
                } else {
                    if (_remainingSeconds.value > 1) {
                        _remainingSeconds.value -= 1
                    } else {
                        // Phase finished
                        _remainingSeconds.value = 0
                        triggerVibration()
                        if (!_isBreak.value) {
                            // Switch to break phase
                            _isBreak.value = true
                            val breakSec = _timerMode.value.breakMinutes * 60
                            _remainingSeconds.value = breakSec
                            _totalDurationSeconds.value = breakSec
                        } else {
                            // Break finished
                            _isBreak.value = false
                            val workSec = _timerMode.value.workMinutes * 60
                            _remainingSeconds.value = workSec
                            _totalDurationSeconds.value = workSec
                            _timerState.value = TimerState.COMPLETED
                            break
                        }
                    }
                }
            }
        }
    }

    fun pauseTimer() {
        if (_timerState.value == TimerState.RUNNING) {
            _timerState.value = TimerState.PAUSED
            timerJob?.cancel()
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timerState.value = TimerState.IDLE
        _isBreak.value = false
        if (_timerMode.value == TimerMode.STOPWATCH) {
            _stopwatchSeconds.value = 0
        } else {
            val totalSec = _timerMode.value.workMinutes * 60
            _remainingSeconds.value = totalSec
            _totalDurationSeconds.value = totalSec
        }
    }

    fun adjustTimerSeconds(deltaSeconds: Int) {
        if (_timerMode.value == TimerMode.STOPWATCH) return
        val newRemaining = maxOf(60, _remainingSeconds.value + deltaSeconds)
        _remainingSeconds.value = newRemaining
        if (newRemaining > _totalDurationSeconds.value) {
            _totalDurationSeconds.value = newRemaining
        }
    }

    fun logCompletedSession(notes: String = "", rating: Int = 5) {
        val durationMins = if (_timerMode.value == TimerMode.STOPWATCH) {
            maxOf(1, _stopwatchSeconds.value / 60)
        } else {
            _timerMode.value.workMinutes
        }

        viewModelScope.launch {
            repository.insertSession(
                StudySessionEntity(
                    subjectName = _timerSubject.value,
                    durationMinutes = durationMins,
                    timestamp = System.currentTimeMillis(),
                    studyMode = _timerMode.value.title,
                    notes = notes,
                    productivityRating = rating
                )
            )
            resetTimer()
        }
    }

    private fun triggerVibration() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(400)
            }
        } catch (_: Exception) {}
    }

    // --- Task Actions ---
    fun toggleTask(id: Long, completed: Boolean) {
        viewModelScope.launch {
            repository.setTaskCompleted(id, completed)
        }
    }

    fun addTask(task: StudyTaskEntity) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    // --- Schedule Item Actions ---
    fun addScheduleItem(item: ScheduleItemEntity) {
        viewModelScope.launch {
            repository.insertScheduleItem(item)
        }
    }

    fun deleteScheduleItem(id: Long) {
        viewModelScope.launch {
            repository.deleteScheduleItem(id)
        }
    }

    // --- Subject Actions ---
    fun addSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.insertSubject(subject)
        }
    }

    fun updateSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.updateSubject(subject)
        }
    }

    fun deleteSubject(id: Long) {
        viewModelScope.launch {
            repository.deleteSubject(id)
        }
    }

    fun toggleTopicStatus(subjectId: Long, topic: String) {
        viewModelScope.launch {
            val sub = repository.getSubjectById(subjectId) ?: return@launch
            val completedList = sub.completedTopics.split("\n").filter { it.isNotBlank() }.toMutableList()
            if (completedList.contains(topic)) {
                completedList.remove(topic)
            } else {
                completedList.add(topic)
            }
            repository.updateSubject(sub.copy(completedTopics = completedList.joinToString("\n")))
        }
    }

    // --- Exam Actions ---
    fun addExam(exam: ExamCountdownEntity) {
        viewModelScope.launch {
            repository.insertExam(exam)
        }
    }

    fun deleteExam(id: Long) {
        viewModelScope.launch {
            repository.deleteExam(id)
        }
    }

    // --- Jarvis AI Study Assistant Generator ---
    private val _aiPlan = MutableStateFlow<GeneratedStudyPlan?>(null)
    val aiPlan: StateFlow<GeneratedStudyPlan?> = _aiPlan.asStateFlow()

    private val _isGeneratingPlan = MutableStateFlow(false)
    val isGeneratingPlan: StateFlow<Boolean> = _isGeneratingPlan.asStateFlow()

    fun generateAiStudyPlan(subject: String, topic: String, daysAvailable: Int, hoursPerDay: Float) {
        _isGeneratingPlan.value = true
        viewModelScope.launch {
            delay(1200) // Brief calculation animation for polished feel
            val steps = mutableListOf<PlanStep>()
            val dailyMins = (hoursPerDay * 60).toInt()

            for (day in 1..daysAvailable) {
                val phase = when {
                    day == 1 -> "Foundation & Core Concepts"
                    day <= daysAvailable / 2 -> "Deep Mastery & Problem Solving"
                    day < daysAvailable -> "Practice Tests & Weak-spot Drilling"
                    else -> "Final Active Recall & Formula Polish"
                }

                val focus = when {
                    day == 1 -> "Breakdown of $topic definitions, mental models, and foundational formulas."
                    day <= daysAvailable / 2 -> "Hands-on problem sets for $topic. Focus on high-weight subtopics."
                    day < daysAvailable -> "Timed mock exam / flashcard drills on past paper questions."
                    else -> "Light flashcard revision, sleep early, mental rehearsal."
                }

                val recallTip = when (day % 4) {
                    0 -> "Feynman Technique: Explain this subtopic in 3 sentences without jargon."
                    1 -> "Blurting Method: Close notes and write down everything you remember in 5 minutes."
                    2 -> "Interleaved Practice: Mix 2 difficult problems with 2 simpler review problems."
                    else -> "Spaced Retrieval: Test yourself on Day 1 concepts before starting today's session."
                }

                steps.add(
                    PlanStep(
                        dayNumber = day,
                        phase = phase,
                        focusArea = focus,
                        recommendedMinutes = dailyMins,
                        activeRecallTip = recallTip
                    )
                )
            }

            _aiPlan.value = GeneratedStudyPlan(
                title = "Jarvis Adaptive Roadmap: $topic",
                subject = subject,
                totalDays = daysAvailable,
                estimatedHours = hoursPerDay * daysAvailable,
                steps = steps
            )
            _isGeneratingPlan.value = false
        }
    }

    fun clearAiPlan() {
        _aiPlan.value = null
    }

    // --- Helper Calculations ---
    private fun calculateStreak(sessions: List<StudySessionEntity>): Int {
        if (sessions.isEmpty()) return 0
        val dayFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val uniqueDays = sessions.map { dayFormat.format(Date(it.timestamp)) }.distinct().sortedDescending()

        val todayStr = dayFormat.format(Date())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dayFormat.format(cal.time)

        if (uniqueDays.isEmpty()) return 0
        val firstDay = uniqueDays[0]
        if (firstDay != todayStr && firstDay != yesterdayStr) {
            return 0
        }

        var streak = 1
        var checkCal = Calendar.getInstance()
        if (firstDay == yesterdayStr) {
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
        }

        for (i in 1 until uniqueDays.size) {
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
            val expectedDay = dayFormat.format(checkCal.time)
            if (uniqueDays[i] == expectedDay) {
                streak++
            } else {
                break
            }
        }
        return streak
    }

    private fun calculateWeeklyMetrics(sessions: List<StudySessionEntity>): List<DayStudyMetric> {
        val result = mutableListOf<DayStudyMetric>()
        val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val dayFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

        val cal = Calendar.getInstance()
        val todayStr = dayFormat.format(cal.time)

        // Generate past 7 days (including today)
        cal.add(Calendar.DAY_OF_YEAR, -6)
        for (i in 0..6) {
            val dateStr = dayFormat.format(cal.time)
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
            val label = dayLabels[dayOfWeek - 1]

            val minutesForDay = sessions.filter {
                dayFormat.format(Date(it.timestamp)) == dateStr
            }.sumOf { it.durationMinutes }

            result.add(
                DayStudyMetric(
                    dayLabel = label,
                    dayNumber = cal.get(Calendar.DAY_OF_MONTH),
                    totalMinutes = minutesForDay,
                    isToday = dateStr == todayStr
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return result
    }
}
