package com.cherrytime.domain.model

sealed interface TimerState {
    data object Idle : TimerState

    data class Running(
        val remainingMs: Long,
        val phase: Phase,
        val totalMs: Long,
    ) : TimerState {
        val progressFraction: Float get() = 1f - remainingMs.toFloat() / totalMs
    }

    data class Paused(
        val remainingMs: Long,
        val phase: Phase,
        val totalMs: Long,
    ) : TimerState

    data class Finished(val phase: Phase) : TimerState
}
