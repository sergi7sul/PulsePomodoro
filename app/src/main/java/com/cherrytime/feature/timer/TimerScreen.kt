package com.cherrytime.feature.timer

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cherrytime.domain.model.Phase
import com.cherrytime.domain.model.TimerState

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TimerContent(uiState = uiState, onIntent = viewModel::onIntent)
}

@Composable
private fun TimerContent(
    uiState: TimerUiState,
    onIntent: (TimerIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        PhaseLabel(phase = uiState.currentPhase)

        Spacer(Modifier.height(32.dp))

        PhaseRing(
            progress = uiState.displayProgress,
            remainingMs = uiState.displayRemainingMs,
            phase = uiState.currentPhase,
            ringSize = 260.dp,
        )

        Spacer(Modifier.height(24.dp))

        SessionDots(sessionsInCycle = uiState.sessionsInCycle)

        Spacer(Modifier.height(24.dp))

        MotivationCard(
            quote = uiState.currentQuote,
            visible = uiState.currentPhase.isBreak,
        )

        Spacer(Modifier.height(if (uiState.currentPhase.isBreak) 16.dp else 40.dp))

        TimerControls(timerState = uiState.timerState, nextPhase = uiState.nextPhase, onIntent = onIntent)
    }
}

@Composable
private fun PhaseLabel(phase: Phase) {
    Text(
        text = phase.label.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = MaterialTheme.typography.labelLarge.letterSpacing,
    )
}

@Composable
private fun PhaseRing(
    progress: Float,
    remainingMs: Long,
    phase: Phase,
    ringSize: Dp,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 400),
        label = "ring_progress",
    )

    val ringColor by animateColorAsState(
        targetValue = when (phase) {
            Phase.WORK -> MaterialTheme.colorScheme.primary
            Phase.SHORT_BREAK -> MaterialTheme.colorScheme.secondary
            Phase.LONG_BREAK -> MaterialTheme.colorScheme.tertiary
        },
        animationSpec = tween(durationMillis = 600),
        label = "ring_color",
    )

    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(ringSize)) {
            val strokePx = 18.dp.toPx()
            val diameter = size.minDimension - strokePx
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            if (animatedProgress > 0f) {
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            }
        }

        TimerText(remainingMs = remainingMs)
    }
}

@Composable
private fun TimerText(remainingMs: Long) {
    val minutes = remainingMs / 60_000
    val seconds = (remainingMs % 60_000) / 1_000
    Text(
        text = "%02d:%02d".format(minutes, seconds),
        style = MaterialTheme.typography.displayMedium,
        fontWeight = FontWeight.Light,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun SessionDots(sessionsInCycle: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(4) { index ->
            SessionDot(filled = index < sessionsInCycle)
        }
    }
}

@Composable
private fun SessionDot(filled: Boolean) {
    val color by animateColorAsState(
        targetValue = if (filled) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant,
        label = "dot_color",
    )
    Canvas(modifier = Modifier.size(10.dp)) {
        drawCircle(color = color)
    }
}

@Composable
private fun TimerControls(
    timerState: TimerState,
    nextPhase: Phase,
    onIntent: (TimerIntent) -> Unit,
) {
    when (timerState) {
        is TimerState.Idle -> {
            Button(onClick = { onIntent(TimerIntent.Start) }) {
                Text("Start ${nextPhase.label}")
            }
        }

        is TimerState.Running -> {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FilledTonalButton(onClick = { onIntent(TimerIntent.Pause) }) {
                    Text("Pause")
                }
                OutlinedButton(onClick = { onIntent(TimerIntent.Skip) }) {
                    Text("Skip")
                }
            }
        }

        is TimerState.Paused -> {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { onIntent(TimerIntent.Resume) }) {
                    Text("Resume")
                }
                OutlinedButton(onClick = { onIntent(TimerIntent.Reset) }) {
                    Text("Reset")
                }
            }
        }

        is TimerState.Finished -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (timerState.phase == Phase.WORK) "Session complete!" else "Break done!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = { onIntent(TimerIntent.Start) }) {
                    Text("Start ${nextPhase.label}")
                }
            }
        }
    }
}
