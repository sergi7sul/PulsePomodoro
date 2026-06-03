package com.cherrytime.data.db.session

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionEntity): Long

    @Query("""
        SELECT * FROM sessions
        WHERE startedAt >= :dayStartMs AND startedAt < :dayEndMs
        ORDER BY startedAt DESC
    """)
    fun getSessionsForDay(dayStartMs: Long, dayEndMs: Long): Flow<List<SessionEntity>>

    @Query("SELECT COUNT(*) FROM sessions WHERE phase = 'WORK' AND completedAt IS NOT NULL")
    fun getTotalCompletedWorkSessions(): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM sessions
        WHERE phase = 'WORK' AND completedAt IS NOT NULL
          AND startedAt >= :dayStartMs AND startedAt < :dayEndMs
    """)
    suspend fun countCompletedWorkSessionsForDay(dayStartMs: Long, dayEndMs: Long): Int
}
