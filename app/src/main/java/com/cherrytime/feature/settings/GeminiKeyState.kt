package com.cherrytime.feature.settings

data class GeminiKeyState(
    val hasKey: Boolean = false,
    val isValidating: Boolean = false,
    val error: String? = null,
)
