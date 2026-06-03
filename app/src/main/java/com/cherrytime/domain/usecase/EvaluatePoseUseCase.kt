package com.cherrytime.domain.usecase

import com.cherrytime.domain.model.LandmarkTriplet
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import javax.inject.Inject
import kotlin.math.acos
import kotlin.math.sqrt

class EvaluatePoseUseCase @Inject constructor() {

    fun invoke(result: PoseLandmarkerResult, triplets: List<LandmarkTriplet>): PoseScore {
        if (result.landmarks().isEmpty() || triplets.isEmpty()) return PoseScore.UNKNOWN
        val landmarks = result.landmarks()[0]
        val scores = triplets.map { triplet ->
            scoreTriplet(landmarks, triplet)
        }
        return when {
            scores.all { it == TripletScore.GOOD } -> PoseScore.GOOD
            scores.any { it == TripletScore.BAD } -> PoseScore.BAD
            else -> PoseScore.FAIR
        }
    }

    private fun scoreTriplet(landmarks: List<NormalizedLandmark>, triplet: LandmarkTriplet): TripletScore {
        if (triplet.first >= landmarks.size || triplet.vertex >= landmarks.size || triplet.third >= landmarks.size) {
            return TripletScore.GOOD
        }
        val angle = angleDeg(landmarks[triplet.first], landmarks[triplet.vertex], landmarks[triplet.third])
        return when {
            angle in triplet.minAngleDeg..triplet.maxAngleDeg -> TripletScore.GOOD
            angle < triplet.minAngleDeg - 15 || angle > triplet.maxAngleDeg + 15 -> TripletScore.BAD
            else -> TripletScore.FAIR
        }
    }

    private fun angleDeg(a: NormalizedLandmark, b: NormalizedLandmark, c: NormalizedLandmark): Float {
        val abX = a.x() - b.x(); val abY = a.y() - b.y()
        val cbX = c.x() - b.x(); val cbY = c.y() - b.y()
        val dot = abX * cbX + abY * cbY
        val magAB = sqrt(abX * abX + abY * abY)
        val magCB = sqrt(cbX * cbX + cbY * cbY)
        if (magAB == 0f || magCB == 0f) return 0f
        val cos = (dot / (magAB * magCB)).coerceIn(-1f, 1f)
        return Math.toDegrees(acos(cos.toDouble()).toFloat().toDouble()).toFloat()
    }

    private enum class TripletScore { GOOD, FAIR, BAD }
}

enum class PoseScore { GOOD, FAIR, BAD, UNKNOWN }
