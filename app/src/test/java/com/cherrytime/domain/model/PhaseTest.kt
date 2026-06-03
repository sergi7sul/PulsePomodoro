package com.cherrytime.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PhaseTest {

    @Test
    fun `WORK default duration is 25 minutes`() {
        assertThat(Phase.WORK.defaultDurationMs).isEqualTo(25 * 60 * 1000L)
    }

    @Test
    fun `SHORT_BREAK default duration is 5 minutes`() {
        assertThat(Phase.SHORT_BREAK.defaultDurationMs).isEqualTo(5 * 60 * 1000L)
    }

    @Test
    fun `LONG_BREAK default duration is 15 minutes`() {
        assertThat(Phase.LONG_BREAK.defaultDurationMs).isEqualTo(15 * 60 * 1000L)
    }

    @Test
    fun `only WORK is not a break`() {
        assertThat(Phase.WORK.isBreak).isFalse()
        assertThat(Phase.SHORT_BREAK.isBreak).isTrue()
        assertThat(Phase.LONG_BREAK.isBreak).isTrue()
    }
}
