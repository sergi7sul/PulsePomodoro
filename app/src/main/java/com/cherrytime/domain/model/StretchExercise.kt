package com.cherrytime.domain.model

data class StretchExercise(
    val id: Int,
    val name: String,
    val instruction: String,
    val durationSec: Int,
    val targetLandmarkTriplets: List<LandmarkTriplet> = emptyList(),
)

data class LandmarkTriplet(
    val first: Int,
    val vertex: Int,
    val third: Int,
    val minAngleDeg: Float,
    val maxAngleDeg: Float,
)
