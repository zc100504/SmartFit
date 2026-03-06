// app/src/main/java/com/example/smartfit/ui/navigation/AppNavHost.kt
package com.example.smartfit.ui.navigation

import android.graphics.Color as AColor
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartfit.di.AppGraph
import com.example.smartfit.ui.AppViewModelProvider
import com.example.smartfit.ui.activitystats.ActivityStatsScreen
import com.example.smartfit.ui.activitystats.ActivityStatsViewModel
import com.example.smartfit.ui.icon.SmartFitIcons
import com.example.smartfit.ui.logs.LogDetailScreen
import com.example.smartfit.ui.logs.LogDetailViewModel
import com.example.smartfit.ui.profile.ChangePasswordScreen
import com.example.smartfit.ui.profile.EditProfileScreen
import com.example.smartfit.ui.profile.FaqScreen
import com.example.smartfit.ui.profile.PrivacyPolicyScreen
import com.example.smartfit.ui.theme.ThemeViewModel
import com.example.smartfit.ui.tips.NewTipQuestionScreen
import com.example.smartfit.ui.tips.TipConversationScreen
import com.example.smartfit.ui.tips.TipsViewModel

// Tablet detail pane screens: now a sealed class so TipConversation can carry threadId

private sealed class TabletDetailScreen {
    object ActivitySummary : TabletDetailScreen()
    object NewTipQuestion : TabletDetailScreen()
    data class TipConversation(val threadId: String) : TabletDetailScreen()
    data class LogDetail(val id: Long, val type: String) : TabletDetailScreen()

