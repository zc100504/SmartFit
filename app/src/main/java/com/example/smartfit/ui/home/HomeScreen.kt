// app/src/main/java/com/example/smartfit/ui/home/HomeScreen.kt
package com.example.smartfit.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.smartfit.ui.icon.SmartFitIcons
import com.example.smartfit.ui.navigation.Dest
import com.example.smartfit.ui.theme.*
import com.example.smartfit.ui.AppViewModelProvider



@Composable
fun HomeScreen(
    navController: NavHostController,
    contentPadding: PaddingValues,
    onSummaryClick: () -> Unit,
    isTabletLandscape: Boolean,
    onOpenLogDetailInDetail: (Long, String) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeContent(
        uiState = uiState,
        onPeriodChange = viewModel::onPeriodChange,
        onFilterChange = viewModel::onFilterChange,
        modifier = Modifier.padding(contentPadding),
        onSummaryClick = onSummaryClick,
        onActivityClick = { item ->
            val typeString = when (item.type) {
                ActivityType.EXERCISE -> "activity"
                ActivityType.FOOD -> "food"
            }

            if (isTabletLandscape) {
                // Tablet landscape → open in right detail pane
                onOpenLogDetailInDetail(item.id, typeString)
            } else {
                // Phone / portrait → normal navigation to LogDetail
                navController.navigate(
                    Dest.LogDetail(
                        id = item.id,
                        type = typeString
                    )
                )
            }
        }
    )
}



@Composable
fun HomeContent(
    uiState: HomeUiState,
    onPeriodChange: (ActivityPeriod) -> Unit,
    onFilterChange: (ActivityFilter) -> Unit,
    modifier: Modifier = Modifier,
    onSummaryClick: () -> Unit,
    onActivityClick: (ActivityItemUiState) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    // Compute filtered list once, then feed into LazyColumn.items()
    val listToDisplay = uiState.activities

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            colorScheme.background,
            colorScheme.background.copy(alpha = 0.9f)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HomeTopBar(
                        userName = uiState.userName,
                        avatarUrl = uiState.avatarUrl
                    )
                }

                item {
                    ActivityPeriodTabs(
                        current = uiState.summary.period,
                        onPeriodChange = onPeriodChange
                    )
                }

                item {
                    ActivitySummaryCard(
                        summary = uiState.summary,
                        onViewDetail = onSummaryClick
                    )
                }

                item {
                    RecentlyActivityHeader(
                        uiState = uiState,
                        onFilterChange = onFilterChange
                    )
                }

                // Section list: each activity item
                items(listToDisplay) { item ->
                    ActivityItemCard(
                        item = item,
                        onClick = { onActivityClick(item) }
                    )
                }

                item {
                    SmartFitTipsCard(tips = uiState.tips)
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    userName: String = "John Smith",
    avatarUrl: String = "",
    onNotificationsClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: avatar + welcome text
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular avatar placeholder: initials + gradient background
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(LimePrimarySoft, LimePrimary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (avatarUrl.isNotBlank()) {
                    // Show real avatar from URL
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "User avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    // Fallback: initials avatar
                    Text(
                        text = userName.firstOrNull()?.uppercase() ?: "S",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF020617),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Welcome to SmartFit,",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Text(
                    text = "$userName!",
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Right: notification icon bubble
        /*
        SmallIconBubble(
            icon = SmartFitIcons.Notifications,
            contentDescription = "Notifications",
            onClick = onNotificationsClick
        )*/
    }
}

@Composable
private fun ActivityPeriodTabs(
    current: ActivityPeriod,
    onPeriodChange: (ActivityPeriod) -> Unit
) {
    val isDark = isSmartFitDarkTheme()

    val containerColor =
        if (isDark) DarkSurfaceGlass else LightSurfaceGlass

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PeriodChip(
                text = "Daily Activity",
                selected = current == ActivityPeriod.DAILY,
                modifier = Modifier.weight(1f),
                onClick = { onPeriodChange(ActivityPeriod.DAILY) }
            )
            PeriodChip(
                text = "Weekly Activity",
                selected = current == ActivityPeriod.WEEKLY,
                modifier = Modifier.weight(1f),
                onClick = { onPeriodChange(ActivityPeriod.WEEKLY) }
            )
        }
    }
}

@Composable
private fun PeriodChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSmartFitDarkTheme()

    val bgColor: Color
    val border: BorderStroke?
    val textColor: Color

    if (selected) {
        if (isDark) {
            // Dark: subtle glass background + neon lime text/border
            bgColor = Color.White.copy(alpha = 0.08f)
            border = BorderStroke(1.dp, colorScheme.primary)
            textColor = colorScheme.primary
        } else {
            // Light: lime background + dark text
            bgColor = colorScheme.primary.copy(alpha = 0.28f)
            border = BorderStroke(1.dp, colorScheme.primary)
            textColor = Color(0xFF020617)
        }
    } else {
        bgColor = Color.Transparent
        border = null
        textColor = colorScheme.onSurface.copy(alpha = 0.7f)
    }

    Surface(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(999.dp),
        color = bgColor,
        border = border
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

@Composable
private fun ActivitySummaryCard(
    summary: ActivitySummaryUiState,
    onViewDetail: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSmartFitDarkTheme()

    val surfaceColor =
        if (isDark) colorScheme.surface.copy(alpha = 0.35f)
        else Color.White.copy(alpha = 0.94f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(26.dp),
                clip = false
            ),
        shape = RoundedCornerShape(26.dp),
        color = surfaceColor,
        tonalElevation = 0.dp,
        border = BorderStroke(
            1.dp,
            if (isDark) Color.White.copy(alpha = 0.08f)
            else Color.White.copy(alpha = 0.65f)
        )
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(
                                Color.White.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.7f),
                                Color.Transparent
                            )
                        }
                    )
                )
                .padding(20.dp)
        ) {
            // Header: title + "View detail"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (summary.period == ActivityPeriod.DAILY)
                        "Today’s Summary"
                    else
                        "This Week’s Summary",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface
                )

                TextButton(
                    onClick = onViewDetail,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "View detail",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isDark) {
                            colorScheme.primary     // high contrast in dark mode
                        } else {
                            Color(0xFF5A9D09)      // slightly darker green in light mode
                        }
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem("Active Time", "${summary.activeMinutes} min")
                SummaryItem("Calories Burned", "${summary.caloriesBurned} kcal")
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                SummaryItem("Calories Intake", "${summary.caloriesIntake} kcal")
            }

        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RecentlyActivityHeader(
    uiState: HomeUiState,
    onFilterChange: (ActivityFilter) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSmartFitDarkTheme()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recently Activity",
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onBackground
            )
            TextButton(onClick = { /* TODO: navigate to all activities */ }) {
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isDark) {
                        colorScheme.primary
                    } else {
                        Color(0xFF5A9D09)
                    }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        FilterSegment(
            current = uiState.filter,
            onFilterChange = onFilterChange
        )

        Spacer(Modifier.height(12.dp))
    }
}




