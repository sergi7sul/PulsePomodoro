package com.cherrytime.feature.timer

sealed interface TimerIntent {
    data object Start : TimerIntent
    data object Pause : TimerIntent
    data object Resume : TimerIntent
    data object Reset : TimerIntent
    data object Skip : TimerIntent
}
