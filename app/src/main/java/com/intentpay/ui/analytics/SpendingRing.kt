package com.intentpay.ui.analytics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intentpay.ui.theme.TextSecondary

@Composable
fun SpendingRing(
    spends: List<CategorySpend>,
    totalSpent: Double,
    modifier: Modifier = Modifier
) {
    val animatedSweep = remember { Animatable(0f) }

    LaunchedEffect(spends) {
        animatedSweep.snapTo(0f)
        animatedSweep.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 24.dp.toPx()
            val sizePx = size.minDimension
            val rectSize = Size(sizePx - strokeWidth, sizePx - strokeWidth)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            if (spends.isEmpty()) {
                drawArc(
                    color = Color(0x33FFFFFF), // Empty state
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = rectSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                return@Canvas
            }

            var currentStartAngle = -90f // Start at top

            spends.forEach { spend ->
                val sweepAngle = spend.percentage * 360f * animatedSweep.value
                val color = Color(spend.category.colorHex)

                drawArc(
                    color = color,
                    startAngle = currentStartAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = rectSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                currentStartAngle += sweepAngle
            }
        }

        // Total spending text in the middle
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Total Spent", color = TextSecondary, fontSize = 14.sp)
            Text("₹$totalSpent", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    }
}
