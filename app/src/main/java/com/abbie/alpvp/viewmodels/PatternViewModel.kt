package com.abbie.alpvp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class PatternUiState(
    val mode: Mode = Mode.IDLE,
    val sequence: List<Int> = emptyList(),
    val showingIndex: Int = -1,       // index tile yang sedang menyala otomatis
    val userProgress: Int = 0,        // langkah user saat ini
    val score: Int = 0,               // Total ronde yang berhasil diselesaikan
    val message: String? = null
) {
    enum class Mode { IDLE, SHOWING, INPUT, SUCCESS, FAIL }
}

class PatternViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PatternUiState())
    val uiState: StateFlow<PatternUiState> = _uiState

    private val colorsCount = 4

    // Memulai game (reset score)
    fun startGame() {
        _uiState.value = PatternUiState(score = 0)
        startRound()
    }

    // Memulai ronde baru (Endless loop)
    private fun startRound() {
        viewModelScope.launch {
            // Jeda sedikit sebelum ronde baru mulai agar tidak kaget
            delay(500)

            // Generate pola baru (panjang 3 atau 4 agar tidak terlalu sudah/mudah)
            val length = if (_uiState.value.score > 2) 4 else 3
            val seq = generateSequence(length)

            _uiState.value = _uiState.value.copy(
                sequence = seq,
                mode = PatternUiState.Mode.SHOWING,
                showingIndex = -1,
                userProgress = 0,
                message = "Watch the pattern..."
            )
            showSequence(seq)
        }
    }

    private fun generateSequence(length: Int): List<Int> {
        val list = mutableListOf<Int>()
        repeat(length) {
            list.add(Random.nextInt(colorsCount))
        }
        return list
    }

    private suspend fun showSequence(seq: List<Int>) {
        delay(300)
        for (i in seq.indices) {
            _uiState.value = _uiState.value.copy(showingIndex = i)
            delay(500) // Durasi nyala
            _uiState.value = _uiState.value.copy(showingIndex = -1)
            delay(150) // Jeda antar nyala
        }

        _uiState.value = _uiState.value.copy(
            mode = PatternUiState.Mode.INPUT,
            showingIndex = -1,
            message = "Your turn!"
        )
    }

    fun onTileTapped(index: Int) {
        val state = _uiState.value
        if (state.mode != PatternUiState.Mode.INPUT) return

        val expected = state.sequence.getOrNull(state.userProgress)

        if (expected == index) {
            // BENAR
            val newProgress = state.userProgress + 1
            if (newProgress >= state.sequence.size) {
                // RONDE SELESAI -> MENANG
                val newScore = state.score + 1
                _uiState.value = state.copy(
                    userProgress = newProgress,
                    score = newScore,
                    mode = PatternUiState.Mode.SUCCESS,
                    message = "Great Job! Next..."
                )

                // OTOMATIS MULAI RONDE BARU (Endless)
                viewModelScope.launch {
                    delay(800) // Beri waktu user menikmati kemenangan sebentar
                    startRound()
                }
            } else {
                // LANJUT INPUT BERIKUTNYA
                _uiState.value = state.copy(userProgress = newProgress)
            }
        } else {
            // SALAH
            _uiState.value = state.copy(
                mode = PatternUiState.Mode.FAIL,
                message = "Oops! Try again."
            )
            // Replay otomatis pola yang sama
            viewModelScope.launch {
                delay(800)
                replay()
            }
        }
    }

    fun replay() {
        val seq = _uiState.value.sequence
        if (seq.isEmpty()) {
            startGame()
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                mode = PatternUiState.Mode.SHOWING,
                userProgress = 0,
                showingIndex = -1,
                message = "Watch again..."
            )
            showSequence(seq)
        }
    }
}