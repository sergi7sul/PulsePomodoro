package com.cherrytime.domain.usecase

import com.cherrytime.data.db.session.DaySessionSummary
import com.cherrytime.domain.repository.TimerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class AnalyticsStats(
    val weeklyMinutes: List<Pair<Long, Long>>,   // epochDay → focusMinutes (7 days)
    val monthlyCount: List<Pair<Long, Int>>,      // epochDay → sessionCount (30 days)
    val currentStreak: Int,
    val totalCompletedSessions: Int,
)

class GetDailyStatsUseCase @Inject constructor(
    private val timerRepository: TimerRepository,
) {
    private val todayEpochDay: Long
        get() = System.currentTimeMillis() / 86_400_000L

    operator fun invoke(): Flow<AnalyticsStats> {
        val today = todayEpochDay
        val sevenDaysAgoMs = (today - 6) * 86_400_000L
        val thirtyDaysAgoMs = (today - 29) * 86_400_000L

        val weeklyFlow = timerRepository.getWorkSummaryByDay(sevenDaysAgoMs)
        val monthlyFlow = timerRepository.getWorkSummaryByDay(thirtyDaysAgoMs)
        val totalFlow = timerRepository.getTotalCompletedWorkSessions()

        return combine(weeklyFlow, monthlyFlow, totalFlow) { weekly, monthly, total ->
            AnalyticsStats(
                weeklyMinutes = buildWeeklyList(today, weekly),
                monthlyCount = buildMonthlyList(today, monthly),
                currentStreak = computeStreak(monthly),
                totalCompletedSessions = total,
            )
        }
    }

    private fun buildWeeklyList(
        today: Long,
        summaries: List<DaySessionSummary>,
    ): List<Pair<Long, Long>> {
        val map = summaries.associate { it.epochDay to it.focusMinutes }
        return (6 downTo 0).map { daysAgo ->
            val day = today - daysAgo
            day to (map[day] ?: 0L)
        }
    }

    private fun buildMonthlyList(
        today: Long,
        summaries: List<DaySessionSummary>,
    ): List<Pair<Long, Int>> {
        val map = summaries.associate { it.epochDay to it.sessionCount }
        return (29 downTo 0).map { daysAgo ->
            val day = today - daysAgo
            day to (map[day] ?: 0)
        }
    }

    private fun computeStreak(summaries: List<DaySessionSummary>): Int {
        val today = todayEpochDay
        val daysWithSessions = summaries
            .filter { it.sessionCount > 0 }
            .map { it.epochDay }
            .toSet()
        var streak = 0
        var day = today
        while (day in daysWithSessions) {
            streak++
            day--
        }
        return streak
    }
}
