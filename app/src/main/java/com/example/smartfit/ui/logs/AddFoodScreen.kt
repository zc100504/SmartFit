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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartfit.ui.AppViewModelProvider
import com.example.smartfit.ui.theme.isSmartFitDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    navController: NavController,
    maxContentWidth: Dp = 760.dp
) {
    val vm: AddFoodViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val ui by vm.uiState.collectAsStateWithLifecycle()
    val isFormValid by vm.isFormValid.collectAsStateWithLifecycle()
    val isReady by vm.isReady.collectAsStateWithLifecycle()

    // 完成后返回
    LaunchedEffect(ui.saveCompleted) {
        if (ui.saveCompleted) {
            navController.popBackStack()
            vm.onSaveCompleted()
        }
    }

    val config = androidx.compose.ui.platform.LocalConfiguration.current
    val isTablet = config.screenWidthDp >= 600
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSmartFitDarkTheme()

    if (ui.showUpdateConfirmation) {
        AlertDialog(
            onDismissRequest = { vm.onDismissUpdateDialog() },
            title = { Text("Update Food Log") },
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
                        text = if (ui.isEditMode) "Edit Food Log" else "Add Food Log",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    /*if (ui.isEditMode) {
                        IconButton(onClick = { /* no-op */ }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit mode")
                        }
                    }*/
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            // 底部大按钮（跟草图第三块）
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
                        onClick = { vm.saveFoodLog() },
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
                                if (ui.isEditMode) "Update Log" else "Save Food Log",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        // 背景跟 Home 一样的渐变
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

                // -------- 顶部大卡（第一块） --------
                GlassFoodHeroCard(
                    ui = ui,
                    isTablet = isTablet,
                    maxContentWidth = maxContentWidth
                )

                Spacer(Modifier.height(18.dp))

                // -------- 中间表单大卡（第二块） --------
                GlassFoodFormCard(
                    ui = ui,
                    isTablet = isTablet,
                    maxContentWidth = maxContentWidth,
                    isReady = isReady,
                    isFormValid = isFormValid,
                    vm = vm
                )
            }
        }
    }
}

/* ---------- 顶部 hero 卡片 ---------- */

@Composable
private fun GlassFoodHeroCard(
    ui: AddFoodUiState,
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeroFoodIcon(modifier = Modifier.size(86.dp))

                    Spacer(Modifier.width(18.dp))

                    Column {
                        Text(
                            ui.foodName.ifBlank { "New Food Log" },
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Track calories & meal type",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HeroFoodIcon(modifier = Modifier.size(110.dp))
                    Spacer(Modifier.height(10.dp))
                    Text(
                        ui.foodName.ifBlank { "Add Food" },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Track calories & meal type",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroFoodIcon(modifier: Modifier = Modifier) {
    val bgGradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.secondary
        )
    )
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 8.dp,
        modifier = modifier
    ) {
        Box(
            modifier = modifier
                .size(110.dp)          // 这里的 size 会被外面覆盖（tablet 用 86.dp），没关系
                .clip(CircleShape)
                .background(bgGradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🍽",
                fontSize = 34.sp,
                color = Color.White,   // 在彩色背景上保持白色，跟 log icon 一致
                fontWeight = FontWeight.Bold
            )
        }
    }


}

/* ---------- 中间表单卡片 ---------- */

@Composable
private fun GlassFoodFormCard(
    ui: AddFoodUiState,
    isTablet: Boolean,
    maxContentWidth: Dp,
    isReady: Boolean,
    isFormValid: Boolean,
    vm: AddFoodViewModel
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
                "Food",
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))

            FieldRow(label = "Name") {
                InputField(
                    value = ui.foodName,
                    onValueChange = vm::onFoodNameChange,
                    placeholder = "e.g. Grilled salmon",
                    singleLine = true,
                    keyboardType = KeyboardType.Text
                )
            }

            Spacer(Modifier.height(12.dp))

            FieldRow(label = "Calories (kcal)") {
                InputField(
                    value = ui.calories,
                    onValueChange = vm::onCaloriesChange,
                    placeholder = "e.g. 300",
                    singleLine = true,
                    keyboardType = KeyboardType.Number
                )
            }

            Spacer(Modifier.height(12.dp))

            FieldRow(label = "Meal type") {
                MealTypeDropdown(
                    value = ui.mealType,
                    expanded = ui.isMealTypeMenuExpanded,
                    onOpen = vm::onMealTypeMenuOpen,
                    onDismiss = vm::onMealTypeMenuDismiss,
                    onSelect = vm::onMealTypeChange,
                    options = vm.mealTypes
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                "Notes",
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            InputField(
                value = ui.description,
                onValueChange = vm::onDescriptionChange,
                placeholder = "Add optional notes",
                singleLine = false,
                minLines = 3
            )

            Spacer(Modifier.height(18.dp))

            when {
                !isReady -> {
                    Text(
                        "Preparing form…",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                !isFormValid -> {
                    Text(
                        "Please fill required fields",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

/* ---------- 共用小组件（和之前一样，只是颜色微调） ---------- */

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
    minLines: Int = 1
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
            cursorColor = MaterialTheme.colorScheme.onSurface
        ),
        minLines = minLines
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealTypeDropdown(
    value: String,
    expanded: Boolean,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    options: List<String>
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (!expanded) onOpen() }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            shape = RoundedCornerShape(12.dp),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                        onSelect(opt)
                        onDismiss()
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { if (!expanded) onOpen() }
        )
    }
}
