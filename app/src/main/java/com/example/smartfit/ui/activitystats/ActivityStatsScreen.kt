// app/src/main/java/com/example/smartfit/ui/activitystats/ActivityStatsScreen.kt
package com.example.smartfit.ui.activitystats

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smartfit.ui.theme.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow



/** Main statistics screen: “Activity Summary” */
@Composable
fun ActivityStatsScreen(
    uiState: ActivityStatsUiState,
    onBackClick: () -> Unit,
    onPeriodChange: (StatsPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSmartFitDarkTheme()

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            if (isDark) DarkBg else LightBg,
            if (isDark) DarkBgSoft else LightBgSoft
        )
    )

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Surface(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize()
                .navigationBarsPadding(),
            shape = RoundedCornerShape(30.dp),
            color = if (isDark) DarkBgSoft else GlassLight,
            tonalElevation = 0.dp,
            border = if (isDark) {
                BorderStroke(1.dp, GlassBorderDark)
            } else {
                BorderStroke(1.dp, GlassBorderLight)
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                StatsTopBar(
                    title = "Activity Summary",
                    dateLabel = uiState.dateLabel,
                    onBackClick = onBackClick
                )

                StatsPeriodTabs(
                    current = uiState.period,
                    onPeriodChange = onPeriodChange
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)                  // take remaining height
                        .verticalScroll(scrollState), // scroll inside card
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ){
                    StatsHeroCard(uiState = uiState)

                    Spacer(Modifier.height(16.dp))

                    // Active Time
                    val distanceLabels = when (uiState.period) {
                        StatsPeriod.DAY -> uiState.distancePoints.indices.map { "A${it + 1}" }   // Activity 1..N
                        StatsPeriod.WEEK -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    }

                    StatsChartCard(
                        title = when (uiState.period) {
                            StatsPeriod.DAY -> "Active Time Trend"
                            StatsPeriod.WEEK -> "Weekly Active Time"
                        },
                        subtitle = when (uiState.period) {
                            StatsPeriod.DAY -> "Each point = active minutes in one session today"
                            StatsPeriod.WEEK -> "Each point = total active minutes per day (Mon–Sun)"
                        }
                    ) {
                        DistanceLineChart(
                            values = uiState.distancePoints,   // ✅ 用真实分钟
                            xLabels = distanceLabels
                        )
                    }


                    Spacer(Modifier.height(16.dp))

                    // Calories Burned
                    val caloriesLabels = when (uiState.period) {
                        StatsPeriod.DAY -> uiState.caloriesBurnedPoints.indices.map { "A${it + 1}" }
                        StatsPeriod.WEEK -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    }

                    StatsChartCard(
                        title = when (uiState.period) {
                            StatsPeriod.DAY -> "Calories Burned (Daily)"
                            StatsPeriod.WEEK -> "Calories Burned (Weekly)"
                        },
                        subtitle = when (uiState.period) {
                            StatsPeriod.DAY -> "Each bar = one activity session (kcal)"
                            StatsPeriod.WEEK -> "Each bar = total calories burned per day (Mon–Sun)"
                        }
                    ) {
                        CaloriesBarChart(
                            values = uiState.caloriesBurnedPoints,
                            xLabels = caloriesLabels
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    DailyGoalPieCard(
                        period = uiState.period,
                        intakeKcal = uiState.caloriesIntake,
                        burnedKcal = uiState.caloriesBurned
                    )
                }
            }
        }
    }
}

/* ------------ Top bar (back + title + date) ------------ */

@Composable
private fun StatsTopBar(
    title: String,
    dateLabel: String,
    onBackClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colorScheme.onBackground
            )
        }

        Spacer(Modifier.width(4.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = dateLabel,
                modifier = Modifier.testTag("stats_Date"),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

/* ------------ Daily / Weekly segmented control ------------ */

@Composable
private fun StatsPeriodTabs(
    current: StatsPeriod,
    onPeriodChange: (StatsPeriod) -> Unit
) {
    val isDark = isSmartFitDarkTheme()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(999.dp),
        color = if (isDark) GlassDark else GlassLight,
        border = BorderStroke(1.dp, LimePrimary)
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatsPeriodChip(
                text = "Daily",
                selected = current == StatsPeriod.DAY,
                modifier = Modifier.weight(1f),
                onClick = { onPeriodChange(StatsPeriod.DAY) }
            )
            StatsPeriodChip(
                text = "Weekly",
                selected = current == StatsPeriod.WEEK,
                modifier = Modifier.weight(1f),
                onClick = { onPeriodChange(StatsPeriod.WEEK) }
            )
        }
    }
}

