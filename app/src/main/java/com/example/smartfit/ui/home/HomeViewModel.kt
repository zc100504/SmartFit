package com.example.smartfit.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.model.ActivityLog
import com.example.smartfit.data.model.FoodLog
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.data.repository.FoodRepository
import com.example.smartfit.data.repository.PrefsRepository
import com.example.smartfit.data.repository.TipsRepository
import com.example.smartfit.data.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// Supported periods for the summary section
enum class ActivityPeriod { DAILY, WEEKLY }

// Filter for the “Recently Activity” list
enum class ActivityFilter { ALL, EXERCISE, FOOD }

// Logical type of an item in the recent list
enum class ActivityType { EXERCISE, FOOD }

// Logical icon type used by the UI layer
enum class ActivityIcon { RUNNING, CYCLING, FOOD, DRINK }

// One row in the “Recently Activity” list on the Home screen
data class ActivityItemUiState(
    val id: Long,
    val type: ActivityType,
    val title: String,
    val subtitle: String,
    val caloriesText: String,
    val icon: ActivityIcon
)

// Data shown in the summary card (no steps, only real data)
data class ActivitySummaryUiState(
    val activeMinutes: Int,
    val caloriesIntake: Int,
    val caloriesBurned: Int,
    val period: ActivityPeriod
)

// Full UI state for the Home screen
data class HomeUiState(
    val isLoading: Boolean = false,
    val userName: String = "SmartFit User",
    val avatarUrl: String = "",
    val summary: ActivitySummaryUiState = ActivitySummaryUiState(
        activeMinutes = 0,
        caloriesIntake = 0,
        caloriesBurned = 0,
        period = ActivityPeriod.DAILY
    ),
    val filter: ActivityFilter = ActivityFilter.ALL,
    val activities: List<ActivityItemUiState> = emptyList(),
    val tips: String = "Log your meals and activities daily to get more accurate insights."
)



/**
 * Home screen ViewModel.
 *
 * All data now comes from Room via repositories.
 */
