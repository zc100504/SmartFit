package com.example.smartfit

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartfit.data.model.ActivityLog
import com.example.smartfit.data.model.FoodLog
import com.example.smartfit.ui.logs.*
import com.example.smartfit.ui.theme.SmartFitTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogsUITest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    /** ⭐ TEST 1 — 空状态应该显示 "No activities yet" */
    @Test
    fun logsScreen_showsEmptyState() {
        composeRule.setContent {
            SmartFitTheme {
                LogsList(
                    items = emptyList(),
                    isLoading = false,
                    navController = FakeNavController(),
                    isDarkTheme = false,
                    isTabletLandscape = false,
                    onOpenLogDetailInDetail = { _, _ -> }
                )
            }
        }

        composeRule.onNodeWithText("No activities yet").assertIsDisplayed()
        composeRule.onNodeWithText("Add a new log to see it here.").assertIsDisplayed()
    }

    /** ⭐ TEST 2 — 显示 Activity Log */
    @Test
    fun logsScreen_showsActivityLog() {

        val activity = ActivityLog(
            id = 1L,
            title = "Morning Run",
            type = "Running",
            durationMin = 30,
            distance = 3.5,
            calories = 250.0,
            notes = "",
            timestamp = System.currentTimeMillis(),
            userId = 1L
        )

        composeRule.setContent {
            SmartFitTheme {
                LogsList(
                    items = listOf(activity),
                    isLoading = false,
                    navController = FakeNavController(),
                    isDarkTheme = false,
                    isTabletLandscape = false,
                    onOpenLogDetailInDetail = { _, _ -> }
                )
            }
        }

        composeRule.onNodeWithText("Morning Run").assertIsDisplayed()
        composeRule.onNodeWithText("30 min • 250 kcal").assertIsDisplayed()
    }

    /** ⭐ TEST 3 — 显示 Food Log */
    @Test
    fun logsScreen_showsFoodLog() {

        val food = FoodLog(
            id = 10L,
            name = "Chicken Rice",
            mealType = "Lunch",
            calories = 500.0,
            notes = "Tasty",
            timestamp = System.currentTimeMillis(),
            userId = 1L
        )

        composeRule.setContent {
            SmartFitTheme {
                LogsList(
                    items = listOf(food),
                    isLoading = false,
                    navController = FakeNavController(),
                    isDarkTheme = false,
                    isTabletLandscape = false,
                    onOpenLogDetailInDetail = { _, _ -> }
                )
            }
        }

        composeRule.onNodeWithText("Chicken Rice").assertIsDisplayed()
        composeRule.onNodeWithText("500 kcal").assertIsDisplayed()
    }

    /** ⭐ TEST 4 — Phone segmented filter UI 是否显示 */
    @Test
    fun phoneSegmentedControl_showsAllButtons() {

        composeRule.setContent {
            SmartFitTheme {
                PhoneSegmentedControl(
                    options = listOf("all", "exercise", "food"),
                    selected = "all",
                    onSelect = {}
                )
            }
        }

        composeRule.onNodeWithText("All").assertIsDisplayed()
        composeRule.onNodeWithText("Exercise").assertIsDisplayed()
        composeRule.onNodeWithText("Food").assertIsDisplayed()
    }

}

/** Fake NavController — 不会做任何事情，只是让 UI 测试可以运行 */
class FakeNavController : NavHostController(
    ApplicationProvider.getApplicationContext()
)
