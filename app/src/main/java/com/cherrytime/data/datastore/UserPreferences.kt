package com.cherrytime.data.datastore

import com.cherrytime.domain.model.Phase

data class UserPreferences(
    val workDurationMin: Int = 25,
    val shortBreakMin: Int = 5,
    val longBreakMin: Int = 15,
    val longBreakInterval: Int = 4,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val waterReminderEnabled: Boolean = true,
    val waterReminderIntervalMin: Int = 45,
    val postureReminderEnabled: Boolean = true,
    val postureReminderIntervalMin: Int = 60,
    val onboardingCompleted: Boolean = false,
) {
    fun durationMs(phase: Phase): Long = when (phase) {
        Phase.WORK -> workDurationMin * 60_000L
        Phase.SHORT_BREAK -> shortBreakMin * 60_000L
        Phase.LONG_BREAK -> longBreakMin * 60_000L
    }
}
