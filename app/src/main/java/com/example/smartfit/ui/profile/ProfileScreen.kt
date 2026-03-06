// app/src/main/java/com/example/smartfit/ui/profile/ProfileScreen.kt
package com.example.smartfit.ui.profile

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.smartfit.BuildConfig
import com.example.smartfit.ui.AppViewModelProvider
import com.example.smartfit.ui.navigation.Dest
import com.example.smartfit.ui.theme.isSmartFitDarkTheme
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    onEditProfileClick: (() -> Unit)? = null,
    onChangePasswordClick: (() -> Unit)? = null,
    isTabletLandscape: Boolean,
    onPrivacyPolicyClick: (() -> Unit)? = null,
    onFaqClick: (() -> Unit)? = null,
) {
    val vm: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)

    val user by vm.user.collectAsState()
    val username by vm.username.collectAsState()
    val email by vm.email.collectAsState()
    val themeMode by vm.themeMode.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val selectedTheme = remember(themeMode) { mutableStateOf(themeMode) }
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)      // ✅ 跟全 app 一样的背景
    ) {

        if (isTablet) {
            // ================= TABLET LAYOUT =================
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 800.dp)       // 中间一块内容区域
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    item {
                        ProfileHeaderCard(
                            name = username,
                            avatarUrl = user?.avatarUrl ?: "https://i.pravatar.cc/150?img=12",
                            onEdit = {
                                if (onEditProfileClick != null && isTabletLandscape) {
                                    onEditProfileClick()
                                } else {
                                    navController.navigate(Dest.EditProfile)
                                }
                            }
                        )
                    }
                    item {
                        AccountCard(
                            email = email,
                            onManagePassword = {
                                if (onChangePasswordClick != null && isTabletLandscape) {
                                    onChangePasswordClick()
                                } else {
                                    navController.navigate(Dest.ChangePassword)
                                }
                            }
                        )
                    }
                    item {
                        AppearanceSettingsCard(
                            selectedTheme = selectedTheme.value,
                            onThemeChange = {
                                selectedTheme.value = it
                                vm.setTheme(it)
                            }
                        )
                    }
                    item {
                        SupportAboutCard(
                            navController = navController,
                            isTabletLandscape = isTabletLandscape,
                            onPrivacyPolicyClick = onPrivacyPolicyClick,
                            onFaqClick = onFaqClick
                        )
                    }
                    item {
                        LogoutButton(
                            onLogout = { showLogoutDialog = true }
                        )
                    }
                }
            }
        } else {
            // ================= PHONE LAYOUT =================
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    ProfileHeaderCard(
                        name = username,
                        avatarUrl = user?.avatarUrl ?: "https://i.pravatar.cc/150?img=12",
                        onEdit = {
                            // ✅ 手机点头像 → Edit Profile
                            if (onEditProfileClick != null && isTabletLandscape) {
                                onEditProfileClick()
                            } else {
                                navController.navigate(Dest.EditProfile)
                            }
                        }
                    )
                }

                item {
                    AccountCard(
                        email = email,
                        onManagePassword = {
                            if (onChangePasswordClick != null && isTabletLandscape) {
                                onChangePasswordClick()
                            } else {
                                navController.navigate(Dest.ChangePassword)
                            }
                        }
                    )
                }
                item {
                    AppearanceSettingsCard(
                        selectedTheme = selectedTheme.value,
                        onThemeChange = {
                            selectedTheme.value = it
                            vm.setTheme(it)
                        }
                    )
                }
                item {
                    SupportAboutCard(
                        navController = navController,
                        isTabletLandscape = isTabletLandscape
                    )

                }
                item {
                    LogoutButton(
                        onLogout = { showLogoutDialog = true }
                    )
                }
            }
        }
    }

    // Logout dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                Button(
                    onClick = {
                        vm.logout()
                        navController.navigate(Dest.Login) {
                            popUpTo(Dest.Home) { inclusive = true }
                            launchSingleTop = true
                        }
                        showLogoutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/* ---------- 左边菜单样式（目前如果有用的话） ---------- */

@Composable
fun ProfileMenuItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val backgroundColor =
        if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val textColor =
        if (selected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 12.dp)
            .hoverable(interactionSource = interactionSource)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = label,
                color = textColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

/* ---------- 头部卡片 ---------- */

@Composable
fun ProfileHeaderCard(name: String, avatarUrl: String, onEdit: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSmartFitDarkTheme()

    val surfaceColor =
        if (isDark) colorScheme.surface.copy(alpha = 0.45f)
        else Color.White.copy(alpha = 0.96f)

    val borderColor =
        if (isDark) Color.White.copy(alpha = 0.10f)
        else Color.White.copy(alpha = 0.55f)

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar + edit button
            Box(modifier = Modifier.size(90.dp)) {
                Image(
                    painter = rememberAsyncImagePainter(avatarUrl),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(CircleShape),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = colorScheme.primary,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.width(20.dp))

            Column {
                Text(
                    text = if (name.isBlank()) "User" else name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Tap edit to update your info",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/* ---------- 账号 & 外观 ---------- */

@Composable
fun AccountCard(email: String, onManagePassword: () -> Unit) {
    SettingsCard(title = "Account") {
        SettingsRow(
            label = "Email",
            value = email,
            icon = Icons.Default.Email
        )
        Spacer(modifier = Modifier.height(8.dp))
        SettingsClickableRow(
            label = "Manage Password",
            icon = Icons.Default.Lock,
            onClick = onManagePassword
        )
    }
}

@Composable
fun AppearanceSettingsCard(
    selectedTheme: String,
    onThemeChange: (String) -> Unit
) {
    val options = ThemeOption.values().toList()
    val isTablet = LocalConfiguration.current.screenWidthDp >= 600

    SettingsCard(title = "Appearance") {
        var containerWidth by remember { mutableStateOf(0.dp) }
        val density = LocalDensity.current

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = if (isTablet) 10.dp else 6.dp,
                    horizontal = if (isTablet) 2.dp else 0.dp
                )
                .height(if (isTablet) 52.dp else 40.dp)
                .clip(RoundedCornerShape(if (isTablet) 14.dp else 10.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    if (containerWidth == 0.dp) {
                        containerWidth = with(density) { placeable.width.toDp() }
                    }
                    layout(placeable.width, placeable.height) {
                        placeable.placeRelative(0, 0)
                    }
                }
        ) {
            if (containerWidth > 0.dp) {
                val segmentWidth = containerWidth / options.size
                val selectedIndex = options.indexOfFirst { it.value == selectedTheme }
                    .takeIf { it >= 0 } ?: 0

                val indicatorOffset by animateDpAsState(
                    targetValue = segmentWidth * selectedIndex,
                    label = "theme-indicator-offset",
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )

                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(if (isTablet) 14.dp else 10.dp),
                    modifier = Modifier
                        .width(segmentWidth)
                        .fillMaxHeight()
                        .offset(x = indicatorOffset)
                ) {}

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    options.forEach { option ->
                        val isSelected = selectedTheme == option.value
                        val contentColor =
                            if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onThemeChange(option.value) }
                                ),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                option.icon,
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(if (isTablet) 22.dp else 18.dp)
                            )
                            Spacer(Modifier.width(if (isTablet) 10.dp else 6.dp))
                            Text(
                                option.label,
                                color = contentColor,
                                style = if (isTablet) MaterialTheme.typography.titleSmall
                                else MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class ThemeOption(val value: String, val label: String, val icon: ImageVector) {
    LIGHT("LIGHT", "Light", Icons.Default.WbSunny),
    DARK("DARK", "Dark", Icons.Default.NightsStay),
    SYSTEM("SYSTEM", "System", Icons.Default.Settings)
}

@Composable
fun SupportAboutCard(
    navController: NavController,
    isTabletLandscape: Boolean,
    onPrivacyPolicyClick: (() -> Unit)? = null,
    onFaqClick: (() -> Unit)? = null
){
    SettingsCard(title = "Support & About") {
        SettingsClickableRow(
            label = "Privacy Policy",
            icon = Icons.Default.PrivacyTip,
            onClick = {
                if (isTabletLandscape && onPrivacyPolicyClick != null) {
                    onPrivacyPolicyClick()
                } else {
                    navController.navigate(Dest.PrivacyPolicy)
                }
            }
        )
        Spacer(Modifier.height(8.dp))
        SettingsClickableRow(
            label = "FAQ",
            icon = Icons.Default.Help,
            onClick = {
                if (isTabletLandscape && onFaqClick != null) {
                    onFaqClick()
                } else {
                    navController.navigate(Dest.Faq)
                }
            }
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "App Version ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LogoutButton(onLogout: () -> Unit) {
    Button(
        onClick = onLogout,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Icon(Icons.Default.ExitToApp, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Logout")
    }
}

/* ------------------ 通用 Settings 卡片 ------------------ */

@Composable
fun SettingsCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSmartFitDarkTheme()

    val surfaceColor =
        if (isDark) colorScheme.surface.copy(alpha = 0.45f)
        else Color.White.copy(alpha = 0.96f)

    val borderColor =
        if (isDark) Color.White.copy(alpha = 0.10f)
        else Color.White.copy(alpha = 0.55f)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SettingsRow(label: String, value: String, icon: ImageVector? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsClickableRow(label: String, icon: ImageVector? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
        }
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
}
