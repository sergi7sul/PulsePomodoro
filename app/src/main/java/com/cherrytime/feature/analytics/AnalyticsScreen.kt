package com.cherrytime.feature.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cherrytime.R
import com.cherrytime.core.ui.chart.BarChart
import com.cherrytime.core.ui.chart.HeatmapCalendar
import com.cherrytime.domain.usecase.AnalyticsStats
import java.util.Calendar

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = hiltViewModel()) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    if (stats == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val data = stats!!
    val hasEnoughData = data.weeklyMinutes.count { it.second > 0 } >= 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.analytics_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(Modifier.height(16.dp))

        if (!hasEnoughData) {
            Text(
                text = stringResource(R.string.analytics_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            SummaryRow(stats = data)
            Spacer(Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(Modifier.height(20.dp))
            WeeklyChart(data = data)
            Spacer(Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(Modifier.height(20.dp))
            MonthlyHeatmap(data = data)
        }
    }
}

@Composable
private fun SummaryRow(stats: AnalyticsStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard(
            label = "Streak",
            value = "${stats.currentStreak}d",
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = "Total sessions",
            value = "${stats.totalCompletedSessions}",
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = "Today",
            value = "${stats.weeklyMinutes.lastOrNull()?.second ?: 0}m",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun WeeklyChart(data: AnalyticsStats) {
    Text(
        text = "This week (focus minutes)",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(8.dp))
    val barData = data.weeklyMinutes.map { (epochDay, minutes) ->
        epochDayLabel(epochDay) to minutes.toFloat()
    }
    BarChart(
        data = barData,
        barColor = MaterialTheme.colorScheme.primary,
        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun MonthlyHeatmap(data: AnalyticsStats) {
    Text(
        text = "Last 30 days",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(8.dp))
    HeatmapCalendar(
        data = data.monthlyCount,
        baseColor = MaterialTheme.colorScheme.primary,
        emptyColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

private val DAY_LABELS = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")

private fun epochDayLabel(epochDay: Long): String {
    // epochDay 0 = 1970-01-01 = Thursday (index 3 in Mon=0 system)
    return DAY_LABELS[((epochDay + 3) % 7).toInt()]
}
