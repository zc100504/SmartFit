package com.example.smartfit.ui.profile

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.smartfit.ui.AppViewModelProvider
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import com.example.smartfit.ui.theme.isSmartFitDarkTheme

// ----------------- 头像列表保留不变 -----------------
val defaultAvatars = listOf(
    "https://i.pravatar.cc/150?img=12",
    "https://i.pravatar.cc/150?img=5",
    "https://i.pravatar.cc/150?img=7",
    "https://i.pravatar.cc/150?img=25",
    "https://i.pravatar.cc/150?img=32"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    onBackClick: (() -> Unit)? = null,          // ✅ new optional back callback
    vm: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val userState by vm.user.collectAsState()
    val currentUser = userState

    var showConfirmationDialog by remember { mutableStateOf(false) }

    var name by remember(currentUser?.username) { mutableStateOf(currentUser?.username ?: "") }
    var email by remember(currentUser?.email) { mutableStateOf(currentUser?.email ?: "") }
    var selectedAvatarUrl by remember(currentUser) {
        mutableStateOf(currentUser?.avatarUrl ?: defaultAvatars.first())
    }

    val hasChanges = (name.isNotBlank() && name != currentUser?.username) ||
            (email.isNotBlank() && email != currentUser?.email) ||
            (selectedAvatarUrl != currentUser?.avatarUrl)

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Information") },
                navigationIcon = {                     // ✅ back button
                    IconButton(
                        onClick = {
                            onBackClick?.invoke() ?: navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
        if (currentUser == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (isTablet) {
                // ---- Tablet: centered card ----
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxHeight(0.9f)
                            .widthIn(max = 370.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.surface.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        EditProfileForm(
                            name = name,
                            onNameChange = { name = it },
                            email = email,
                            onEmailChange = { email = it },
                            selectedAvatarUrl = selectedAvatarUrl,
                            onAvatarChange = { selectedAvatarUrl = it },
                            hasChanges = hasChanges,
                            onSaveClick = { showConfirmationDialog = true },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp, vertical = 20.dp)
                        )
                    }
                }
            } else {
                // ---- Phone ----
                EditProfileForm(
                    name = name,
                    onNameChange = { name = it },
                    email = email,
                    onEmailChange = { email = it },
                    selectedAvatarUrl = selectedAvatarUrl,
                    onAvatarChange = { selectedAvatarUrl = it },
                    hasChanges = hasChanges,
                    onSaveClick = { showConfirmationDialog = true },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }

            if (showConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmationDialog = false },
                    title = { Text("Confirm Changes") },
                    text = {
                        Text("Are you sure you want to save these changes to your profile?")
                    },
                    confirmButton = {
                        FilledTonalButton(
                            onClick = {
                                vm.updateUserProfile(name, email, selectedAvatarUrl)
                                showConfirmationDialog = false
                                onBackClick?.invoke() ?: navController.popBackStack()
                            }
                        ) { Text("Save") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmationDialog = false }) {
                            Text("Cancel", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }
        }
    }
}


/**
 * 共享的表单内容：头像 + 切换 + 文本框 + 按钮
 * phone / tablet 都复用，只是容器不一样
 */
@Composable
private fun EditProfileForm(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    selectedAvatarUrl: String,
    onAvatarChange: (String) -> Unit,
    hasChanges: Boolean,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val elevation by animateDpAsState(if (hasChanges) 8.dp else 2.dp)

    Column(
        modifier = modifier.verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 头像
        Crossfade(targetState = selectedAvatarUrl) { avatar ->
            EditableAvatar(avatarUrl = avatar)
        }

        AvatarSwitcher(
            currentAvatar = selectedAvatarUrl,
            onAvatarChange = onAvatarChange
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = hasChanges,
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = elevation)
        ) {
            Text("Save Changes")
        }
    }
}

// 下面这两个保持你原来的实现

@Composable
fun EditableAvatar(
    avatarUrl: String,
    modifier: Modifier = Modifier
) {
    val painter = rememberAsyncImagePainter(avatarUrl)
    Box(
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
    ) {
        Image(
            painter = painter,
            contentDescription = "User Avatar",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun AvatarSwitcher(
    currentAvatar: String,
    onAvatarChange: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val currentIndex = defaultAvatars.indexOf(currentAvatar).coerceAtLeast(0)

        IconButton(onClick = {
            val prevIndex = (currentIndex - 1 + defaultAvatars.size) % defaultAvatars.size
            onAvatarChange(defaultAvatars[prevIndex])
        }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous Avatar") }

        Text(
            text = "Avatar ${currentIndex + 1} / ${defaultAvatars.size}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        IconButton(onClick = {
            val nextIndex = (currentIndex + 1) % defaultAvatars.size
            onAvatarChange(defaultAvatars[nextIndex])
        }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next Avatar") }
    }
}
