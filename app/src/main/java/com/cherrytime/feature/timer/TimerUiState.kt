package com.cherrytime.feature.timer

import com.cherrytime.domain.model.Phase
import com.cherrytime.domain.model.Quote
import com.cherrytime.domain.model.TimerState

data class TimerUiState(
    val timerState: TimerState = TimerState.Idle,
    val nextPhase: Phase = Phase.WORK,
    val completedWorkSessions: Int = 0,
    val currentQuote: Quote? = null,
) {
    val displayProgress: Float
        get() = when (val s = timerState) {
            is TimerState.Running -> s.progressFraction
            is TimerState.Paused -> 1f - s.remainingMs.toFloat() / s.totalMs
            is TimerState.Finished -> 1f
            is TimerState.Idle -> 0f
        }

    val displayRemainingMs: Long
        get() = when (val s = timerState) {
            is TimerState.Running -> s.remainingMs
            is TimerState.Paused -> s.remainingMs
            is TimerState.Idle -> nextPhase.defaultDurationMs
            is TimerState.Finished -> 0L
        }

    val currentPhase: Phase
        get() = when (val s = timerState) {
            is TimerState.Running -> s.phase
            is TimerState.Paused -> s.phase
            is TimerState.Finished -> s.phase
            is TimerState.Idle -> nextPhase
        }

    val sessionsInCycle: Int get() = completedWorkSessions % 4
}
