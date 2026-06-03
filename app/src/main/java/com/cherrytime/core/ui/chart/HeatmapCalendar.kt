package com.cherrytime.core.ui.chart

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun HeatmapCalendar(
    data: List<Pair<Long, Int>>,       // epochDay → count (30 entries, oldest first)
    baseColor: Color,
    emptyColor: Color,
    modifier: Modifier = Modifier,
    cellSize: Dp = 16.dp,
    gap: Dp = 3.dp,
) {
    val maxCount = data.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1

    var animTarget by remember { mutableFloatStateOf(0f) }
    val animProgress by animateFloatAsState(
        targetValue = animTarget,
        animationSpec = tween(durationMillis = 600),
        label = "heatmap",
    )
    LaunchedEffect(data) { animTarget = 1f }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(cellSize * 5 + gap * 4),
    ) {
        val cellPx = cellSize.toPx()
        val gapPx = gap.toPx()
        val stride = cellPx + gapPx

        data.forEachIndexed { index, (_, count) ->
            val col = index / 5
            val row = index % 5
            val fraction = (count.toFloat() / maxCount) * animProgress
            val color = lerp(emptyColor, baseColor, fraction)

            drawRoundRect(
                color = color,
                topLeft = Offset(col * stride, row * stride),
                size = Size(cellPx, cellPx),
                cornerRadius = CornerRadius(3.dp.toPx()),
            )
        }
    }
}
