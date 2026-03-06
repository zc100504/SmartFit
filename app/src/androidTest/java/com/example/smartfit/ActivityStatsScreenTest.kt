package com.example.smartfit

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartfit.ui.activitystats.*
import com.example.smartfit.ui.theme.SmartFitTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityStatsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun fakeUi() = ActivityStatsUiState(
        period = StatsPeriod.DAY,
        dateLabel = "Today",
        totalDurationMinutes = 50,
        totalDistanceKm = 3.4f,
        caloriesIntake = 1500,
        caloriesBurned = 350,
        distancePoints = listOf(10f, 20f, 30f),
        caloriesBurnedPoints = listOf(120f, 180f, 160f),
        stepsPoints = emptyList(),
        currentSteps = 0,
        goalSteps = 0
    )

    /** Test 1 — Activity Summary 标题显示 */
    @Test
    fun activityStats_shows_title() {
        composeRule.setContent {
            SmartFitTheme {
                ActivityStatsScreen(
                    uiState = fakeUi(),
                    onBackClick = {},
                    onPeriodChange = {}
                )
            }
        }

        composeRule.onNodeWithTag("stats_Date").assertIsDisplayed()
        composeRule.onNodeWithTag("stats_title").assertIsDisplayed()
    }

    /** Test 2 — Hero Stats 有 Duration / Intake / Burned */
    @Test
    fun activityStats_shows_hero_stats() {
        composeRule.setContent {
            SmartFitTheme {
                ActivityStatsScreen(
                    uiState = fakeUi(),
                    onBackClick = {},
                    onPeriodChange = {}
                )
            }
        }

        composeRule.onNodeWithTag("stats_duration").assertIsDisplayed()
        composeRule.onNodeWithTag("stats_intake_label").assertIsDisplayed()
        composeRule.onNodeWithTag("stats_burned_label").assertIsDisplayed()
    }
}
