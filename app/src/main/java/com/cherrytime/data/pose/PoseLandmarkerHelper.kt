package com.cherrytime.data.pose

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseLandmarkerHelper(
    private val context: Context,
    private val onResult: (PoseLandmarkerResult) -> Unit,
    private val onError: (String) -> Unit,
) {
    private var poseLandmarker: PoseLandmarker? = null

    val isAvailable: Boolean get() = poseLandmarker != null

    fun setup() {
        runCatching {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_ASSET_PATH)
                .build()
            val options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setNumPoses(1)
                .setResultListener { result, _ -> onResult(result) }
                .setErrorListener { error -> onError(error.message ?: "MediaPipe error") }
                .build()
            poseLandmarker = PoseLandmarker.createFromOptions(context, options)
        }.onFailure { e ->
            onError(
                if (e is IllegalStateException && e.message?.contains("asset") == true)
                    "Model file not found. Download $MODEL_ASSET_PATH and place it in assets/."
                else e.message ?: "Failed to initialize pose landmarker"
            )
        }
    }

    fun detect(imageProxy: ImageProxy) {
        val landmarker = poseLandmarker ?: return
        val bitmap = imageProxy.use {
            Bitmap.createBitmap(it.width, it.height, Bitmap.Config.ARGB_8888).also { bmp ->
                it.planes[0].buffer.rewind()
                bmp.copyPixelsFromBuffer(it.planes[0].buffer)
            }
        }
        val mpImage = BitmapImageBuilder(bitmap).build()
        landmarker.detectAsync(mpImage, SystemClock.uptimeMillis())
    }

    fun close() {
        poseLandmarker?.close()
        poseLandmarker = null
    }

    companion object {
        const val MODEL_ASSET_PATH = "pose_landmarker_lite.task"
    }
}
