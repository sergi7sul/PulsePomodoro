package com.cherrytime.feature.activebreak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cherrytime.data.db.stretch.StretchExerciseDao
import com.cherrytime.data.db.stretch.toDomain
import com.cherrytime.domain.model.StretchExercise
import com.cherrytime.domain.usecase.EvaluatePoseUseCase
import com.cherrytime.domain.usecase.PoseScore
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActiveBreakUiState(
    val exercises: List<StretchExercise> = emptyList(),
    val currentIndex: Int = 0,
    val secondsRemaining: Int = 0,
    val poseScore: PoseScore = PoseScore.UNKNOWN,
    val isFinished: Boolean = false,
    val poseAvailable: Boolean = false,
) {
    val current: StretchExercise? get() = exercises.getOrNull(currentIndex)
    val progress: Float
        get() = current?.let {
            1f - secondsRemaining.toFloat() / it.durationSec
        } ?: 0f
}

@HiltViewModel
class ActiveBreakViewModel @Inject constructor(
    private val stretchDao: StretchExerciseDao,
    private val evaluatePose: EvaluatePoseUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveBreakUiState())
    val uiState: StateFlow<ActiveBreakUiState> = _uiState.asStateFlow()

    private var countdownJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            val exercises = stretchDao.getAll().map { it.toDomain() }
            if (exercises.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    exercises = exercises,
                    secondsRemaining = exercises[0].durationSec,
                )
                startCountdown()
            }
        }
    }

    fun onPoseResult(result: PoseLandmarkerResult) {
        val exercise = _uiState.value.current ?: return
        val score = evaluatePose.invoke(result, exercise.targetLandmarkTriplets)
        _uiState.value = _uiState.value.copy(poseScore = score, poseAvailable = true)
    }

    fun onPoseError(message: String) {
        _uiState.value = _uiState.value.copy(poseAvailable = false)
    }

    fun skipExercise() = advanceToNext()

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (_uiState.value.secondsRemaining > 0) {
                delay(1_000)
                _uiState.value = _uiState.value.copy(
                    secondsRemaining = _uiState.value.secondsRemaining - 1,
                )
            }
            advanceToNext()
        }
    }

    private fun advanceToNext() {
        val state = _uiState.value
        val nextIndex = state.currentIndex + 1
        if (nextIndex >= state.exercises.size) {
            countdownJob?.cancel()
            _uiState.value = state.copy(isFinished = true)
        } else {
            _uiState.value = state.copy(
                currentIndex = nextIndex,
                secondsRemaining = state.exercises[nextIndex].durationSec,
                poseScore = PoseScore.UNKNOWN,
            )
            startCountdown()
        }
    }

    override fun onCleared() {
        countdownJob?.cancel()
        super.onCleared()
    }
}
