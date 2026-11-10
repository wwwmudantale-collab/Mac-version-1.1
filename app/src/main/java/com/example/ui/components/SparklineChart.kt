package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun SparklineChart(
    points: List<Double>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    fillGradient: Boolean = true
) {
    if (points.size < 2) return

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val minVal = points.minOrNull() ?: 0.0
        val maxVal = points.maxOrNull() ?: 1.0
        val range = if (maxVal - minVal > 0) maxVal - minVal else 1.0

        val stepX = width / (points.size - 1)

        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { index, value ->
            val x = index * stepX
            val normalizedY = (value - minVal) / range
            val y = (height - (normalizedY * height * 0.85f) - (height * 0.08f)).toFloat()

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                val prevX = (index - 1) * stepX
                val prevNormY = (points[index - 1] - minVal) / range
                val prevY = (height - (prevNormY * height * 0.85f) - (height * 0.08f)).toFloat()

                val controlX1 = prevX + (x - prevX) / 2
                val controlY1 = prevY
                val controlX2 = prevX + (x - prevX) / 2
                val controlY2 = y

                path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
            }
        }

        if (fillGradient) {
            fillPath.lineTo(width, height)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.25f),
                        Color.Transparent
                    )
                )
            )
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}
