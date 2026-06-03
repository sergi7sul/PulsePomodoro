package com.cherrytime.data.db.stretch

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cherrytime.domain.model.StretchExercise

@Entity(tableName = "stretch_exercises")
data class StretchExerciseEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val instruction: String,
    val durationSec: Int,
)

fun StretchExerciseEntity.toDomain() = StretchExercise(
    id = id,
    name = name,
    instruction = instruction,
    durationSec = durationSec,
)
