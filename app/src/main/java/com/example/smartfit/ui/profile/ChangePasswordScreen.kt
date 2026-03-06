package com.example.smartfit.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartfit.ui.AppViewModelProvider
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.example.smartfit.ui.theme.isSmartFitDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    onBackClick: (() -> Unit)? = null,       // ✅ new optional back callback
    vm: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isNewPasswordValid = newPassword.length >= 6
    val passwordsMatch = newPassword == confirmPassword
    val canAttemptChange = currentPassword.isNotBlank() && isNewPasswordValid && passwordsMatch

    val isTablet = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Password") },          // ✅ correct title
                navigationIcon = {                            // ✅ back button
                    IconButton(
                        onClick = {
                            onBackClick?.invoke() ?: navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    titleContentColor = colorScheme.onBackground,
                    navigationIconContentColor = colorScheme.onBackground,
                    actionIconContentColor = colorScheme.onBackground
                )
            )
        },
        containerColor = colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        if (isTablet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .heightIn(min = 400.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PasswordForm(
                            currentPassword = currentPassword,
                            onCurrentChange = { currentPassword = it },
                            newPassword = newPassword,
                            onNewChange = { newPassword = it },
                            confirmPassword = confirmPassword,
                            onConfirmChange = { confirmPassword = it },
                            showCurrent = showCurrent,
                            onToggleCurrent = { showCurrent = !showCurrent },
                            showNew = showNew,
                            onToggleNew = { showNew = !showNew },
                            showConfirm = showConfirm,
                            onToggleConfirm = { showConfirm = !showConfirm },
                            canSave = canAttemptChange,
                            errorMessage = errorMessage,
                            onSave = {
                                vm.changePassword(
                                    currentPassword,
                                    newPassword,
                                    onSuccess = {
                                        onBackClick?.invoke()
                                            ?: navController.popBackStack()
                                    },
                                    onError = { error -> errorMessage = error }
                                )
                            }
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PasswordForm(
                    currentPassword = currentPassword,
                    onCurrentChange = { currentPassword = it },
                    newPassword = newPassword,
                    onNewChange = { newPassword = it },
                    confirmPassword = confirmPassword,
                    onConfirmChange = { confirmPassword = it },
                    showCurrent = showCurrent,
                    onToggleCurrent = { showCurrent = !showCurrent },
                    showNew = showNew,
                    onToggleNew = { showNew = !showNew },
                    showConfirm = showConfirm,
                    onToggleConfirm = { showConfirm = !showConfirm },
                    canSave = canAttemptChange,
                    errorMessage = errorMessage,
                    onSave = {
                        vm.changePassword(
                            currentPassword,
                            newPassword,
                            onSuccess = {
                                onBackClick?.invoke()
                                    ?: navController.popBackStack()
                            },
                            onError = { error -> errorMessage = error }
                        )
                    }
                )
            }
        }
    }
}


@Composable
fun PasswordForm(
    currentPassword: String,
    onCurrentChange: (String) -> Unit,
    newPassword: String,
    onNewChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmChange: (String) -> Unit,
    showCurrent: Boolean,
    onToggleCurrent: () -> Unit,
    showNew: Boolean,
    onToggleNew: () -> Unit,
    showConfirm: Boolean,
    onToggleConfirm: () -> Unit,
    canSave: Boolean,
    errorMessage: String?,
    onSave: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Your password must be at least 6 characters long.",
            style = MaterialTheme.typography.bodyMedium
        )

        PasswordField(
            label = "Current Password",
            value = currentPassword,
            onValueChange = onCurrentChange,
            showPassword = showCurrent,
            onToggleShow = onToggleCurrent,
        )

        PasswordField(
            label = "New Password",
            value = newPassword,
            onValueChange = onNewChange,
            showPassword = showNew,
            onToggleShow = onToggleNew,
            isError = newPassword.isNotEmpty() && newPassword.length < 6
        )

        PasswordField(
            label = "Confirm New Password",
            value = confirmPassword,
            onValueChange = onConfirmChange,
            showPassword = showConfirm,
            onToggleShow = onToggleConfirm,
            isError = confirmPassword.isNotEmpty() && confirmPassword != newPassword
        )

        if (errorMessage != null) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = onSave,
            enabled = canSave,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Save Password")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    showPassword: Boolean,
    onToggleShow: () -> Unit,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleShow) {
                Icon(
                    imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (showPassword) "Hide password" else "Show password"
                )
            }
        },
        isError = isError,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        colors = OutlinedTextFieldDefaults.colors(
            // Color of the border and label when the field is focused
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.onSurface, // Black in light, white in dark

            // A more visible color for the unfocused label
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
