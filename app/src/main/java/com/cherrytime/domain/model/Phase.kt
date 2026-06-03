package com.cherrytime.domain.model

enum class Phase(val defaultDurationMs: Long, val label: String) {
    WORK(25 * 60 * 1000L, "Focus"),
    SHORT_BREAK(5 * 60 * 1000L, "Short Break"),
    LONG_BREAK(15 * 60 * 1000L, "Long Break");

    val isBreak: Boolean get() = this != WORK
}
