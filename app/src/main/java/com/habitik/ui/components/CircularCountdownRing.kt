package com.habitik.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CircularCountdownRing(
    progress     : Float,        // 0.0 → 1.0
    remainingTime: String,
    gradient     : Brush,
    modifier     : Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label         = "RingProgress"
    )

    // Continuous subtle pulse on the glow
    val infiniteTransition = rememberInfiniteTransition(label = "ringPulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.25f,
        targetValue   = 0.45f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "glowAlpha"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            val strokeMain  = 14.dp.toPx()
            val strokeGlow1 = strokeMain * 2.4f
            val strokeGlow2 = strokeMain * 1.5f

            // Track ring (dim)
            drawArc(
                color      = Color.White.copy(alpha = 0.08f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter  = false,
                style      = Stroke(width = strokeMain, cap = StrokeCap.Round)
            )

            // Outer bloom
            drawArc(
                brush      = gradient,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter  = false,
                style      = Stroke(width = strokeGlow1, cap = StrokeCap.Round),
                alpha      = glowAlpha
            )

            // Mid glow
            drawArc(
                brush      = gradient,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter  = false,
                style      = Stroke(width = strokeGlow2, cap = StrokeCap.Round),
                alpha      = 0.55f
            )

            // Main progress arc
            drawArc(
                brush      = gradient,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter  = false,
                style      = Stroke(width = strokeMain, cap = StrokeCap.Round)
            )
        }

        // Text overlay
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text          = remainingTime,
                style         = MaterialTheme.typography.displayMedium,
                fontWeight    = FontWeight.Black,
                color         = Color.White,
                letterSpacing = (-1.5).sp
            )
            Text(
                text          = "REMAINING",
                style         = MaterialTheme.typography.labelSmall,
                color         = Color.White.copy(alpha = 0.55f),
                fontWeight    = FontWeight.Bold,
                letterSpacing = 3.sp
            )
        }
    }
}
