package com.example.smartfit.ui.logs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smartfit.R
import com.example.smartfit.ui.AppViewModelProvider
import com.example.smartfit.ui.theme.isSmartFitDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLogScreen(
    navController: NavHostController,
    maxContentWidth: Dp = 760.dp
) {
    val vm: AddLogViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val ui by vm.state.collectAsStateWithLifecycle()
    val isFormValid by vm.isFormValid.collectAsStateWithLifecycle()
    val isReady by vm.isReady.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.effect.collect { eff ->
            when (eff) {
                is AddLogEffect.SaveCompleted -> navController.popBackStack()
            }
        }
    }

    val config = androidx.compose.ui.platform.LocalConfiguration.current
    val isTablet = config.screenWidthDp >= 600
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSmartFitDarkTheme()

    val typeOptions = stringArrayResource(R.array.activity_types).toList()
    var typeMenuExpanded by remember { mutableStateOf(false) }

    if (ui.showUpdateConfirmation) {
        AlertDialog(
            onDismissRequest = { vm.onDismissUpdateDialog() },
            title = { Text("Update Activity") },
            text = { Text("Are you sure you want to save these changes?") },
            confirmButton = {
                FilledTonalButton(onClick = { vm.confirmUpdate() }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { vm.onDismissUpdateDialog() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (ui.isEditMode) "Edit Activity" else "Add Activity",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(
                        horizontal = if (isTablet) 48.dp else 20.dp,
                        vertical = 14.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.widthIn(
                        max = if (isTablet) maxContentWidth else Dp.Infinity
                    )
                ) {
                    val enabled = isFormValid && isReady && !ui.isSaving
                    Button(
                        onClick = { vm.save() },
                        enabled = enabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        )
                    ) {
                        if (ui.isSaving) {
                            CircularProgressIndicator(
                                color = colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Saving…", fontWeight = FontWeight.SemiBold)
                        } else {
                            Text(
                                text = if (ui.isEditMode) "Update Activity" else "Save Activity",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            colorScheme.background,
                            colorScheme.background.copy(alpha = 0.98f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (isTablet) 32.dp else 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 顶部 hero 卡
                GlassActivityHeroCard(
                    ui = ui,
                    isTablet = isTablet,
                    maxContentWidth = maxContentWidth
                )

                Spacer(Modifier.height(18.dp))

                // 中间表单卡
                GlassActivityFormCard(
                    ui = ui,
                    isTablet = isTablet,
                    maxContentWidth = maxContentWidth,
                    isReady = isReady,
                    typeOptions = typeOptions,
                    typeMenuExpanded = typeMenuExpanded,
                    onTypeMenuExpandedChange = { typeMenuExpanded = it },
                    vm = vm
                )
            }
        }
    }
}

/* ---------- 顶部 hero 卡 ---------- */

@Composable
private fun GlassActivityHeroCard(
    ui: AddLogUiState,
    isTablet: Boolean,
    maxContentWidth: Dp
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSmartFitDarkTheme()

    val surfaceColor =
        if (isDark) colorScheme.surface.copy(alpha = 0.45f)
        else Color.White.copy(alpha = 0.96f)

    val borderColor =
        if (isDark) Color.White.copy(alpha = 0.10f)
        else Color.White.copy(alpha = 0.55f)

    Surface(
        modifier = Modifier
            .then(
                if (isTablet) Modifier.widthIn(max = maxContentWidth)
                else Modifier.fillMaxWidth()
            )
            .padding(horizontal = if (isTablet) 0.dp else 18.dp),
        shape = RoundedCornerShape(26.dp),
        color = surfaceColor,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            if (isTablet) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ActivityHeroIcon(modifier = Modifier.size(86.dp))

                    Spacer(Modifier.width(18.dp))

                    Column {
                        Text(
                            ui.title.ifBlank { "New Activity" },
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Track duration, distance & calories",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ActivityHeroIcon(modifier = Modifier.size(110.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        ui.title.ifBlank { "Add Activity" },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Track duration, distance & calories",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityHeroIcon(
    modifier: Modifier = Modifier
) {
    val bgGradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.secondary
        )
    )
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = modifier
                .size(110.dp)          // 同样，外层 tablet 会传 86.dp
                .clip(CircleShape)
                .background(bgGradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🏃‍♂️",
                fontSize = 34.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/* ---------- 中间表单卡 ---------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlassActivityFormCard(
    ui: AddLogUiState,
    isTablet: Boolean,
    maxContentWidth: Dp,
    isReady: Boolean,
    typeOptions: List<String>,
    typeMenuExpanded: Boolean,
    onTypeMenuExpandedChange: (Boolean) -> Unit,
    vm: AddLogViewModel
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSmartFitDarkTheme()

    val surfaceColor =
        if (isDark) colorScheme.surface.copy(alpha = 0.40f)
        else Color.White.copy(alpha = 0.96f)

    val borderColor =
        if (isDark) Color.White.copy(alpha = 0.10f)
        else Color.White.copy(alpha = 0.55f)

    Surface(
        modifier = Modifier
            .then(
                if (isTablet) Modifier.widthIn(max = maxContentWidth)
                else Modifier.fillMaxWidth()
            )
            .padding(horizontal = if (isTablet) 0.dp else 18.dp),
        shape = RoundedCornerShape(26.dp),
        color = surfaceColor,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Activity",
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))

            FieldRow(label = "Activity Name") {
                InputField(
                    value = ui.title,
                    onValueChange = vm::onTitleChange,
                    placeholder = "e.g. Morning Run",
                    singleLine = true,
                    enabled = isReady
                )
            }

            Spacer(Modifier.height(12.dp))

            FieldRow(label = "Type") {
                ExposedDropdownMenuBox(
                    expanded = typeMenuExpanded,
                    onExpandedChange = {
                        if (isReady) onTypeMenuExpandedChange(!typeMenuExpanded)
                    }
                ) {
                    OutlinedTextField(
                        value = ui.type,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(typeMenuExpanded)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = typeMenuExpanded,
                        onDismissRequest = { onTypeMenuExpandedChange(false) }
                    ) {
                        typeOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    vm.onTypeChange(opt)
                                    onTypeMenuExpandedChange(false)
                                }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = isReady) {
                                if (!typeMenuExpanded) onTypeMenuExpandedChange(true)
                            }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            FieldRow(label = "Duration (min)") {
                InputField(
                    value = ui.durationMin,
                    onValueChange = vm::onDurationChange,
                    placeholder = "e.g. 30",
                    singleLine = true,
                    keyboardType = KeyboardType.Number,
                    enabled = isReady
                )
            }

            Spacer(Modifier.height(12.dp))

            FieldRow(label = "Distance (km)") {
                InputField(
                    value = ui.distanceKm,
                    onValueChange = vm::onDistanceChange,
                    placeholder = "e.g. 3.5",
                    singleLine = true,
                    keyboardType = KeyboardType.Number,
                    enabled = isReady
                )
            }

            Spacer(Modifier.height(12.dp))

            FieldRow(label = "Calories (kcal)") {
                InputField(
                    value = ui.calories,
                    onValueChange = vm::onCaloriesChange,
                    placeholder = "e.g. 250",
                    singleLine = true,
                    keyboardType = KeyboardType.Number,
                    enabled = isReady
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "Notes",
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            InputField(
                value = ui.notes,
                onValueChange = vm::onNotesChange,
                placeholder = "Optional notes about the activity",
                singleLine = false,
                minLines = 3,
                enabled = isReady
            )

            Spacer(Modifier.height(16.dp))

            if (ui.isLoading) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Loading…", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(12.dp))
            }

            ui.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

/* ---------- 共用 FieldRow / InputField（和上面 Food 的一致） ---------- */

@Composable
private fun FieldRow(
    label: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
            ),
            elevation = CardDefaults.cardElevation(0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(Modifier.padding(12.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    singleLine: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { if (placeholder.isNotBlank()) Text(placeholder) },
        singleLine = singleLine,
        maxLines = if (singleLine) 1 else 6,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        ),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.onSurface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        minLines = minLines,
        enabled = enabled
    )
}