@Composable
private fun StatsPeriodChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = isSmartFitDarkTheme()

    val bgColor: Color
    val textColor: Color

    if (selected) {
        bgColor = LimePrimary
        textColor = if (isDark) Color.Black else Color(0xFF020617)
    } else {
        bgColor = Color.Transparent
        textColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B)
    }

    Surface(
        onClick = onClick,
        modifier = modifier.height(32.dp),
        shape = RoundedCornerShape(999.dp),
        color = bgColor
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

/* ------------ Hero gradient card ------------ */

@Composable
private fun StatsHeroCard(
    uiState: ActivityStatsUiState
) {
    val isDark = isSmartFitDarkTheme()

    val gradient = if (isDark) {
        Brush.linearGradient(
            listOf(
                LimePrimary.copy(alpha = 0.98f),
                LimePrimarySoft.copy(alpha = 0.90f),
                Color(0xFF22D3EE).copy(alpha = 0.80f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                LimePrimary.copy(alpha = 0.85f),
                LimePrimarySoft.copy(alpha = 0.80f),
                Color(0xFF9AE6FF).copy(alpha = 0.70f)
            )
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .shadow(4.dp, shape = RoundedCornerShape(26.dp)),
        shape = RoundedCornerShape(26.dp),
        color = Color.Transparent,
        border = if (isDark) BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)) else null
    ) {
        Box(
            modifier = Modifier.background(gradient)
        ) {
            // Frosted overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            if (!isDark) {
                                listOf(
                                    Color.White.copy(alpha = 0.82f),
                                    Color.White.copy(alpha = 0.55f),
                                    Color.Transparent
                                )
                            } else {
                                listOf(
                                    Color.DarkGray.copy(alpha = 0.50f),
                                    Color.DarkGray.copy(alpha = 0.20f),
                                    Color.Transparent
                                )
                            }
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val titleColor = if (isDark) Color.White else Color(0xFF020617)
                val unitColor = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF4B5563)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Today",
                            modifier = Modifier.testTag("stats_title"),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark)
                                Color.White.copy(alpha = 0.9f)
                            else
                                Color(0xFF6B7280)
                        )
                        Text(
                            text = if (uiState.period == StatsPeriod.DAY)
                                "Daily Activity"
                            else
                                "Weekly Activity",
                            style = MaterialTheme.typography.titleMedium,
                            color = titleColor
                        )
                    }

                    Surface(
                        modifier = Modifier.size(30.dp),
                        shape = CircleShape,
                        color = if (isDark)
                            Color.White.copy(alpha = 0.18f)
                        else
                            Color.White.copy(alpha = 0.9f),
                        tonalElevation = 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (uiState.period == StatsPeriod.DAY) "D" else "W",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isDark) Color.White else Color(0xFF020617)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format("%.2f", uiState.totalDistanceKm),
                        style = MaterialTheme.typography.displaySmall,
                        color = if (isDark) Color.White else Color(0xFF020617),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "km",
                        style = MaterialTheme.typography.titleSmall,
                        color = unitColor
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HeroStatPill(
                        modifier = Modifier.weight(1f).testTag("stats_duration"),
                        label = "Duration",
                        value = "${uiState.totalDurationMinutes} min"
                    )
                    HeroStatPill(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stats_intake_label"),
                        label = "Intake",
                        value = "${uiState.caloriesIntake} kcal"
                    )
                    HeroStatPill(
                        modifier = Modifier.weight(1f).testTag("stats_burned_label"),
                        label = "Burned",
                        value = "${uiState.caloriesBurned} kcal"
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroStatPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    val isDark = isSmartFitDarkTheme()

    val bgColor: Color
    val borderColor: Color
    val labelColor: Color
    val valueColor: Color

    if (isDark) {
        bgColor = Color.White.copy(alpha = 0.10f)
        borderColor = Color.White.copy(alpha = 0.28f)
        labelColor = Color.White.copy(alpha = 0.78f)
        valueColor = Color.White
    } else {
        bgColor = Color.White.copy(alpha = 0.58f)
        borderColor = Color.White.copy(alpha = 0.70f)
        labelColor = Color(0xFFA0AEC0)
        valueColor = Color(0xFF020617)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (isDark) 0.18f else 0.35f),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = valueColor,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/* ------------ Chart card ------------ */

@Composable
private fun StatsChartCard(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSmartFitDarkTheme()
    val innerColor = if (isDark) DarkSurfaceGlass else Color.White

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .shadow(
                elevation = if (isDark) 18.dp else 26.dp,
                shape = RoundedCornerShape(28.dp),
                clip = false
            ),
        shape = RoundedCornerShape(28.dp),
        border = if (isDark) {
            BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
        } else {
            BorderStroke(1.dp, GlassBorderLight)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isDark) DarkOnSurface else LightOnSurface
            )

            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(innerColor)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                content = content
            )
        }
    }
}


/* ------------ Distance line chart ------------ */

