// app/src/main/java/com/example/smartfit/ui/AppViewModelProvider.kt
package com.example.smartfit.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.smartfit.SmartFitApplication
import com.example.smartfit.di.AppGraph
import com.example.smartfit.ui.activitystats.ActivityStatsViewModel   // 👈 NEW
import com.example.smartfit.ui.auth.LoginViewModel
import com.example.smartfit.ui.auth.SignUpViewModel
import com.example.smartfit.ui.logs.AddFoodViewModel
import com.example.smartfit.ui.logs.AddLogViewModel
import com.example.smartfit.ui.logs.LogDetailViewModel
import com.example.smartfit.ui.logs.LogsViewModel
import com.example.smartfit.ui.profile.ProfileViewModel
import com.example.smartfit.ui.tips.TipsViewModel
import com.example.smartfit.ui.home.HomeViewModel

object AppViewModelProvider {

    val Factory = viewModelFactory {

        // --- Auth ---
        initializer {
            val graph = appGraph()
            LoginViewModel(graph.userRepo, graph.prefsRepo)
        }

        initializer {
            val graph = appGraph()
            SignUpViewModel(graph.userRepo, graph.prefsRepo)
        }

        // --- Logs list ---
        initializer {
            val graph = appGraph()
            LogsViewModel(
                activityRepository = graph.activityRepo,
                foodRepository = graph.foodRepo,
                prefsRepository = graph.prefsRepo
            )
        }

        // --- Log detail ---
        initializer {
            val graph = appGraph()
            LogDetailViewModel(
                activityRepository = graph.activityRepo,
                foodRepository = graph.foodRepo,
                prefsRepository = graph.prefsRepo,
                savedStateHandle = this.createSavedStateHandle()
            )
        }

        // --- Add activity log form ---
        initializer {
            val graph = appGraph()
            AddLogViewModel(
                activityRepository = graph.activityRepo,
                prefsRepository = graph.prefsRepo,
                savedStateHandle = this.createSavedStateHandle()
            )
        }

        // --- Add food log form ---
        initializer {
            val graph = appGraph()
            AddFoodViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                foodRepository = graph.foodRepo,
                prefsRepository = graph.prefsRepo
            )
        }

        // --- Tips ---
        initializer {
            val graph = appGraph()
            TipsViewModel(
                tipsRepo = graph.tipsRepo,
                prefsRepo = graph.prefsRepo
            )
        }

        // --- Profile (theme, goal, logout) ---
        initializer {
            val graph = appGraph()
            // Pass both required repositories to the ViewModel
            ProfileViewModel(
                prefs = graph.prefsRepo,
                userRepository = graph.userRepo,
            )
        }



        // --- Home ---
        initializer {
            val graph = appGraph()
            HomeViewModel(
                activityRepository = graph.activityRepo,
                foodRepository = graph.foodRepo,
                prefsRepository = graph.prefsRepo,
                tipsRepository= graph.tipsRepo,
                userRepository= graph.userRepo
            )
        }


        initializer {
            val graph = appGraph()
            ActivityStatsViewModel(
                activityRepo = graph.activityRepo,
                foodRepo = graph.foodRepo,
                prefsRepo = graph.prefsRepo
            )
        }
    }
}

/** Resolve our Application → AppGraph from CreationExtras. */
private fun CreationExtras.appGraph(): AppGraph {
    val app = (this[AndroidViewModelFactory.APPLICATION_KEY] as Application) as SmartFitApplication
    return app.graph
}
