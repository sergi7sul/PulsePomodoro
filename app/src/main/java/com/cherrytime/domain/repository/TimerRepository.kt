package com.cherrytime.domain.repository

import com.cherrytime.domain.model.PomodoroSession
import kotlinx.coroutines.flow.Flow

interface TimerRepository {
    suspend fun saveSession(session: PomodoroSession): Long
    fun getSessionsForDate(dateEpochDay: Long): Flow<List<PomodoroSession>>
    fun getTotalCompletedWorkSessions(): Flow<Int>
}
