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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BarChart(
    data: List<Pair<String, Float>>,       // label → value
    barColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 160.dp,
) {
    val textMeasurer = rememberTextMeasurer()
    val maxValue = data.maxOfOrNull { it.second }?.takeIf { it > 0f } ?: 1f

    var animTarget by remember { mutableFloatStateOf(0f) }
    val animProgress by animateFloatAsState(
        targetValue = animTarget,
        animationSpec = tween(durationMillis = 700),
        label = "bar_chart",
    )
    LaunchedEffect(data) { animTarget = 1f }

    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        val labelHeight = 20.dp.toPx()
        val chartHeight = size.height - labelHeight
        val barWidth = (size.width / data.size) * 0.6f
        val gap = (size.width / data.size) * 0.4f / 2f
        val slotWidth = size.width / data.size

        data.forEachIndexed { index, (label, value) ->
            val barHeightPx = chartHeight * (value / maxValue) * animProgress
            val x = index * slotWidth + gap

            if (barHeightPx > 0f) {
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, chartHeight - barHeightPx),
                    size = Size(barWidth, barHeightPx),
                    cornerRadius = CornerRadius(4.dp.toPx()),
                )
            }

            val textLayout = textMeasurer.measure(
                text = label,
                style = TextStyle(color = labelColor, fontSize = 10.sp),
            )
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(
                    x + barWidth / 2 - textLayout.size.width / 2,
                    chartHeight + 4.dp.toPx(),
                ),
            )
        }
    }
}
