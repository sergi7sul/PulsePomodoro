package com.cherrytime.domain.repository

import com.cherrytime.data.db.session.DaySessionSummary
import com.cherrytime.domain.model.PomodoroSession
import kotlinx.coroutines.flow.Flow

interface TimerRepository {
    suspend fun saveSession(session: PomodoroSession): Long
    fun getSessionsForDate(dateEpochDay: Long): Flow<List<PomodoroSession>>
    fun getTotalCompletedWorkSessions(): Flow<Int>
    fun getWorkSummaryByDay(fromMs: Long): Flow<List<DaySessionSummary>>
}
