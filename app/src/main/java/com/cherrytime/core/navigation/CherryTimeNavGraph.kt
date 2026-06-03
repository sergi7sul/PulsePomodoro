package com.cherrytime.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cherrytime.data.camera.CameraManager
import com.cherrytime.feature.activebreak.ActiveBreakScreen
import com.cherrytime.feature.analytics.AnalyticsScreen
import com.cherrytime.feature.settings.SettingsScreen
import com.cherrytime.feature.timer.TimerScreen

@Composable
fun CherryTimeNavGraph(
    navController: NavHostController,
    cameraManager: CameraManager,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Timer.route,
        modifier = modifier,
    ) {
        composable(Screen.Timer.route) {
            TimerScreen(
                onStartActiveBreak = { navController.navigate(Screen.ActiveBreak.route) },
            )
        }
        composable(Screen.Analytics.route) {
            AnalyticsScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        composable(Screen.ActiveBreak.route) {
            ActiveBreakScreen(
                cameraManager = cameraManager,
                onFinished = { navController.popBackStack() },
            )
        }
    }
}