class HomeViewModel(
    private val activityRepository: ActivityRepository,
    private val foodRepository: FoodRepository,
    private val prefsRepository: PrefsRepository,
    private val userRepository: UserRepository,
    private val tipsRepository: TipsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _period = MutableStateFlow(ActivityPeriod.DAILY)

    private val _filter = MutableStateFlow(ActivityFilter.ALL)


    init {
        observeData()
        observeUser()
        observeTips()
    }

    private fun observeTips() {
        viewModelScope.launch {
            prefsRepository.getUserId()
                .onEach { id ->
                    android.util.Log.d("HomeViewModel", "observeTips userId from prefs = $id")
                }
                .filter { it > 0L }
                .distinctUntilChanged()
                .collect { userId ->
                    val tipText = try {
                        android.util.Log.d("HomeViewModel", "Fetching random tip for userId = $userId")
                        tipsRepository.getRandomTipForUser(userId)
                    } catch (e: Exception) {
                        android.util.Log.e("HomeViewModel", "Error loading random tip", e)
                        "Log your meals and activities daily to get more accurate insights."
                    }

                    android.util.Log.d("HomeViewModel", "Final tip text = $tipText")

                    _uiState.update { state ->
                        state.copy(tips = tipText)
                    }
                }
        }
    }




    private fun observeUser() {
        viewModelScope.launch {
            prefsRepository.getUserId()
                .filter { it > 0L }              // only after user is created
                .distinctUntilChanged()
                .flatMapLatest { userId ->
                    userRepository.getUserById(userId)
                }
                .collect { user ->
                    _uiState.update { state ->
                        state.copy(
                            userName = user?.username ?: state.userName,
                            avatarUrl = user?.avatarUrl ?: state.avatarUrl
                        )
                    }
                }
        }
    }



    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            prefsRepository.getUserId()
                .filter { it > 0L }
                .distinctUntilChanged()
                .flatMapLatest { userId ->
                    combine(
                        activityRepository.getAllByUser(userId),
                        foodRepository.getAllByUser(userId),
                        _period,
                        _filter
                    ) { activities, foods, period, filter ->
                        val current = _uiState.value

                        buildStateFromData(
                            activities = activities,
                            foods = foods,
                            period = period,
                            filter = filter,                 // use filter from Flow
                            currentUserName = current.userName,
                            currentAvatarUrl = current.avatarUrl,
                            currentTips = current.tips
                        )
                    }
                }
                .collect { newState ->
                    _uiState.value = newState.copy(isLoading = false)
                }
        }
    }




    private fun buildStateFromData(
        activities: List<ActivityLog>,
        foods: List<FoodLog>,
        period: ActivityPeriod,
        filter: ActivityFilter,
        currentUserName: String,
        currentAvatarUrl: String,
        currentTips: String
    ): HomeUiState {
        val summary = buildSummary(activities, foods, period)

        val limitedItems = buildActivityItems(
            activities = activities,
            foods = foods,
            filter = filter,
            limit = 5
        )

        return HomeUiState(
            isLoading = false,
            userName = currentUserName,
            avatarUrl = currentAvatarUrl,
            summary = summary,
            filter = filter,
            activities = limitedItems,
            tips = currentTips
        )
    }



    private fun buildSummary(
        activities: List<ActivityLog>,
        foods: List<FoodLog>,
        period: ActivityPeriod
    ): ActivitySummaryUiState {
        val now = System.currentTimeMillis()
        val fromTs = when (period) {
            ActivityPeriod.DAILY -> now - TimeUnit.DAYS.toMillis(1)
            ActivityPeriod.WEEKLY -> now - TimeUnit.DAYS.toMillis(7)
        }

        val actsInRange = activities.filter { it.timestamp >= fromTs }
        val foodsInRange = foods.filter { it.timestamp >= fromTs }

        // Map to Int explicitly then sum() – use 0.0 to keep Double type before toInt()
        val activeMinutes = actsInRange.sumOf { (it.durationMin ?: 0.0).toInt() }

        val caloriesBurned = actsInRange.sumOf { (it.calories ?: 0.0).toInt() }

        val caloriesIntake = foodsInRange.sumOf { food -> food.calories!!.toInt() }

        return ActivitySummaryUiState(
            activeMinutes = activeMinutes,
            caloriesIntake = caloriesIntake,
            caloriesBurned = caloriesBurned,
            period = period
        )
    }

    private fun buildActivityItems(
        activities: List<ActivityLog>,
        foods: List<FoodLog>,
        filter: ActivityFilter,
        limit: Int = 10
    ): List<ActivityItemUiState> {
        val formatter = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())

        // Internal holder with timestamp to help sorting
        data class ItemWithTime(
            val item: ActivityItemUiState,
            val timestamp: Long
        )

        val actItems = activities.map { act ->
            val duration = (act.durationMin ?: 0.0).toInt()
            val calories = (act.calories ?: 0.0).toInt()
            val rawTitle = act.title ?: "Activity"

            val title = if (duration > 0) {
                "$rawTitle · $duration min"
            } else {
                rawTitle
            }

            val subtitle = buildString {
                append(formatter.format(Date(act.timestamp)))
                if (calories > 0) append(" · $calories kcal burned")
            }

            ItemWithTime(
                item = ActivityItemUiState(
                    id = act.id,
                    type = ActivityType.EXERCISE,
                    title = title,
                    subtitle = subtitle,
                    caloriesText = if (calories > 0) "-$calories kcal" else "-",
                    icon = guessExerciseIcon(rawTitle)
                ),
                timestamp = act.timestamp
            )
        }

        val foodItems = foods.map { food ->
            val calories = food.calories?.toInt() ?: 0
            val rawTitle = food.name

            val subtitle = buildString {
                append(formatter.format(Date(food.timestamp)))
                if (calories > 0) append(" · $calories kcal intake")
            }

            ItemWithTime(
                item = ActivityItemUiState(
                    id = food.id,
                    type = ActivityType.FOOD,
                    title = rawTitle,
                    subtitle = subtitle,
                    caloriesText = "+$calories kcal",
                    icon = ActivityIcon.FOOD
                ),
                timestamp = food.timestamp
            )
        }

        val merged: List<ItemWithTime> = when (filter) {
            ActivityFilter.ALL -> actItems + foodItems      // mix together
            ActivityFilter.EXERCISE -> actItems
            ActivityFilter.FOOD -> foodItems
        }

        return merged
            .sortedByDescending { it.timestamp } // latest first
            .take(limit)                         // limit to 10 items
            .map { it.item }
    }

    private fun guessExerciseIcon(title: String): ActivityIcon {
        val t = title.lowercase(Locale.getDefault())
        return when {
            "run" in t || "jog" in t -> ActivityIcon.RUNNING
            "cycle" in t || "bike" in t -> ActivityIcon.CYCLING
            "drink" in t || "water" in t -> ActivityIcon.DRINK
            else -> ActivityIcon.RUNNING
        }
    }

    /** Called when user taps “Daily Activity” / “Weekly Activity”. */
    fun onPeriodChange(period: ActivityPeriod) {
        _period.value = period
        // summary.period will update automatically via combine
    }

    /** Called when user selects filter All / Exercise / Food. */
    fun onFilterChange(filter: ActivityFilter) {
        // Drive the filter Flow so combine(...) recomputes immediately
        _filter.value = filter
    }

}
