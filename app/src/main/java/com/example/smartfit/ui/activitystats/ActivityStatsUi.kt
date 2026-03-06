package com.example.smartfit.ui.activitystats

enum class StatsPeriod { DAY, WEEK }


data class ActivityStatsUiState(
    val period: StatsPeriod,
    val dateLabel: String,
    val totalDurationMinutes: Int,
    val totalDistanceKm: Float,
    val caloriesIntake: Int,
    val caloriesBurned: Int,
    val distancePoints: List<Float>,
    // 新增
    val caloriesBurnedPoints: List<Float>,
    val stepsPoints: List<Float>,
    val currentSteps: Int,
    val goalSteps: Int
)

