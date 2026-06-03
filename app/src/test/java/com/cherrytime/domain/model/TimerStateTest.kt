package com.cherrytime.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TimerStateTest {

    @Test
    fun `Running progressFraction is 0 at start`() {
        val state = TimerState.Running(
            remainingMs = 25 * 60 * 1000L,
            phase = Phase.WORK,
            totalMs = 25 * 60 * 1000L,
        )
        assertThat(state.progressFraction).isWithin(0.001f).of(0f)
    }

    @Test
    fun `Running progressFraction is 1 when time is up`() {
        val state = TimerState.Running(
            remainingMs = 0L,
            phase = Phase.WORK,
            totalMs = 25 * 60 * 1000L,
        )
        assertThat(state.progressFraction).isWithin(0.001f).of(1f)
    }

    @Test
    fun `Running progressFraction is 0_5 at half time`() {
        val total = 25 * 60 * 1000L
        val state = TimerState.Running(
            remainingMs = total / 2,
            phase = Phase.WORK,
            totalMs = total,
        )
        assertThat(state.progressFraction).isWithin(0.001f).of(0.5f)
    }

    @Test
    fun `Idle is distinct from Running and Paused`() {
        val idle = TimerState.Idle
        assertThat(idle).isNotInstanceOf(TimerState.Running::class.java)
        assertThat(idle).isNotInstanceOf(TimerState.Paused::class.java)
        assertThat(idle).isNotInstanceOf(TimerState.Finished::class.java)
    }

    @Test
    fun `Finished carries the completed phase`() {
        val state = TimerState.Finished(Phase.SHORT_BREAK)
        assertThat(state.phase).isEqualTo(Phase.SHORT_BREAK)
    }

    @Test
    fun `PomodoroSession wasCompleted is true only when completedAt is set`() {
        val incomplete = PomodoroSession(
            phase = Phase.WORK,
            durationMs = Phase.WORK.defaultDurationMs,
            startedAt = 1000L,
            completedAt = null,
        )
        val complete = incomplete.copy(completedAt = 2000L)

        assertThat(incomplete.wasCompleted).isFalse()
        assertThat(complete.wasCompleted).isTrue()
    }
}
