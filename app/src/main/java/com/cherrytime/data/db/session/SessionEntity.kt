package com.cherrytime.data.db.session

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cherrytime.domain.model.Phase
import com.cherrytime.domain.model.PomodoroSession

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phase: String,
    val durationMs: Long,
    val startedAt: Long,
    val completedAt: Long?,
)

fun SessionEntity.toDomain() = PomodoroSession(
    id = id,
    phase = Phase.valueOf(phase),
    durationMs = durationMs,
    startedAt = startedAt,
    completedAt = completedAt,
)

fun PomodoroSession.toEntity() = SessionEntity(
    id = id,
    phase = phase.name,
    durationMs = durationMs,
    startedAt = startedAt,
    completedAt = completedAt,
)
