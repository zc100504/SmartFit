package com.example.smartfit.ui.logs

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartfit.data.model.ActivityLog
import com.example.smartfit.data.model.FoodLog
import com.example.smartfit.data.model.LogItem
import com.example.smartfit.ui.AppViewModelProvider
import com.example.smartfit.ui.navigation.Dest
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.smartfit.ui.theme.isSmartFitDarkTheme



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    navController: NavHostController,
    windowSizeClass: WindowSizeClass,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    isDarkTheme: Boolean,
    isTabletLandscape: Boolean,                          // ✅ 新增
    onOpenLogDetailInDetail: (Long, String) -> Unit      // ✅ 新增
) {
    val vm: LogsViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val ui by vm.state.collectAsState()

    // ------------ Local UI state (filter + date) ------------
    var selectedFilter by remember { mutableStateOf("all") }
    var selectedDate by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, day: Int ->
                calendar.set(year, month, day)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                selectedDate = sdf.format(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // ------------ Filter logic (type + optional date) ------------
    val filteredItems = remember(ui.items, selectedFilter, selectedDate) {
        val timeRange: Pair<Long, Long>? = selectedDate?.let { dateStr ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.parse(dateStr)?.let { parsed ->
                val cal = Calendar.getInstance()
                cal.time = parsed

                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfDay = cal.timeInMillis

                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val endOfDay = cal.timeInMillis

                startOfDay to endOfDay
            }
        }

        ui.items.filter { log ->
            val matchType = when (selectedFilter) {
                "all" -> true
                "exercise" -> log is ActivityLog
                "food" -> log is FoodLog
                else -> true
            }

            val matchDate = if (timeRange != null) {
                log.timestamp in timeRange.first..timeRange.second
            } else {
                true
            }

            matchType && matchDate
        }
    }

    // ------------ Layout / theming ------------
    val isTablet =
        windowSizeClass.widthSizeClass == WindowWidthSizeClass.Medium ||
                windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSmartFitDarkTheme()

    Scaffold(
        topBar = {
            if (isTablet) {
                TopAppBar(
                    title = { Text("My Activities") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorScheme.background,
                        titleContentColor = colorScheme.onBackground,
                        navigationIconContentColor = colorScheme.onBackground,
                        actionIconContentColor = colorScheme.onBackground
                    )
                )
            }
        },
        containerColor = colorScheme.background,                      // ✅ 整个 scaffold 用背景色
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->

        if (isTablet) {
            // =================== TABLET / LARGE SCREEN ===================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.TopCenter
            ) {
                val cardSurface =
                    if (isDark) colorScheme.surface.copy(alpha = 0.35f)
                    else Color.White.copy(alpha = 0.96f)

                val cardBorder = BorderStroke(
                    1.dp,
                    if (isDark) Color.White.copy(alpha = 0.10f)
                    else Color.White.copy(alpha = 0.60f)
                )

                Card(
                    modifier = Modifier
                        .padding(top = 32.dp)
                        .fillMaxWidth(0.82f)
                        .fillMaxHeight(0.85f),
                    colors = CardDefaults.cardColors(containerColor = cardSurface),
                    shape = RoundedCornerShape(32.dp),
                    border = cardBorder
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 18.dp)
                    ) {
                        // Top row: text tabs + date card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TabletTabs(
                                selectedFilter = selectedFilter,
                                onFilterChange = { selectedFilter = it }
                            )

                            Spacer(Modifier.weight(1f))

                            DateCard(
                                text = selectedDate ?: "Select date",
                                onClick = { datePickerDialog.show() }
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Divider(
                            color = colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )

                        Spacer(Modifier.height(18.dp))

                        LogsList(
                            items = filteredItems,
                            isLoading = ui.isLoading,
                            navController = navController,
                            isDarkTheme = isDarkTheme,
                            isTabletLandscape = isTabletLandscape,
                            onOpenLogDetailInDetail = onOpenLogDetailInDetail
                        )

                    }
                }
            }
        } else {
            // =================== PHONE / SMALL SCREEN ===================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Activities",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )

                    DateCard(
                        text = selectedDate ?: "Select date",
                        onClick = { datePickerDialog.show() }
                    )
                }

                PhoneSegmentedControl(
                    options = listOf("all", "exercise", "food"),
                    selected = selectedFilter,
                    onSelect = { selectedFilter = it }
                )

                Spacer(Modifier.height(24.dp))

                LogsList(
                    items = filteredItems,
                    isLoading = ui.isLoading,
                    navController = navController,
                    isDarkTheme = isDarkTheme,
                    isTabletLandscape = isTabletLandscape,
                    onOpenLogDetailInDetail = onOpenLogDetailInDetail
                )
            }
        }
    }
}



@Composable
private fun TabletTabs(
    selectedFilter: String,
    onFilterChange: (String) -> Unit
) {
    val tabs = listOf("all", "exercise", "food")
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.wrapContentWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, key ->
            val isSelected = key == selectedFilter

            Column(
                modifier = Modifier
                    .padding(end = if (index < tabs.lastIndex) 32.dp else 0.dp)
                    .clickable { onFilterChange(key) },
                // ✅ 关键：里面的 Text 和 下划线 都居中
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = key.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected)
                        colorScheme.onSurface
                    else
                        colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .width(60.dp) // 这条线会以 Column 中心为轴
                        .background(
                            if (isSelected) colorScheme.primary else Color.Transparent
                        )
                )
            }
        }
    }
}




