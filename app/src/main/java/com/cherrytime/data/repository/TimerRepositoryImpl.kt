package com.cherrytime.data.repository

import com.cherrytime.data.db.session.SessionDao
import com.cherrytime.data.db.session.toDomain
import com.cherrytime.data.db.session.toEntity
import com.cherrytime.domain.model.PomodoroSession
import com.cherrytime.domain.repository.TimerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao,
) : TimerRepository {

    override suspend fun saveSession(session: PomodoroSession): Long =
        sessionDao.insert(session.toEntity())

    override fun getSessionsForDate(dateEpochDay: Long): Flow<List<PomodoroSession>> {
        val dayStartMs = dateEpochDay * 86_400_000L
        val dayEndMs = dayStartMs + 86_400_000L
        return sessionDao.getSessionsForDay(dayStartMs, dayEndMs)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getTotalCompletedWorkSessions(): Flow<Int> =
        sessionDao.getTotalCompletedWorkSessions()
}
