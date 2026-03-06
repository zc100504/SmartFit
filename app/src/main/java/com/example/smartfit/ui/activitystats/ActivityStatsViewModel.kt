package com.example.smartfit.ui.activitystats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.model.ActivityLog
import com.example.smartfit.data.model.FoodLog
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.data.repository.FoodRepository
import com.example.smartfit.data.repository.PrefsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.Calendar


@OptIn(ExperimentalCoroutinesApi::class)
class ActivityStatsViewModel(
    private val activityRepo: ActivityRepository,
    private val foodRepo: FoodRepository,
    private val prefsRepo: PrefsRepository
) : ViewModel() {

    private val _period = MutableStateFlow(StatsPeriod.DAY)

    private val _uiState = MutableStateFlow(
        ActivityStatsUiState(
            period = StatsPeriod.DAY,
            dateLabel = "Today",
            totalDurationMinutes = 0,
            totalDistanceKm = 0f,
            caloriesIntake = 0,
            caloriesBurned = 0,
            distancePoints = emptyList(),
            caloriesBurnedPoints = emptyList(),
            stepsPoints = emptyList(),
            currentSteps = 0,
            goalSteps = 10_000
        )
    )
    val uiState: StateFlow<ActivityStatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefsRepo.getUserId()
                .filter { it > 0L }
                .distinctUntilChanged()
                .flatMapLatest { userId ->
                    combine(
                        activityRepo.getAllByUser(userId),
                        foodRepo.getAllByUser(userId),
                        _period
                    ) { activities: List<ActivityLog>,
                        foods: List<FoodLog>,
                        period: StatsPeriod ->
                        computeStats(activities, foods, period)
                    }
                }
                .collect { stats ->
                    _uiState.value = stats
                }
        }
    }

    fun onPeriodChange(period: StatsPeriod) {
        _period.value = period
    }

    private fun computeStats(
        activities: List<ActivityLog>,
        foods: List<FoodLog>,
        period: StatsPeriod
    ): ActivityStatsUiState {
        val now = System.currentTimeMillis()
        return when (period) {
            StatsPeriod.DAY -> computeDailyStats(now, activities, foods)
            StatsPeriod.WEEK -> computeWeeklyStats(now, activities, foods)
        }
    }

    private fun computeDailyStats(
        now: Long,
        activities: List<ActivityLog>,
        foods: List<FoodLog>
    ): ActivityStatsUiState {
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis
        val endOfDay = startOfDay + TimeUnit.DAYS.toMillis(1)

        val actsToday = activities.filter { it.timestamp in startOfDay until endOfDay }
        val foodsToday = foods.filter { it.timestamp in startOfDay until endOfDay }


        val totalDuration = actsToday.sumOf { it.durationMin ?: 0 }
        val totalBurned = actsToday.sumOf { (it.calories ?: 0.0).toInt() }
        val totalIntake = foodsToday.sumOf { (it.calories ?: 0.0).toInt() }


        val totalDistanceKm = actsToday.sumOf { it.distance ?: 0.0 }.toFloat()



        val distancePoints = actsToday.map { (it.durationMin ?: 0).toFloat() }

        val caloriesPoints = actsToday.map { (it.calories ?: 0.0).toFloat() }
        val stepsPoints = normalizeSeries(
            actsToday.map { (it.durationMin ?: 0).toFloat() }
        )

        val steps = totalDuration * 100

        return ActivityStatsUiState(
            period = StatsPeriod.DAY,
            dateLabel = "Today",
            totalDurationMinutes = totalDuration,
            totalDistanceKm = totalDistanceKm,
            caloriesIntake = totalIntake,
            caloriesBurned = totalBurned,
            distancePoints = distancePoints,
            caloriesBurnedPoints = caloriesPoints,
            stepsPoints = stepsPoints,
            currentSteps = steps,
            goalSteps = 10_000
        )
    }


    private fun computeWeeklyStats(
        now: Long,
        activities: List<ActivityLog>,
        foods: List<FoodLog>
    ): ActivityStatsUiState {

        val weekCal = Calendar.getInstance().apply {
            timeInMillis = now
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfWeek = weekCal.timeInMillis
        val endOfWeek = startOfWeek + TimeUnit.DAYS.toMillis(7)

        val actsWeek = activities.filter { it.timestamp in startOfWeek until endOfWeek }
        val foodsWeek = foods.filter { it.timestamp in startOfWeek until endOfWeek }

        val totalDuration = actsWeek.sumOf { it.durationMin ?: 0 }
        val totalBurned = actsWeek.sumOf { (it.calories ?: 0.0).toInt() }
        val totalIntake = foodsWeek.sumOf { (it.calories ?: 0.0).toInt() }


        val totalDistanceKm = actsWeek.sumOf { it.distance ?: 0.0 }.toFloat()


        val distancePerDay = FloatArray(7) { 0f }    // index 0 = Monday, ... 6 = Sunday
        val caloriesPerDay = FloatArray(7) { 0f }

        val dayCal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
        }

        actsWeek.forEach { log ->
            dayCal.timeInMillis = log.timestamp
            val dayOfWeek = dayCal.get(Calendar.DAY_OF_WEEK)
            val index = when (dayOfWeek) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }
            distancePerDay[index] += (log.durationMin ?: 0).toFloat()
            caloriesPerDay[index] += (log.calories ?: 0.0).toFloat()
        }

        val distancePoints = distancePerDay.toList()
        val caloriesPoints = caloriesPerDay.toList()
        val stepsPoints = normalizeSeries(distancePerDay.toList())

        val steps = totalDuration * 100

        return ActivityStatsUiState(
            period = StatsPeriod.WEEK,
            dateLabel = "This Week (Mon–Sun)",
            totalDurationMinutes = totalDuration,
            totalDistanceKm = totalDistanceKm,
            caloriesIntake = totalIntake,
            caloriesBurned = totalBurned,
            distancePoints = distancePoints,
            caloriesBurnedPoints = caloriesPoints,
            stepsPoints = stepsPoints,
            currentSteps = steps,
            goalSteps = 10_000
        )
    }



    private fun normalizeSeries(raw: List<Float>): List<Float> {
        if (raw.isEmpty()) return emptyList()
        val max = raw.maxOrNull() ?: 0f
        if (max <= 0f) return raw.map { 0f }
        return raw.map { it / max }
    }
}
