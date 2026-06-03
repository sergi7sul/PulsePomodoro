package com.cherrytime.feature.activebreak

import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cherrytime.data.camera.CameraManager
import com.cherrytime.data.pose.PoseLandmarkerHelper
import com.cherrytime.domain.usecase.PoseScore
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ActiveBreakScreen(
    cameraManager: CameraManager,
    onFinished: () -> Unit,
    viewModel: ActiveBreakViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isFinished) {
        LaunchedEffect(Unit) { onFinished() }
        return
    }

    CameraPermissionScreen(
        onGranted = {
            ActiveBreakContent(
                uiState = uiState,
                cameraManager = cameraManager,
                onPoseResult = viewModel::onPoseResult,
                onPoseError = viewModel::onPoseError,
                onSkip = viewModel::skipExercise,
                onFinished = onFinished,
            )
        },
        onDenied = { onFinished() },
    )
}

@Composable
private fun ActiveBreakContent(
    uiState: ActiveBreakUiState,
    cameraManager: CameraManager,
    onPoseResult: (PoseLandmarkerResult) -> Unit,
    onPoseError: (String) -> Unit,
    onSkip: () -> Unit,
    onFinished: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val poseLandmarkerHelper = remember {
        PoseLandmarkerHelper(context, onResult = onPoseResult, onError = onPoseError)
    }
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) { poseLandmarkerHelper.setup() }
        cameraManager.bindToLifecycle(lifecycleOwner, previewView) { imageProxy ->
            poseLandmarkerHelper.detect(imageProxy)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            poseLandmarkerHelper.close()
            cameraManager.unbind()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f)),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = uiState.current?.name ?: "",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "%d s".format(uiState.secondsRemaining),
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { uiState.progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.White.copy(alpha = 0.3f),
                )
            }

            if (uiState.poseAvailable) {
                PoseScoreIndicator(score = uiState.poseScore)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = uiState.current?.instruction ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onSkip) {
                        Text("Skip", color = Color.White)
                    }
                    Button(onClick = onFinished) {
                        Text("End Break")
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PoseScoreIndicator(score: PoseScore) {
    val (color, label) = when (score) {
        PoseScore.GOOD -> Color(0xFF4CAF50) to "Great form!"
        PoseScore.FAIR -> Color(0xFFFFC107) to "Adjust slightly"
        PoseScore.BAD -> Color(0xFFF44336) to "Check your position"
        PoseScore.UNKNOWN -> Color.Gray to "Getting ready..."
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Canvas(modifier = Modifier.size(12.dp)) { drawCircle(color = color) }
        Text(text = label, color = Color.White, style = MaterialTheme.typography.labelLarge)
    }
}