@Composable
private fun DateCard(
    text: String,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSmartFitDarkTheme()

    val surfaceColor =
        if (isDark) colorScheme.surfaceVariant.copy(alpha = 0.9f)
        else Color.White

    val borderColor =
        if (isDark) Color.White.copy(alpha = 0.14f)
        else colorScheme.outline.copy(alpha = 0.6f)

    Surface(
        modifier = Modifier
            .height(36.dp)
            .wrapContentWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, borderColor),
        color = surfaceColor,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Select date",
                modifier = Modifier.size(16.dp),
                tint = colorScheme.onSurfaceVariant
            )
        }
    }
}



@Composable
fun PhoneSegmentedControl(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSmartFitDarkTheme()

    val containerColor =
        if (isDark) colorScheme.surfaceVariant.copy(alpha = 0.7f)
        else colorScheme.surfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(containerColor),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        options.forEachIndexed { index, key ->
            val isSelected = key == selected

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(
                        when (index) {
                            0 -> RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp)
                            options.lastIndex -> RoundedCornerShape(topEnd = 22.dp, bottomEnd = 22.dp)
                            else -> RoundedCornerShape(0.dp)
                        }
                    )
                    .background(
                        if (isSelected) colorScheme.primary else Color.Transparent
                    )
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = if (isSelected)
                            colorScheme.primary.copy(alpha = 0.9f)
                        else
                            Color.Transparent,
                    )
                    .clickable { onSelect(key) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = key.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected)
                        Color(0xFF020617)       // readable on lime in light mode
                    else
                        colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}





@Composable
fun LogsList(
    items: List<LogItem>,
    isLoading: Boolean,
    navController: NavHostController,
    isDarkTheme: Boolean,
    isTabletLandscape: Boolean,                          // ✅
    onOpenLogDetailInDetail: (Long, String) -> Unit      // ✅
) {
    val listState = rememberLazyListState()

    when {
        isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }

        items.isEmpty() -> EmptyState()

        else -> LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = items,
                key = { logItem ->
                    // Combine the item type with its ID
                    when (logItem) {
                        is ActivityLog -> "activity_${logItem.id}"
                        is FoodLog -> "food_${logItem.id}"
                    }
                }
            ) { log ->
                LogItemCard(
                    log = log,
                    onClick = {
                        val type = when (log) {
                            is ActivityLog -> "activity"
                            is FoodLog -> "food"
                        }
                        if (isTabletLandscape) {

                            onOpenLogDetailInDetail(log.id, type)
                        } else {

                            navController.navigate(Dest.LogDetail(log.id, type))
                        }
                    },
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

// Add this new composable inside LogsScreen.kt

@Composable
private fun LogIconBubble(log: LogItem, isDarkTheme: Boolean) {

    // Define the gradient based on the log type for a richer look
    val iconBgGradient = when (log) {
        is ActivityLog -> Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                MaterialTheme.colorScheme.primary
            )
        )
        is FoodLog -> Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                MaterialTheme.colorScheme.secondary
            )
        )
    }

    // --- FIX: Set icon tint based on the current theme (light/dark) ---
    val iconTint = if (isDarkTheme) {
        Color.White // Use white icon for dark theme
    } else {
        Color(0xFF020617) // Use a near-black icon for light theme
    }

    Box(
        modifier = Modifier
            .size(48.dp) // Same size as your original icon
            .clip(CircleShape)
            .background(iconBgGradient),
        contentAlignment = Alignment.Center
    ) {
        val text = when (log) {
            is ActivityLog -> "🏃‍♂"
            is FoodLog -> "🍽️"
        }

        Text(
            text = text,
            color = iconTint, // Use the same theme-aware color
            style = MaterialTheme.typography.titleMedium, // A good size for the bubble
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
private fun LogItemCard(
    log: LogItem,
    onClick: () -> Unit,
    isDarkTheme: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSmartFitDarkTheme()

    // ✅ 浅色下改成稍微深一点的灰，跟中间大白卡有对比
    val surfaceColor =
        if (isDark) colorScheme.surface.copy(alpha = 0.45f)
        else colorScheme.surfaceVariant.copy(alpha = 0.90f)

    // ✅ 边框也用 outlineVariant，比纯白更稳一点
    val borderColor =
        if (isDark) Color.White.copy(alpha = 0.10f)
        else colorScheme.outlineVariant.copy(alpha = 0.6f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = surfaceColor,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .padding(16.dp)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LogIconBubble(log = log, isDarkTheme = isDarkTheme)

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                val title = when (log) {
                    is FoodLog -> log.name ?: "Food"
                    is ActivityLog -> log.title ?: log.type
                }
                Text(title, style = MaterialTheme.typography.titleMedium)

                val duration = (log as? ActivityLog)?.durationMin?.let { "$it min" }
                val calories = "${log.calories?.toInt() ?: 0} kcal"

                Text(
                    text = if (duration != null) "$duration • $calories" else calories,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    log.displayDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ListAlt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Text("No activities yet", style = MaterialTheme.typography.titleMedium)
        Text("Add a new log to see it here.")
    }
}

@Composable
fun TabletFilterChips(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(option) },
                label = {
                    Text(
                        option.replaceFirstChar { it.uppercase() },
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface, // unselected
                    selectedContainerColor = MaterialTheme.colorScheme.primary, // selected same as calendar icon
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary, // text on selected chip
                    labelColor = MaterialTheme.colorScheme.onSurface // text on unselected chip
                ),
                border = FilterChipDefaults.filterChipBorder(
                    selected = isSelected,
                    enabled = true,
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), // unselected border
                    selectedBorderColor = MaterialTheme.colorScheme.primary // selected border matches icon
                )
            )
        }
    }
}



