package com.abbie.alpvp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class TimerState {
    IDLE, FOCUS, SHORT_BREAK, LONG_BREAK, PAUSED
}

enum class AnimationType {
    NONE,
    LOADER_CAT,
    WATER_BUBBLE
}

data class TimerUiState(
    val timerState: TimerState = TimerState.IDLE,
    val timeRemaining: Int = 25 * 60,
    val totalTime: Int = 25 * 60,
    val pomodoroCount: Int = 0,
    val isRunning: Boolean = false,
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val selectedAnimation: AnimationType = AnimationType.LOADER_CAT,
    val previousState: TimerState = TimerState.IDLE,
    val showSettings: Boolean = false,
    val showAnimationPicker: Boolean = false,
    val shouldNavigateToGame: Boolean = false
)

class TimerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        startTimerLoop()
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                if (_uiState.value.isRunning && _uiState.value.timeRemaining > 0) {
                    delay(1000L)
                    _uiState.update { it.copy(timeRemaining = it.timeRemaining - 1) }
                } else if (_uiState.value.isRunning && _uiState.value.timeRemaining == 0) {
                    handleTimerComplete()
                } else {
                    delay(100L)
                }
            }
        }
    }

    private fun handleTimerComplete() {
        _uiState.update { state ->
            when (state.timerState) {
                TimerState.FOCUS -> {
                    val newPomodoroCount = state.pomodoroCount + 1
                    if (newPomodoroCount % 4 == 0) {
                        state.copy(
                            isRunning = false,
                            pomodoroCount = newPomodoroCount,
                            timerState = TimerState.LONG_BREAK,
                            timeRemaining = state.longBreakMinutes * 60,
                            totalTime = state.longBreakMinutes * 60,
                            shouldNavigateToGame = true
                        )
                    } else {
                        state.copy(
                            isRunning = false,
                            pomodoroCount = newPomodoroCount,
                            timerState = TimerState.SHORT_BREAK,
                            timeRemaining = state.shortBreakMinutes * 60,
                            totalTime = state.shortBreakMinutes * 60,
                            shouldNavigateToGame = true
                        )
                    }
                }
                TimerState.SHORT_BREAK, TimerState.LONG_BREAK -> {
                    state.copy(
                        isRunning = false,
                        timerState = TimerState.IDLE,
                        timeRemaining = state.focusMinutes * 60,
                        totalTime = state.focusMinutes * 60
                    )
                }
                else -> state.copy(isRunning = false)
            }
        }
    }

    fun startFocusSession() {
        _uiState.update {
            it.copy(
                previousState = TimerState.FOCUS,
                timerState = TimerState.FOCUS,
                timeRemaining = it.focusMinutes * 60,
                totalTime = it.focusMinutes * 60,
                isRunning = true
            )
        }
    }

    fun pauseTimer() {
        _uiState.update {
            val newPreviousState = if (it.timerState != TimerState.PAUSED) it.timerState else it.previousState
            it.copy(
                isRunning = false,
                timerState = TimerState.PAUSED,
                previousState = newPreviousState
            )
        }
    }

    fun resumeTimer() {
        _uiState.update {
            it.copy(
                isRunning = true,
                timerState = if (it.timerState == TimerState.PAUSED) it.previousState else it.timerState
            )
        }
    }

    fun resetTimer() {
        _uiState.update {
            it.copy(
                isRunning = false,
                timerState = TimerState.IDLE,
                timeRemaining = it.focusMinutes * 60,
                totalTime = it.focusMinutes * 60
            )
        }
    }

    fun skipToNext() {
        _uiState.update { state ->
            when (state.timerState) {
                TimerState.FOCUS -> {
                    if ((state.pomodoroCount + 1) % 4 == 0) {
                        state.copy(
                            isRunning = false,
                            timerState = TimerState.LONG_BREAK,
                            timeRemaining = state.longBreakMinutes * 60,
                            totalTime = state.longBreakMinutes * 60
                        )
                    } else {
                        state.copy(
                            isRunning = false,
                            timerState = TimerState.SHORT_BREAK,
                            timeRemaining = state.shortBreakMinutes * 60,
                            totalTime = state.shortBreakMinutes * 60
                        )
                    }
                }
                else -> {
                    state.copy(
                        isRunning = false,
                        timerState = TimerState.IDLE,
                        timeRemaining = state.focusMinutes * 60,
                        totalTime = state.focusMinutes * 60
                    )
                }
            }
        }
    }

    fun updateSettings(focusMinutes: Int, shortBreakMinutes: Int, longBreakMinutes: Int) {
        _uiState.update {
            val newTimeRemaining = if (it.timerState == TimerState.IDLE) focusMinutes * 60 else it.timeRemaining
            val newTotalTime = if (it.timerState == TimerState.IDLE) focusMinutes * 60 else it.totalTime

            it.copy(
                focusMinutes = focusMinutes,
                shortBreakMinutes = shortBreakMinutes,
                longBreakMinutes = longBreakMinutes,
                timeRemaining = newTimeRemaining,
                totalTime = newTotalTime
            )
        }
    }

    fun setSelectedAnimation(animation: AnimationType) {
        _uiState.update { it.copy(selectedAnimation = animation) }
    }

    fun setShowSettings(show: Boolean) {
        _uiState.update { it.copy(showSettings = show) }
    }

    fun setShowAnimationPicker(show: Boolean) {
        _uiState.update { it.copy(showAnimationPicker = show) }
    }

    fun clearGameNavigation() {
        _uiState.update { it.copy(shouldNavigateToGame = false) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}