package com.cherrytime.domain.model

data class PomodoroSession(
    val id: Long = 0,
    val phase: Phase,
    val durationMs: Long,
    val startedAt: Long,
    val completedAt: Long?,
) {
    val wasCompleted: Boolean get() = completedAt != null
}
