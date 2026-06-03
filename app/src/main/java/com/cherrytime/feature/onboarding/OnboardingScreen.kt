package com.cherrytime.feature.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onCompleted: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* proceed regardless */ }

    val onFinish: () -> Unit = {
        viewModel.markCompleted()
        onCompleted()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            when (page) {
                0 -> WelcomePage()
                1 -> FocusMethodPage()
                2 -> WellnessPage()
            }
        }

        // Skip button — top end
        TextButton(
            onClick = onFinish,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
        ) {
            Text("Skip")
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Page dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index ->
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(8.dp)) {
                        drawCircle(
                            color = if (index == pagerState.currentPage)
                                androidx.compose.ui.graphics.Color(0xFFE91E63)
                            else androidx.compose.ui.graphics.Color.LightGray
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            if (pagerState.currentPage < 2) {
                Button(
                    onClick = {
                        val next = pagerState.currentPage + 1
                        // Request notification permission when moving to page 2
                        if (next == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        scope.launch { pagerState.animateScrollToPage(next) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Next")
                }
            } else {
                Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Get Started")
                }
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    OnboardingPage(
        emoji = "🍒",
        title = "Welcome to CherryTime",
        body = "A Pomodoro timer with a wellness twist. Focus deeply, rest actively, and build lasting productivity habits.",
    )
}

@Composable
private fun FocusMethodPage() {
    OnboardingPage(
        emoji = "⏱️",
        title = "The Pomodoro Method",
        body = "Work in focused 25-minute sessions separated by short breaks. Every 4 sessions, take a longer break to fully recharge.",
    )
}

@Composable
private fun WellnessPage() {
    OnboardingPage(
        emoji = "🧘",
        title = "Active Breaks",
        body = "During long breaks, CherryTime guides you through short stretches with real-time pose feedback using your front camera. Camera access is optional.",
    )
}

@Composable
private fun OnboardingPage(emoji: String, title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = emoji, style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(32.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