    // ✅ 新增
    object EditProfile : TabletDetailScreen()
    object ChangePassword : TabletDetailScreen()
    object PrivacyPolicy : TabletDetailScreen()
    object Faq : TabletDetailScreen()
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    graph: AppGraph,
    windowSizeClass: WindowSizeClass
) {
    val navController = rememberNavController()

    // --- Session + theme via repositories (no DataStore directly in UI)
    val prefs = graph.prefsRepo
    val isLoggedIn by prefs.isLoggedIn().collectAsStateWithLifecycle(initialValue = null)

    // Theme via small ViewModel wrapper
    val themeVm = remember { ThemeViewModel(prefs) }
    val mode by themeVm.themeMode.collectAsStateWithLifecycle()
    val darkTheme = when (mode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    // --- Window size & orientation ---
    val widthClass = windowSizeClass.widthSizeClass
    val isTablet = widthClass >= WindowWidthSizeClass.Medium

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val isTabletLandscape = isTablet && isLandscape

    // --- Route-aware system bars ---
    SystemBarsForRoute(navController = navController, darkTheme = darkTheme)

    // Wait for preferences to load
    if (isLoggedIn == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(strokeWidth = 4.dp)
        }
        return
    }

    // Decide first screen based on login status
    val startDest: Dest = when {
        isLoggedIn == false -> Dest.Login
        else -> Dest.Home
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination

    // --- Bottom bar (phones only, only for main tabs) ---
    val showBottomBarRoutes = destination?.hierarchy?.any {
        it.hasRoute<Dest.Home>() ||
                it.hasRoute<Dest.Logs>() ||
                it.hasRoute<Dest.Tips>() ||
                it.hasRoute<Dest.Profile>()
    } == true

    val showBottomBar = showBottomBarRoutes && !isTablet

    // --- Nav rail (tablets, also on ActivityStats for consistent layout) ---
    val showNavRailRoutes = destination?.hierarchy?.any {
        it.hasRoute<Dest.Home>() ||
                it.hasRoute<Dest.Logs>() ||
                it.hasRoute<Dest.Tips>() ||
                it.hasRoute<Dest.Profile>() ||
                it.hasRoute<Dest.ActivityStats>()
    } == true

    val showNavRail = isTablet && (!isTabletLandscape || showNavRailRoutes)

    // --- Bottom sheet state (add log) ---
    var showAddSheet by remember { mutableStateOf(false) }

    // --- Tablet detail pane state ---
    var tabletDetailScreen by remember { mutableStateOf<TabletDetailScreen?>(null) }

    // --- Detect if current route is an "auth" route ---
    // For Login / SignUp we want a single full-screen panel (no side whitespace)
    val isAuthRoute = destination?.hierarchy?.any {
        it.hasRoute<Dest.Login>() ||
                it.hasRoute<Dest.SignUp>()
    } == true

    val isAddLogRoute = destination?.hierarchy?.any {
        it.hasRoute<Dest.AddFoodLog>() || it.hasRoute<Dest.AddActivityLog>()
    } == true


    Scaffold(
        bottomBar = {
            // Auth screens never show bottom bar anyway.
            if (showBottomBar && !isAuthRoute) {
                PillBottomBarWithFab(
                    navController = navController,
                    onFabClick = { showAddSheet = true }
                )
            }
        }
    ) { padding ->

        if (isAuthRoute || isAddLogRoute) {
            // ==========================
            // AUTH LAYOUT (single panel)
            // ==========================
            NavGraphContent(
                navController = navController,
                graph = graph,
                innerPadding = padding,
                startDestination = startDest,
                windowSizeClass = windowSizeClass,
                isDark = darkTheme,
                isTabletLandscape = false,
                onShowActivitySummaryInDetail = { /* not used on auth screens */ },
                onShowNewTipQuestionInDetail = { /* not used on auth screens */ },
                onShowTipConversationInDetail = { /* not used on auth screens */ _ -> },
                onShowLogDetailInDetail = { _, _ -> },
                onShowEditProfileInDetail = { },
                onShowChangePasswordInDetail = { },
                onShowPrivacyPolicyInDetail = { },
                onShowFaqInDetail = { },
            )

        } else {
            // =====================================
            // MAIN APP LAYOUT (adaptive two panes)
            // =====================================
            SmartFitAdaptiveRoot(
                windowSizeClass = windowSizeClass,
                isTabletLandscape = isTabletLandscape,
                hasDetailPane = isTabletLandscape && tabletDetailScreen != null,
                navigationBar = {
                    if (showNavRail) {
                        VerticalNavRail(
                            navController = navController,
                            onAddClick = { showAddSheet = true },
                            onTopLevelDestinationSelected = {
                                tabletDetailScreen = null
                            },
                            isDark = darkTheme
                        )
                    }
                },
                mainPane = {
                    NavGraphContent(
                        navController = navController,
                        graph = graph,
                        innerPadding = padding,
                        startDestination = startDest,
                        windowSizeClass = windowSizeClass,
                        isDark = darkTheme,
                        isTabletLandscape = isTabletLandscape,
                        onShowActivitySummaryInDetail = {
                            tabletDetailScreen = TabletDetailScreen.ActivitySummary
                        },
                        onShowNewTipQuestionInDetail = {
                            tabletDetailScreen = TabletDetailScreen.NewTipQuestion
                        },
                        onShowTipConversationInDetail = { threadId ->
                            tabletDetailScreen = TabletDetailScreen.TipConversation(threadId)
                        },
                        onShowLogDetailInDetail = { id, type ->
                            tabletDetailScreen = TabletDetailScreen.LogDetail(id, type)
                        },
                        onShowEditProfileInDetail = {
                            tabletDetailScreen = TabletDetailScreen.EditProfile
                        },
                        onShowChangePasswordInDetail = {
                            tabletDetailScreen = TabletDetailScreen.ChangePassword
                        },
                        // ✅ 新增：点隐私/FAQ 时打开右侧 panel
                        onShowPrivacyPolicyInDetail = {
                            tabletDetailScreen = TabletDetailScreen.PrivacyPolicy
                        },
                        onShowFaqInDetail = {
                            tabletDetailScreen = TabletDetailScreen.Faq
                        },
                    )
                },
                detailPane = {
                    when (val detail = tabletDetailScreen) {
                        TabletDetailScreen.ActivitySummary -> {
                            val statsVm: ActivityStatsViewModel =
                                viewModel(factory = AppViewModelProvider.Factory)
                            val statsUi by statsVm.uiState.collectAsState()

                            ActivityStatsScreen(
                                uiState = statsUi,
                                onBackClick = { tabletDetailScreen = null },
                                onPeriodChange = statsVm::onPeriodChange
                            )
                        }


                        TabletDetailScreen.NewTipQuestion -> {
                            // Use a TipsViewModel here if you want to actually save to DB
                            val tipsVm: TipsViewModel =
                                viewModel(
                                    factory = AppViewModelProvider.Factory
                                )
                            var hasStartedSubmit by remember { mutableStateOf(false) }
                            val menuState by tipsVm.menuUiState.collectAsState()

                            NewTipQuestionScreen(
                                onBackClick = { navController.popBackStack() },
                                onSubmitQuestion = { question ->
                                    hasStartedSubmit = true
                                    tipsVm.submitNewQuestion(question)
                                },
                                isSubmitting = menuState.isLoading && hasStartedSubmit
                            )
                        }

                        is TabletDetailScreen.TipConversation -> {
                            val tipsVm: TipsViewModel =
                                viewModel(
                                    factory = AppViewModelProvider.Factory
                                )

                            TipConversationScreen(
                                threadId = detail.threadId,
                                onBackClick = { tabletDetailScreen = null },
                                viewModel = tipsVm
                            )
                        }

                        is TabletDetailScreen.LogDetail -> {
                            val logDetailVm: LogDetailViewModel =
                                viewModel(factory = AppViewModelProvider.Factory)

                            // Load the correct log when the detail pane is opened / changed
                            LaunchedEffect(detail.id, detail.type) {
                                logDetailVm.loadLog(detail.id, detail.type)
                            }

                            LogDetailScreen(
                                navController = navController,
                                isDark = darkTheme,
                                viewModel = logDetailVm,
                                onBackClick ={ tabletDetailScreen = null }
                            )
                        }

                        is TabletDetailScreen.EditProfile -> {
                            EditProfileScreen(
                                navController = navController,
                                onBackClick = { tabletDetailScreen = null }   // ✅ closes detail pane
                            )
                        }

                        is TabletDetailScreen.ChangePassword -> {
                            ChangePasswordScreen(
                                navController = navController,
                                windowSizeClass = windowSizeClass,
                                onBackClick = { tabletDetailScreen = null }   // ✅ closes detail pane
                            )
                        }
                        is TabletDetailScreen.PrivacyPolicy -> {
                            PrivacyPolicyScreen(
                                onNavigateUp = { tabletDetailScreen = null }
                            )
                        }

                        is TabletDetailScreen.Faq -> {
                            FaqScreen(
                                onNavigateUp = { tabletDetailScreen = null }
                            )
                        }

                        null -> Unit
                    }
                }
            )


            // Add-log bottom sheet should only be shown inside the main app,
            // not on login / sign-up screens.
            if (showAddSheet) {
                AddLogBottomSheet(
                    onDismiss = { showAddSheet = false },
                    onAddExercise = {
                        showAddSheet = false
                        navController.navigate(Dest.AddActivityLog())
                    },
                    onAddFood = {
                        showAddSheet = false
                        navController.navigate(Dest.AddFoodLog())
                    }
                )
            }
        }
    }
}

/**
 * Central, route-aware system bar controller.
 * - Forces dark icons (light bar) on Login/SignUp (image backgrounds).
 * - Else follows app theme (dark or light bars).
 */
@Composable
private fun SystemBarsForRoute(
    navController: NavHostController,
    darkTheme: Boolean
) {
    val activity = LocalActivity.current as? ComponentActivity ?: return
    val backStack by navController.currentBackStackEntryAsState()
    val dest = backStack?.destination

    LaunchedEffect(dest, darkTheme) {
        val darkScrim = AColor.argb(0x66, 0, 0, 0)
        val forceLightIcons =
            dest?.hasRoute<Dest.Login>() == true || dest?.hasRoute<Dest.SignUp>() == true

        if (forceLightIcons || darkTheme) {
            activity.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(AColor.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.dark(AColor.TRANSPARENT)
            )
        } else {
            activity.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(AColor.TRANSPARENT, darkScrim),
                navigationBarStyle = SystemBarStyle.light(AColor.TRANSPARENT, darkScrim)
            )
        }
    }
}

