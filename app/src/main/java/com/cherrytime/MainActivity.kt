package com.cherrytime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cherrytime.core.navigation.CherryTimeNavGraph
import com.cherrytime.core.navigation.Screen
import com.cherrytime.core.ui.theme.CherryTimeTheme
import com.cherrytime.data.camera.CameraManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var cameraManager: CameraManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CherryTimeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    CherryTimeApp(cameraManager = cameraManager)
                }
            }
        }
    }
}

@Composable
private fun CherryTimeApp(cameraManager: CameraManager) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val bottomBarScreens = listOf(Screen.Timer, Screen.Analytics, Screen.Settings)
    val showBottomBar = currentRoute != Screen.ActiveBreak.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomBarScreens.forEach { screen ->
                        val selected = backStackEntry?.destination?.hierarchy
                            ?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = when (screen) {
                                        Screen.Timer -> Icons.Filled.Timer
                                        Screen.Analytics -> Icons.Filled.BarChart
                                        Screen.Settings -> Icons.Filled.Settings
                                        else -> Icons.Filled.Timer
                                    },
                                    contentDescription = null,
                                )
                            },
                            label = {
                                Text(
                                    text = when (screen) {
                                        Screen.Timer -> stringResource(R.string.nav_timer)
                                        Screen.Analytics -> stringResource(R.string.nav_analytics)
                                        Screen.Settings -> stringResource(R.string.settings_title)
                                        else -> ""
                                    }
                                )
                            },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        CherryTimeNavGraph(
            navController = navController,
            cameraManager = cameraManager,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
