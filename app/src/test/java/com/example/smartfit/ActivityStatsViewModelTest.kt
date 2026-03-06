package com.example.smartfit.ui.activitystats

import com.example.smartfit.MainDispatcherRule
import com.example.smartfit.data.model.ActivityLog
import com.example.smartfit.data.model.FoodLog
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.data.repository.FoodRepository
import com.example.smartfit.data.repository.PrefsRepository
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityStatsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val activityRepo: ActivityRepository = mockk()
    private val foodRepo: FoodRepository = mockk()
    private val prefsRepo: PrefsRepository = mockk()

    /**
     * Verify that for period = DAY:
     *  - totalDurationMinutes = sum of activity.durationMin
     *  - totalDistanceKm = sum of activity.distance
     *  - caloriesBurned = sum of activity.calories
     *  - caloriesIntake = sum of food.calories
     */
    @Test
    fun dailyStats_aggregatesActivitiesAndFoodsCorrectly() = runTest {
        val userId = 1L
        val now = System.currentTimeMillis()

        // User id from prefs
        every { prefsRepo.getUserId() } returns flowOf(userId)

        // Two activities today
        val activitiesToday = listOf(
            ActivityLog(
                userId = userId,
                timestamp = now,
                type = "Running",
                title = "Morning Run",
                notes = null,
                durationMin = 30,
                distance = 5.0,
                calories = 300.0
            ),
            ActivityLog(
                userId = userId,
                timestamp = now,
                type = "Cycling",
                title = "Evening Ride",
                notes = null,
                durationMin = 60,
                distance = 20.0,
                calories = 500.0
            )
        )

        // Two food logs today
        val foodsToday = listOf(
            FoodLog(
                name = "Breakfast",
                calories = 400.0,
                mealType = "Breakfast",
                timestamp = now,
                notes = "Oats",
                userId = userId
            ),
            FoodLog(
                name = "Dinner",
                calories = 700.0,
                mealType = "Dinner",
                timestamp = now,
                notes = "Rice + Chicken",
                userId = userId
            )
        )

        every { activityRepo.getAllByUser(userId) } returns flowOf(activitiesToday)
        every { foodRepo.getAllByUser(userId) } returns flowOf(foodsToday)

        val vm = ActivityStatsViewModel(
            activityRepo = activityRepo,
            foodRepo = foodRepo,
            prefsRepo = prefsRepo
        )

        // Wait until stats reflect our data (totalDurationMinutes > 0)
        val state = vm.uiState.first { it.totalDurationMinutes > 0 }

        // Expected sums:
        // duration: 30 + 60 = 90
        // distance: 5.0 + 20.0 = 25.0
        // burned: 300 + 500 = 800
        // intake: 400 + 700 = 1100
        assertEquals(90, state.totalDurationMinutes)
        assertEquals(25.0f, state.totalDistanceKm)
        assertEquals(800, state.caloriesBurned)
        assertEquals(1100, state.caloriesIntake)
    }
}
