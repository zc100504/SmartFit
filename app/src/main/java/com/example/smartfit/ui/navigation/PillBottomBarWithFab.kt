package com.example.smartfit.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.smartfit.ui.icon.SmartFitIcons
import com.example.smartfit.ui.theme.DarkBackground

@Composable
fun PillBottomBarWithFab(
    navController: NavHostController,
    onFabClick: () -> Unit
) {
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination
    val colorScheme = MaterialTheme.colorScheme
    val accent = colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 背后轻微的模糊高光，让 bar 看起来像漂浮在上面
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 30.dp, vertical = 18.dp)
                .blur(26.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    )
                )
        )

        // 主体 pill（玻璃感）
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(28.dp),
                    clip = false
                ),
            shape = RoundedCornerShape(28.dp),
            color = colorScheme.surface.copy(alpha = 0.88f),
            tonalElevation = 8.dp,
            border = BorderStroke(
                1.dp,
                colorScheme.outline.copy(alpha = 0.15f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左边两个：Home + Activity
                BarItem(
                    label = "Home",
                    selected = current.isSelected<Dest.Home>(),
                    onClick = { navController.navigateSingleTopTo(Dest.Home) },
                    icon = { Icon(SmartFitIcons.Home, contentDescription = "Home" ) },
                    accent = accent,
                    modifier = Modifier.weight(1f)
                )

                BarItem(
                    label = "Activity",
                    selected = current.isSelected<Dest.Logs>(),
                    onClick = { navController.navigateSingleTopTo(Dest.Logs) },
                    icon = { Icon(SmartFitIcons.Activity, contentDescription = "Activity") },
                    accent = accent,
                    modifier = Modifier.weight(1f)
                )

                // 中间空出 FAB 区
                Spacer(Modifier.width(56.dp))

                // 右边两个：Tips + Profile
                BarItem(
                    label = "Tips",
                    selected = current.isSelected<Dest.Tips>(),
                    onClick = { navController.navigateSingleTopTo(Dest.Tips) },
                    icon = { Icon(SmartFitIcons.Tips, contentDescription = "Tips") },
                    accent = accent,
                    modifier = Modifier.weight(1f)
                )

                BarItem(
                    label = "Profile",
                    selected = current.isSelected<Dest.Profile>(),
                    onClick = { navController.navigateSingleTopTo(Dest.Profile) },
                    icon = { Icon(SmartFitIcons.Profile, contentDescription = "Profile") },
                    accent = accent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 中间 FAB 背后的柔光圈
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 6.dp)
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.55f),
                            Color.Transparent
                        )
                    )
                )
                .blur(18.dp)
        )

        // 外圈玻璃圆环
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 6.dp)
                .size(58.dp)
                .clip(CircleShape)
                .border(
                    BorderStroke(2.dp, colorScheme.outline.copy(alpha = 0.15f)),
                    CircleShape
                )
                .background(colorScheme.surface.copy(alpha = 0.96f)),
            contentAlignment = Alignment.Center
        ) {
            // 真正的 FAB（渐变 + 大号 +）
            FloatingActionButton(
                onClick = onFabClick,
                shape = CircleShape,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    focusedElevation = 0.dp,
                    hoveredElevation = 0.dp
                ),
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                accent,
                                colorScheme.secondary
                            )
                        ),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = SmartFitIcons.Add,
                    contentDescription = "Add log",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun BarItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.background == DarkBackground

    val circleBg: Color
    val circleBorder: BorderStroke?
    val iconTint: Color
    val textColor: Color

    if (selected) {
        if (isDark) {
            circleBg    = accent.copy(alpha = 0.22f)
            circleBorder = null
            iconTint    = accent
            textColor   = accent
        } else {
            circleBg    = accent.copy(alpha = 0.22f)          // ← 这里让它“深一点”
            circleBorder = BorderStroke(1.dp, accent)
            iconTint    = Color(0xFF5A9D09)
            textColor   = Color(0xFF5A9D09)
        }
    } else {
        circleBg    = Color.Transparent
        circleBorder = null
        iconTint    = colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        textColor   = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 10.dp)
    ) {
        Surface(
            modifier = Modifier.size(28.dp),
            shape = CircleShape,
            color = circleBg,
            border = circleBorder
        ) {
            Box(contentAlignment = Alignment.Center) {
                CompositionLocalProvider(LocalContentColor provides iconTint) {
                    icon()
                }
            }
        }

        Spacer(Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            color = textColor,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
    }
}




/* -------- type-safe helpers（跟你之前一样） -------- */

private inline fun <reified T : Dest> NavDestination?.isSelected(): Boolean =
    this?.hierarchy?.any { it.hasRoute<T>() } == true

private fun NavHostController.navigateSingleTopTo(dest: Dest) {
    navigate(dest) {
        launchSingleTop = true
        popUpTo(graph.findStartDestination().id) { saveState = true }
        restoreState = true
    }
}
