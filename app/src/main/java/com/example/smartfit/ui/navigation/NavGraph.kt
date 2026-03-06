// app/src/main/java/com/example/smartfit/ui/navigation/NavGraphContent.kt
package com.example.smartfit.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.smartfit.di.AppGraph
import com.example.smartfit.ui.activitystats.*
import com.example.smartfit.ui.auth.LoginScreen
import com.example.smartfit.ui.auth.SignUpScreen
import com.example.smartfit.ui.home.HomeScreen
import com.example.smartfit.ui.logs.AddFoodScreen
import com.example.smartfit.ui.logs.AddLogScreen
import com.example.smartfit.ui.logs.LogDetailScreen
import com.example.smartfit.ui.logs.LogsScreen
import com.example.smartfit.ui.profile.ProfileScreen
import com.example.smartfit.ui.profile.EditProfileScreen
import com.example.smartfit.ui.profile.ChangePasswordScreen
import com.example.smartfit.ui.tips.NewTipQuestionScreen
import com.example.smartfit.ui.tips.TipConversationScreen
import com.example.smartfit.ui.tips.TipsMenuScreen
import com.example.smartfit.ui.AppViewModelProvider
import com.example.smartfit.ui.tips.TipsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartfit.ui.activitystats.ActivityStatsViewModel
import com.example.smartfit.ui.profile.FaqScreen
import com.example.smartfit.ui.profile.PrivacyPolicyScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavGraphContent(
    navController: NavHostController,
    graph: AppGraph,
    innerPadding: PaddingValues,
    startDestination: Dest,
    windowSizeClass: WindowSizeClass,
    isDark: Boolean,
    isTabletLandscape: Boolean,
    onShowActivitySummaryInDetail: () -> Unit,
    onShowNewTipQuestionInDetail: () -> Unit,
    onShowTipConversationInDetail: (String) -> Unit,
    onShowLogDetailInDetail: (Long, String) -> Unit,
    onShowEditProfileInDetail: () -> Unit,
    onShowChangePasswordInDetail: () -> Unit,
    onShowPrivacyPolicyInDetail: () -> Unit,
    onShowFaqInDetail: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ----- Auth -----

        composable<Dest.Login> {
            LoginScreen(navController, isDark = isDark)
        }
        composable<Dest.SignUp> {
            SignUpScreen(navController, isDark = isDark)
        }

        // ----- Top-level tabs -----
        composable<Dest.Home> {
            HomeScreen(
                navController = navController,
                contentPadding = innerPadding,
                onSummaryClick = {
                    if (isTabletLandscape) {
                        onShowActivitySummaryInDetail()
                    } else {
                        navController.navigate(Dest.ActivityStats)
                    }
                },
                isTabletLandscape = isTabletLandscape,
                onOpenLogDetailInDetail = onShowLogDetailInDetail
            )
        }


        composable<Dest.Logs> {
            LogsScreen(
                navController = navController,
                windowSizeClass = windowSizeClass,
                contentPadding = innerPadding,
                isDarkTheme = isDark,
                isTabletLandscape = isTabletLandscape,              // ✅
                onOpenLogDetailInDetail = onShowLogDetailInDetail   // ✅
            )
        }


        composable<Dest.PrivacyPolicy> {
            // FIX: Pass a lambda to handle the back press action
            PrivacyPolicyScreen(onNavigateUp = { navController.popBackStack() })
        }

        composable<Dest.Faq> {
            // Also apply the same fix for FaqScreen for consistency
            FaqScreen(onNavigateUp = { navController.popBackStack() })
        }


        // -------------------------------------------------
        //                     TIPS
        // -------------------------------------------------

        // Tips menu
        composable<Dest.Tips> {
            TipsMenuScreen(
                onNewQuestionClick = {
                    if (isTabletLandscape) {
                        // Tablet landscape → open "New Question" in detail pane
                        onShowNewTipQuestionInDetail()
                    } else {
                        // Phone / portrait → normal navigation
                        navController.navigate(Dest.TipsNew)
                    }
                },
                onTipClick = { threadId ->
                    if (isTabletLandscape) {
                        // Tablet landscape → open conversation in detail pane
                        onShowTipConversationInDetail(threadId)
                    } else {
                        // Phone / portrait → normal navigation
                        navController.navigate(Dest.TipDetail(threadId = threadId))
                    }
                },
                contentPadding = innerPadding
            )
        }

        // New question
        // (Used when we actually navigate, e.g. phones or portrait)

        composable<Dest.TipsNew> {
            val tipsVm: TipsViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val menuUi by tipsVm.menuUiState.collectAsState()

            var hasStartedSubmit by remember { mutableStateOf(false) }


            LaunchedEffect(Unit) {
                tipsVm.openThreadEvents.collect { threadId ->
                    hasStartedSubmit = false

                    navController.navigate(Dest.TipDetail(threadId = threadId)) {

                        popUpTo(Dest.TipsNew) { inclusive = true }
                    }
                }
            }

            NewTipQuestionScreen(
                onBackClick = { navController.popBackStack() },
                onSubmitQuestion = { question ->
                    hasStartedSubmit = true
                    tipsVm.submitNewQuestion(question)
                },
                isSubmitting = menuUi.isLoading && hasStartedSubmit
            )
        }


        // Tip conversation
        // (Used when we navigate to a full screen, e.g. phones / portrait)
        composable<Dest.TipDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<Dest.TipDetail>()

            TipConversationScreen(
                threadId = args.threadId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ----- Profile -----
        composable<Dest.Profile> {
            if (isTabletLandscape) {
                ProfileScreen(
                    navController = navController,
                    onEditProfileClick = onShowEditProfileInDetail,
                    onChangePasswordClick = onShowChangePasswordInDetail,
                    onPrivacyPolicyClick = onShowPrivacyPolicyInDetail,
                    onFaqClick = onShowFaqInDetail,
                    isTabletLandscape = isTabletLandscape
                )
            } else {
                ProfileScreen(
                    navController = navController,
                    isTabletLandscape = isTabletLandscape
                )
            }
        }



        composable<Dest.EditProfile> {
            EditProfileScreen(navController = navController)
        }

        composable<Dest.ChangePassword> {
            ChangePasswordScreen(
                navController = navController,
                windowSizeClass = windowSizeClass
            )
        }

        // ----- Detail with type-safe argument
        composable<Dest.LogDetail> { backStackEntry ->
            // The args are automatically passed to the ViewModel's SavedStateHandle,
            // so we don't need to extract them here manually.
            LogDetailScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() },
                isDark = isDark
            )
        }

        // ----- Add / edit food log -----
        composable<Dest.AddFoodLog>(
            enterTransition = {
                slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) +
                        fadeIn(animationSpec = tween(200))
            },
            exitTransition = { fadeOut(tween(150)) },
            popEnterTransition = { fadeIn(tween(150)) },
            popExitTransition = {
                slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) +
                        fadeOut(animationSpec = tween(200))
            }
        ) {
            AddFoodScreen(navController = navController)
        }

        // ----- Add / edit activity log -----
        composable<Dest.AddActivityLog>(
            enterTransition = {
                slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) +
                        fadeIn(animationSpec = tween(200))
            },
            exitTransition = { fadeOut(tween(150)) },
            popEnterTransition = { fadeIn(tween(150)) },
            popExitTransition = {
                slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) +
                        fadeOut(animationSpec = tween(200))
            }
        ) {
            AddLogScreen(navController = navController)
        }

        // ----- Activity stats (demo data for now) -----
        composable<Dest.ActivityStats> {
            val statsVm: ActivityStatsViewModel =
                viewModel(factory = AppViewModelProvider.Factory)
            val statsUi by statsVm.uiState.collectAsState()

            ActivityStatsScreen(
                uiState = statsUi,
                onBackClick = { navController.popBackStack() },
                onPeriodChange = statsVm::onPeriodChange
            )
        }
    }
}
