package com.example.smartfit.ui.logs

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.copy
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartfit.ui.AppViewModelProvider
import com.example.smartfit.data.model.ActivityLog
import com.example.smartfit.data.model.FoodLog
import com.example.smartfit.data.model.LogItem
import com.example.smartfit.ui.navigation.Dest
import com.example.smartfit.ui.theme.isSmartFitDarkTheme
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogDetailScreen(
    navController: NavHostController,
    isDark: Boolean,
    onBackClick: () -> Unit,
    viewModel: LogDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val log by viewModel.log.collectAsState()
    val deleteCompleted by viewModel.deleteCompleted.collectAsState()
    val showDeleteDialog by viewModel.showDeleteConfirmDialog.collectAsState()

    LaunchedEffect(deleteCompleted) {
        if (deleteCompleted) {
            navController.popBackStack()
            viewModel.onDeleteCompleted()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDeleteDialog() },
            title = { Text("Delete log", fontWeight = FontWeight.SemiBold) },
            text = { Text("Are you sure you want to delete this log? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDeleteLog() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                FilledTonalButton(onClick = { viewModel.onDismissDeleteDialog() }) { Text("Cancel") }
            }
        )
    }

    val currentLog = log ?: run {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSmartFitDarkTheme()

    Scaffold(
        topBar = {
            if (isTablet) {
                CenterAlignedTopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            // 或 IconButton(onClick = { onBackClick() })
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            val route = when (currentLog) {
                                is FoodLog -> Dest.AddFoodLog(currentLog.id)
                                is ActivityLog -> Dest.AddActivityLog(currentLog.id)
                            }
                            navController.navigate(route)
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = colorScheme.background,
                        titleContentColor = colorScheme.onBackground,
                        navigationIconContentColor = colorScheme.onBackground,
                        actionIconContentColor = colorScheme.onBackground
                    ),
                    modifier = Modifier.statusBarsPadding()
                )
            }
        },
        containerColor = colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (isTablet) {
                TabletBottomActions(
                    timestamp = currentLog.timestamp,
                    onDelete = { viewModel.onShowDeleteDialog() }
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (!isTablet) {
                PhoneHeroSection(
                    currentLog = currentLog,
                    isDark = isDark,
                    onBack = { navController.popBackStack() },
                    onEdit = {
                        val route = when (currentLog) {
                            is FoodLog -> Dest.AddFoodLog(currentLog.id)
                            is ActivityLog -> Dest.AddActivityLog(currentLog.id)
                        }
                        navController.navigate(route)
                    }
                )
            } else {
                TabletHeroSection(currentLog = currentLog, isDark = isDark)
            }

            Spacer(Modifier.height(18.dp))

            Column(Modifier.padding(horizontal = 20.dp)) {
                MetricsGrid(
                    log = currentLog,
                    columns = if (isTablet) 2 else 1
                )

                Spacer(Modifier.height(16.dp))

                NotesCard(
                    notes = when (currentLog) {
                        is FoodLog -> currentLog.notes ?: "-"
                        is ActivityLog -> currentLog.notes ?: "-"
                        else -> "-"
                    }
                )

                Spacer(Modifier.height(16.dp))

                // DELETE BUTTON UNDER NOTES (only on phone)
                if (!isTablet) {
                    OutlinedButton(
                        onClick = { viewModel.onShowDeleteDialog() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Delete Log")
                    }
                }

                Spacer(Modifier.height(100.dp))
            }
        }
    }
}


@Composable
private fun DetailIconBubble(log: LogItem, isDark: Boolean, modifier: Modifier = Modifier) {

    // Define the gradient based on the log type
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

    // --- FIX: Set icon tint based on the theme ---
    val iconTint = if (isDark) {
        Color.White // White icon in dark mode
    } else {
        Color(0xFF020617) // A near-black color for the icon in light mode
    }


    Box(
        modifier = modifier
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
            color = iconTint,
            // Replace the default style with a specific font size
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


/* ---------------------- PHONE HERO SECTION ---------------------- */

@Composable
private fun PhoneHeroSection(
    currentLog: LogItem,
    isDark: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp) // enough height to accommodate moved elements
    ) {
        // Background covers entire hero section

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f) // Darkens the bottom
                        ),
                        startY = 400f // Start gradient lower down
                    )
                )
        )

        // Back and Edit buttons at the top with status bar padding
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Icon, Title, and Date moved lower, centered horizontally
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp), // lower to below buttons
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DetailIconBubble(
                log = currentLog,
                isDark = isDark,
                modifier = Modifier
                    .size(120.dp)

            )


            Spacer(Modifier.height(20.dp))

            val title = when (currentLog) {
                is FoodLog -> currentLog.name ?: "Food"
                is ActivityLog -> currentLog.title ?: currentLog.type // Use title, fallback to type
            }

            Text(
                text = title, // Was currentLog.type
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            val formatted = SimpleDateFormat(
                "EEE, dd MMM yyyy • hh:mm a",
                Locale.getDefault()
            ).format(currentLog.timestamp)

            Text(
                formatted,
                style = MaterialTheme.typography.bodyMedium.copy(
                    letterSpacing = 0.2.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}


/* ---------------------- TABLET HERO SECTION ---------------------- */

@Composable
private fun TabletHeroSection(currentLog: LogItem, isDark: Boolean) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {

        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 20.dp)
        ) {
            DetailIconBubble(
                log = currentLog,
                isDark = isDark,
                modifier = Modifier
                    .size(110.dp)

            )

            Spacer(Modifier.height(12.dp))

            // --- FIX 2: Use the specific 'title' for ActivityLog ---
            val title = when (currentLog) {
                is FoodLog -> currentLog.name ?: "Food"
                is ActivityLog -> currentLog.title ?: currentLog.type // Use title, fallback to type
            }

            Text(
                title, // This now correctly shows "Evening Run"
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
            )

            val formatted = SimpleDateFormat("yyyy MMM dd • hh:mm a", Locale.getDefault())
                .format(currentLog.timestamp)

            Text(
                formatted,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


/* ---------------------- METRICS + NOTES ---------------------- */

@Composable
private fun MetricsGrid(log: LogItem, columns: Int) {
    val metrics = remember(log) {
        val m = mutableListOf<Pair<String, String>>()
        when (log) {
            is FoodLog -> {
                m += "Meal Type" to (log.mealType ?: "-")
                m += "Calories" to "${log.calories?.toInt() ?: "-"} kcal"
            }
            is ActivityLog -> {
                m += "Type" to log.type
                m += "Duration" to "${log.durationMin} min"

                // --- FIX: Use the correct property name 'distance' instead of 'distanceKm' ---
                log.distance?.let { dist ->
                    // Format the distance to one decimal place. Assuming the value is in km.
                    val formattedDistance = String.format("%.1f km", dist)
                    m += "Distance" to formattedDistance
                }

                m += "Calories" to "${log.calories?.toInt() ?: "-"} kcal"
            }
        }
        m
    }

    // The rest of this composable is already correct and does not need changes.
    if (columns == 1) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            metrics.forEach { (label, value) ->
                MetricCard(label, value)
            }
        }
    } else {
        val pairs = metrics.chunked(2)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            pairs.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { (label, value) ->
                        MetricCard(label, value, Modifier.weight(1f))
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}


@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 0.3.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}


@Composable
private fun NotesCard(notes: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.08f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                "Notes",
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 0.3.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))

            Text(
                notes.ifBlank { "-" },
                style = MaterialTheme.typography.bodyMedium.copy(
                    letterSpacing = 0.1.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}



/* ---------------------- TABLET BOTTOM BAR ---------------------- */

@Composable
private fun TabletBottomActions(
    timestamp: Long,
    onDelete: () -> Unit
) {
    // Fully transparent bar – no background, no elevation
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val date = SimpleDateFormat("yyyy MMM dd", Locale.getDefault())
                .format(timestamp)

            Text(
                "Added on $date",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedButton(
                onClick = onDelete,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Delete Log")
            }
        }
    }
}

