package com.cherrytime.core.navigation

sealed class Screen(val route: String) {
    data object Timer : Screen("timer")
    data object Analytics : Screen("analytics")
    data object Settings : Screen("settings")
    data object ActiveBreak : Screen("active_break")
}