/**
 * Navigation rail used on tablets.
 */
@Composable
private fun VerticalNavRail(
    navController: NavHostController,
    onAddClick: () -> Unit,
    onTopLevelDestinationSelected: () -> Unit,
    isDark: Boolean
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination

    val colorScheme = MaterialTheme.colorScheme

    fun isSelectedHome() =
        destination?.hierarchy?.any { it.hasRoute<Dest.Home>() } == true

    fun isSelectedLogs() =
        destination?.hierarchy?.any { it.hasRoute<Dest.Logs>() } == true

    fun isSelectedTips() =
        destination?.hierarchy?.any { it.hasRoute<Dest.Tips>() } == true

    fun isSelectedProfile() =
        destination?.hierarchy?.any { it.hasRoute<Dest.Profile>() } == true

    // Two color setups: one for dark theme, one for light theme
    val railItemColors = if (isDark) {
        // Dark theme: selected icon/text black on bright primary indicator
        NavigationRailItemDefaults.colors(
            selectedIconColor = Color.Black,
            selectedTextColor = Color.Black,
            indicatorColor = colorScheme.primary,
            unselectedIconColor = colorScheme.onSurfaceVariant,
            unselectedTextColor = colorScheme.onSurfaceVariant
        )
    } else {
        // Light theme: selected uses deep green for a "healthy" look
        val deepGreen = Color(0xFF5A9D09)
        NavigationRailItemDefaults.colors(
            selectedIconColor = deepGreen,
            selectedTextColor = deepGreen,
            indicatorColor = colorScheme.primary,
            unselectedIconColor = colorScheme.onSurfaceVariant,
            unselectedTextColor = colorScheme.onSurfaceVariant
        )
    }

    NavigationRail(
        modifier = Modifier
            .fillMaxHeight()
            .width(88.dp),
        containerColor = colorScheme.background,
        contentColor = colorScheme.onBackground
    ) {
        NavigationRailItem(
            selected = isSelectedHome(),
            onClick = {
                onTopLevelDestinationSelected()
                navController.navigate(Dest.Home) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(SmartFitIcons.Home, contentDescription = "Home") },
            colors = railItemColors
        )

        NavigationRailItem(
            selected = isSelectedLogs(),
            onClick = {
                onTopLevelDestinationSelected()
                navController.navigate(Dest.Logs) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(SmartFitIcons.Activity, contentDescription = "Activity") },
            colors = railItemColors
        )

        NavigationRailItem(
            selected = isSelectedTips(),
            onClick = {
                onTopLevelDestinationSelected()
                navController.navigate(Dest.Tips) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(SmartFitIcons.Tips, contentDescription = "Tips") },
            colors = railItemColors
        )

        NavigationRailItem(
            selected = isSelectedProfile(),
            onClick = {
                onTopLevelDestinationSelected()
                navController.navigate(Dest.Profile) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(SmartFitIcons.Profile, contentDescription = "Profile") },
            colors = railItemColors
        )

        Spacer(modifier = Modifier.weight(1f))

        val addIconColor = if (isDark) {
            Color.Black
        } else {
            Color(0xFF5A9D09)
        }
        // FAB for adding logs.
        FloatingActionButton(
            onClick = onAddClick,
            containerColor = colorScheme.primary,
            contentColor = addIconColor
        ) {
            Icon(
                imageVector = SmartFitIcons.Add,
                contentDescription = "Add log"
            )
        }
    }
}
