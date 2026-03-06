package com.example.smartfit

import android.app.Application
import com.example.smartfit.di.AppContainer // Import the concrete implementation
import com.example.smartfit.di.AppGraph

/**
 * The Application class for SmartFit.
 * It is responsible for creating and holding the application-wide dependency graph.
 */
class SmartFitApplication : Application() {
    // The graph property is correctly typed as the interface for flexibility.
    lateinit var graph: AppGraph

    override fun onCreate() {
        super.onCreate()
        // --- THE FIX ---
        // Instantiate the AppContainer, which is the concrete implementation of AppGraph.
        graph = AppContainer(this)
    }
}
