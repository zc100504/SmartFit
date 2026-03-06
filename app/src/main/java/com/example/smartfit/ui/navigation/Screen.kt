
// app/src/main/java/com/example/smartfit/ui/navigation/Screen.kt
package com.example.smartfit.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object SignUp : Screen("signup")
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Logs : Screen("logs")
    data object LogDetail : Screen("log/{id}") {
        fun createRoute(id: Long) = "log/$id"
    }
    data object AddLog : Screen("log/new")
    data object Tips : Screen("tips")
    data object Profile : Screen("profile")
}