@Composable
private fun DistanceLineChart(
    values: List<Float>,
    xLabels: List<String>
) {
    val isDark = isSmartFitDarkTheme()
    val gridColor = if (isDark) {
        Color.White.copy(alpha = 0.10f)
    } else {
        Color(0xFFE5E7EB)
    }
    val lineColor = if (isDark) ChartGlow else LimePrimary
    val labelColor = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF64748B)
    val textStyle = MaterialTheme.typography.labelSmall

    if (values.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No activity recorded in this period",
                style = MaterialTheme.typography.bodySmall,
                color = labelColor
            )
        }
        return
    }

    val safeLabels = if (xLabels.size == values.size) {
        xLabels
    } else {
        values.indices.map { "P${it + 1}" }
    }


    val ticks = computeAxisTicks(values)
    val axisMax = ticks.last().coerceAtLeast(1f)

    Column(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 40.dp, end = 8.dp, top = 4.dp, bottom = 12.dp)
            ) {
                val w = size.width
                val h = size.height

                // 背景横线：0、mid、max
                ticks.forEach { tick ->
                    val fraction = tick / axisMax
                    val y = h * (1f - fraction)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f
                    )
                }

                val count = values.size
                val stepX = w / count.toFloat()

                val path = Path()
                values.forEachIndexed { index, v ->
                    val fraction = (v / axisMax).coerceIn(0f, 1f)
                    val centerX = stepX * (index + 0.5f)
                    val y = h * (1f - fraction)
                    if (index == 0) {
                        path.moveTo(centerX, y)
                    } else {
                        path.lineTo(centerX, y)
                    }
                }

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 4f)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp, top = 4.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                val bottom = ticks[0]
                val mid = ticks[1]
                val top = ticks[2]

                Text(
                    text = top.toInt().toString(),
                    style = textStyle,
                    color = labelColor
                )
                Text(
                    text = mid.toInt().toString(),
                    style = textStyle,
                    color = labelColor
                )


                Column {
                    Text(
                        text = bottom.toInt().toString(),
                        style = textStyle,
                        color = labelColor
                    )
                    Text(
                        text = "min",
                        style = textStyle,
                        color = labelColor
                    )
                }
            }
        }

            Spacer(Modifier.height(4.dp))


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 40.dp, end = 8.dp)
        ) {
            safeLabels.forEach { label ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = textStyle,
                        color = labelColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}





/* ------------ Calories bar chart ------------ */

@Composable
private fun CaloriesBarChart(
    values: List<Float>,
    xLabels: List<String>
) {
    val isDark = isSmartFitDarkTheme()
    val labelColor = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF64748B)
    val textStyle = MaterialTheme.typography.labelSmall

    if (values.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No calories logged in this period",
                style = MaterialTheme.typography.bodySmall,
                color = labelColor
            )
        }
        return
    }

    val barColor = if (isDark) AccentRed.copy(alpha = 0.9f) else AccentRed
    val bgGrid = if (isDark) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color(0xFFE2E8F0)
    }

    val safeLabels = if (xLabels.size == values.size) {
        xLabels
    } else {
        values.indices.map { "P${it + 1}" }
    }


    val ticks = computeAxisTicks(values)
    val axisMax = ticks.last().coerceAtLeast(1f)

    Column(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 40.dp, end = 8.dp, top = 4.dp, bottom = 12.dp)
            ) {
                val w = size.width
                val h = size.height

                // 0 / mid / max
                ticks.forEach { tick ->
                    val fraction = tick / axisMax
                    val y = h * (1f - fraction)
                    drawLine(bgGrid, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                }

                val count = values.size
                val stepX = w / count.toFloat()
                val barWidth = stepX * 0.5f

                values.forEachIndexed { index, v ->
                    val fraction = (v / axisMax).coerceIn(0f, 1f)
                    val barHeight = h * fraction

                    val centerX = stepX * (index + 0.5f)
                    val left = centerX - barWidth / 2f
                    val top = h - barHeight
                    val right = centerX + barWidth / 2f
                    val bottom = h

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(left, top),
                        size = Size(
                            width = barWidth,
                            height = barHeight
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp, top = 4.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val bottom = ticks[0]
                val mid = ticks[1]
                val top = ticks[2]

                Text(
                    text = top.toInt().toString(),
                    style = textStyle,
                    color = labelColor
                )
                Text(
                    text = mid.toInt().toString(),
                    style = textStyle,
                    color = labelColor
                )

                Column {
                    Text(
                        text = bottom.toInt().toString(),
                        style = textStyle,
                        color = labelColor
                    )
                    Text(
                        text = "kcal",
                        style = textStyle,
                        color = labelColor
                    )
                }
            }

        }

        Spacer(Modifier.height(4.dp))


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 40.dp, end = 8.dp)
        ) {
            safeLabels.forEach { label ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = textStyle,
                        color = labelColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}




/* ------------ PieCard (uses VennChart) ------------ */

@Composable
fun DailyGoalPieCard(
    modifier: Modifier = Modifier,
    period: StatsPeriod,
    intakeKcal: Int,
    burnedKcal: Int
) {
    val isDark = isSmartFitDarkTheme()
    val total = (intakeKcal + burnedKcal).coerceAtLeast(1)
    val colors = listOf(
        Color(0xFF22C55E), // Burned
        Color(0xFF3B82F6)  // Intake
    )

    val title = when (period) {
        StatsPeriod.DAY -> "Daily Calories Balance"
        StatsPeriod.WEEK -> "Weekly Calories Balance"
    }

    val subtitle = when (period) {
        StatsPeriod.DAY -> "Intake vs burned for today"
        StatsPeriod.WEEK -> "Total intake vs burned for this week"
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        color = if (isDark) DarkBgSoft else Color.White,
        tonalElevation = 0.dp,
        border = BorderStroke(
            1.dp,
            if (isDark) Color.White.copy(alpha = 0.12f) else GlassBorderLight
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isDark) DarkOnSurface else LightOnSurface
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val sweepBurned = 360f * burnedKcal / total.toFloat()
                        val sweepIntake = 360f - sweepBurned

                        val diameter = size.minDimension
                        val topLeft = Offset(
                            (size.width - diameter) / 2f,
                            (size.height - diameter) / 2f
                        )
                        val pieSize = Size(diameter, diameter)

                        // Intake
                        drawArc(
                            color = colors[1],
                            startAngle = -90f + sweepBurned,
                            sweepAngle = sweepIntake,
                            useCenter = true,
                            topLeft = topLeft,
                            size = pieSize
                        )

                        // Burned
                        drawArc(
                            color = colors[0],
                            startAngle = -90f,
                            sweepAngle = sweepBurned,
                            useCenter = true,
                            topLeft = topLeft,
                            size = pieSize
                        )
                    }

                    val net = burnedKcal - intakeKcal
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = net.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isDark) Color.White else Color(0xFF020617)
                        )
                        Text(
                            text = "net kcal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GoalLegendDot("Burned", colors[0])
                    Text(
                        text = "$burnedKcal kcal burned",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    GoalLegendDot("Intake", colors[1])
                    Text(
                        text = "$intakeKcal kcal intake",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalLegendDot(
    label: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// 计算「好看」的 Y 轴刻度，比如 0 / 10 / 20 或 0 / 100 / 200
private fun computeAxisTicks(values: List<Float>): List<Float> {
    val maxVal = (values.maxOrNull() ?: 0f).coerceAtLeast(1f)

    // 例如：23 -> 10^1 = 10, n = 2.3 -> 2 * 10 = 20; 265 -> 10^2 = 100, n=2.65 -> 3*10^2=300
    val exp = floor(log10(maxVal.toDouble())).toInt()
    val base = 10.0.pow(exp).toFloat()
    val n = maxVal / base

    val niceMultiplier = when {
        n <= 1f -> 1f
        n <= 2f -> 2f
        n <= 5f -> 5f
        else -> 10f
    }

    val niceMax = niceMultiplier * base           // 顶部刻度，例如 20、50、300
    val mid = niceMax / 2f

    return listOf(0f, mid, niceMax)
}


/* ------------ Demo + previews ------------ */

@Composable
fun DailyActivityStatsDemo() {
    val demo = ActivityStatsUiState(
        period = StatsPeriod.DAY,
        dateLabel = "Today",
        totalDurationMinutes = 54,
        totalDistanceKm = 4.32f,
        caloriesIntake = 1850,
        caloriesBurned = 420,
        distancePoints = listOf(0.1f, 0.4f, 0.7f, 0.5f, 0.9f, 0.6f),
        caloriesBurnedPoints = listOf(120f, 180f, 160f, 220f, 210f, 190f, 230f),
        stepsPoints = listOf(0.2f, 0.5f, 0.7f, 0.6f, 0.9f, 0.8f),
        currentSteps = 7543,
        goalSteps = 10000
    )

    ActivityStatsScreen(
        uiState = demo,
        onBackClick = {},
        onPeriodChange = {}
    )
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun ActivityStatsPreviewLight() {
    SmartFitTheme(darkTheme = false) {
        DailyActivityStatsDemo()
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun ActivityStatsPreviewDark() {
    SmartFitTheme(darkTheme = true) {
        DailyActivityStatsDemo()
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun ChartPreviewLight() {
    SmartFitTheme(darkTheme = false) {
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun ChartPreviewDark() {
    SmartFitTheme(darkTheme = true) {

    }
}
