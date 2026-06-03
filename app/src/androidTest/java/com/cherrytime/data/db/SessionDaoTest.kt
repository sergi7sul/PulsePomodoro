package com.cherrytime.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cherrytime.data.db.session.SessionDao
import com.cherrytime.data.db.session.SessionEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionDaoTest {

    private lateinit var db: CherryTimeDatabase
    private lateinit var dao: SessionDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CherryTimeDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.sessionDao()
    }

    @After
    fun teardown() = db.close()

    @Test
    fun insertAndQueryByDay() = runTest {
        val dayStartMs = 1_000_000L
        val session = SessionEntity(
            phase = "WORK",
            durationMs = 25 * 60_000L,
            startedAt = dayStartMs + 1_000L,
            completedAt = dayStartMs + 26 * 60_000L,
        )
        dao.insert(session)

        val results = dao.getSessionsForDay(dayStartMs, dayStartMs + 86_400_000L).first()
        assertThat(results).hasSize(1)
        assertThat(results.first().phase).isEqualTo("WORK")
    }

    @Test
    fun queryByDay_excludesOtherDays() = runTest {
        val dayStartMs = 86_400_000L
        dao.insert(SessionEntity(phase = "WORK", durationMs = 0, startedAt = dayStartMs - 1L, completedAt = null))
        dao.insert(SessionEntity(phase = "WORK", durationMs = 0, startedAt = dayStartMs + 1L, completedAt = dayStartMs + 100L))

        val results = dao.getSessionsForDay(dayStartMs, dayStartMs + 86_400_000L).first()
        assertThat(results).hasSize(1)
    }

    @Test
    fun totalCompletedWorkSessions_countsOnlyCompletedWork() = runTest {
        dao.insert(SessionEntity(phase = "WORK", durationMs = 0, startedAt = 1L, completedAt = 2L))
        dao.insert(SessionEntity(phase = "WORK", durationMs = 0, startedAt = 3L, completedAt = null))
        dao.insert(SessionEntity(phase = "SHORT_BREAK", durationMs = 0, startedAt = 5L, completedAt = 6L))

        val count = dao.getTotalCompletedWorkSessions().first()
        assertThat(count).isEqualTo(1)
    }
}