@Composable
private fun FilterSegment(
    current: ActivityFilter,
    onFilterChange: (ActivityFilter) -> Unit
) {
    val isDark = isSmartFitDarkTheme()
    val containerColor =
        if (isDark) DarkSurfaceGlass else LightSurfaceGlass

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                text = "All",
                selected = current == ActivityFilter.ALL,
                modifier = Modifier.weight(1f),
                onClick = { onFilterChange(ActivityFilter.ALL) }
            )
            FilterChip(
                text = "Exercise",
                selected = current == ActivityFilter.EXERCISE,
                modifier = Modifier.weight(1f),
                onClick = { onFilterChange(ActivityFilter.EXERCISE) }
            )
            FilterChip(
                text = "Food",
                selected = current == ActivityFilter.FOOD,
                modifier = Modifier.weight(1f),
                onClick = { onFilterChange(ActivityFilter.FOOD) }
            )
        }
    }
}

@Composable
private fun FilterChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        modifier = modifier.height(32.dp),
        shape = RoundedCornerShape(999.dp),
        color = if (selected)
            colorScheme.primary.copy(alpha = 0.60f)
        else
            Color.Transparent
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected)
                    Color(0xFF020617)      // dark text when selected (better in light mode)
                else
                    colorScheme.onSurface.copy(alpha = 0.7f),
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ActivityItemCard(
    item: ActivityItemUiState,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSmartFitDarkTheme()

    val surfaceColor =
        if (isDark) colorScheme.surface.copy(alpha = 0.45f)
        else Color.White.copy(alpha = 0.96f)

    Surface(
        onClick = onClick,   // <-- clickable card
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = surfaceColor,
        tonalElevation = 0.dp,
        border = BorderStroke(
            1.dp,
            if (isDark) Color.White.copy(alpha = 0.1f)
            else Color.White.copy(alpha = 0.55f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBubble(item)

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = item.caloriesText,
                style = MaterialTheme.typography.labelMedium,
                color = if (item.type == ActivityType.FOOD)
                    AccentRed
                else
                    colorScheme.secondary
            )
        }
    }
}

@Composable
private fun IconBubble(item: ActivityItemUiState) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSmartFitDarkTheme()

    val icon = when (item.icon) {
        ActivityIcon.RUNNING -> SmartFitIcons.Running
        ActivityIcon.CYCLING -> SmartFitIcons.Cycling
        ActivityIcon.FOOD    -> SmartFitIcons.Food
        ActivityIcon.DRINK   -> SmartFitIcons.Drink
    }

    val iconBgGradient = when (item.type) {
        ActivityType.EXERCISE -> Brush.linearGradient(
            listOf(LimePrimarySoft, LimePrimary)
        )
        ActivityType.FOOD -> Brush.linearGradient(
            listOf(AccentYellow.copy(alpha = 0.2f), AccentYellow)
        )
    }

    val iconTint = if (isDark) {
        Color.White                // white icons in dark mode
    } else {
        Color(0xFF020617)          // near-black icons in light mode
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(iconBgGradient),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint
        )
    }
}

@Composable
private fun SmartFitTipsCard(tips: String) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSmartFitDarkTheme()

    val surfaceColor =
        if (isDark) colorScheme.surface.copy(alpha = 0.45f)
        else Color.White.copy(alpha = 0.96f)

    val iconTint = if (isDark) {
        Color.White
    } else {
        Color(0xFF020617)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(26.dp),
        color = surfaceColor,
        tonalElevation = 0.dp,
        border = BorderStroke(
            1.dp,
            if (isDark) Color.White.copy(alpha = 0.1f)
            else Color.White.copy(alpha = 0.5f)
        )
    ) {
        Column (
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = SmartFitIcons.Tips,
                        contentDescription = "Tips",
                        tint = iconTint
                    )
                }
                Text(
                    text = "SmartFit Tips",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = tips,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Justify,
                color = colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

/*
@Composable
private fun SmallIconBubble(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSmartFitDarkTheme()

    val iconTint = if (isDark) {
        colorScheme.primary            // neon lime in dark mode
    } else {
        Color(0xFF7BB82F)             // softer green in light mode
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        color = colorScheme.surface.copy(alpha = 0.6f),
        tonalElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint
            )
        }
    }
}
*/