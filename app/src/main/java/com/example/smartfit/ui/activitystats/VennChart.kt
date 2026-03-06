// app/src/main/java/com/example/smartfit/ui/activitystats/VennChart.kt
package com.example.smartfit.ui.activitystats

import android.content.res.Configuration
import androidx.compose.foundation.background
import com.example.smartfit.ui.theme.isSmartFitDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smartfit.ui.theme.*
import kotlin.math.min
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize



@Composable
fun VennChart(
    modifier: Modifier = Modifier,
    values: List<Float>,
    colors: List<Color>
) {
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        if (values.isEmpty()) return@Canvas

        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)
        val strokeWidth = min(w, h) * 0.08f
        val spacing = strokeWidth * 0.35f

        var radius = min(w, h) / 2f - strokeWidth / 2f

        for (i in values.indices) {
            val progress = values[i].coerceIn(0f, 1f)
            val color = colors.getOrNull(i) ?: Color.Black

            val left = center.x - radius
            val top = center.y - radius
            val sizeCircle = Size(radius * 2f, radius * 2f)

            val startAngle = 210f
            val sweepAngle = 240f * progress

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(left, top),
                size = sizeCircle,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            radius -= (strokeWidth + spacing)
            if (radius <= 0f) break
        }
    }
}


@Composable
fun vennColors(): List<Color> {
    val isDark = isSmartFitDarkTheme()

    return if (isDark) {
        listOf(
            LimePrimary,        // Workouts
            LimeSoft,           // Active minutes
            Color(0xFF73A9F9),  // Intake
            AccentRed           // Burned
        )
    } else {
        listOf(
            LimePrimary,        // Workouts
            LimeDark,           // Active minutes
            Color(0xFF73A9F9),  // Intake
            AccentRed           // Burned
        )
    }
}



@Preview
    (showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun VennChartPreviewLight() {
    SmartFitTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .background(LightBgSoft),
            contentAlignment = Alignment.Center
        ) {
            VennChart(
                modifier = Modifier.size(200.dp),
                values = listOf(0.85f, 0.65f, 0.45f, 0.30f),
                colors = vennColors()
            )
        }
    }
}


@Preview
    (showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun VennChartPreviewDark() {
    SmartFitTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .background(DarkBg),
            contentAlignment = Alignment.Center
        ) {
            VennChart(
                modifier = Modifier.size(200.dp),
                values = listOf(0.85f, 0.65f, 0.45f, 0.30f),
                colors = vennColors()
            )
        }
    }
}
