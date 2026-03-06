package com.example.smartfit.ui.navigation

// FIX: Remove unused imports from the old navigation system
// import androidx.navigation.NamedNavArgument
// import androidx.navigation.NavType
// import androidx.navigation.navArgument

@kotlinx.serialization.Serializable
sealed interface Dest {

    // Base route string (mainly needed if you ever use raw string routes)
    val route: String
        get() = this::class.qualifiedName!!

    @kotlinx.serialization.Serializable
    data object Login : Dest {
        override val route: String = "login"
    }

    @kotlinx.serialization.Serializable
    data object SignUp : Dest {
        override val route: String = "signup"
    }

    @kotlinx.serialization.Serializable
    data object Home : Dest {
        override val route: String = "home"
    }

    // --- FIX: Add the destination for the Edit Profile screen ---
    @kotlinx.serialization.Serializable
    data object EditProfile : Dest {
        override val route: String = "edit_profile"
    }

    // In app/src/main/java/com/example/smartfit/ui/navigation/Dest.kt
    @kotlinx.serialization.Serializable
    data object ChangePassword : Dest {
        override val route: String = "change_password"
    }


    @kotlinx.serialization.Serializable
    data object PrivacyPolicy : Dest {
        override val route: String = "privacy_policy"
    }

    @kotlinx.serialization.Serializable
    data object Faq : Dest {
        override val route: String = "faq"
    }

    @kotlinx.serialization.Serializable
    data object Logs : Dest {
        override val route: String = "logs"
    }

    // Detail for a specific log (food or activity)
    @kotlinx.serialization.Serializable
    data class LogDetail(
        val id: Long,
        val type: String     // e.g. "food" or "activity"
    ) : Dest

    /**
     * Navigates to the screen for adding or editing a food log.
     * @param logId The ID of the food log to edit. Pass -1L to create a new log.
     */
    @kotlinx.serialization.Serializable
    data class AddFoodLog(val logId: Long = -1L) : Dest

    /**
     * Navigates to the screen for adding or editing an activity log.
     * @param logId The ID of the activity log to edit. Pass -1L to create a new log.
     */
    @kotlinx.serialization.Serializable
    data class AddActivityLog(val logId: Long = -1L) : Dest

    @kotlinx.serialization.Serializable
    data object Tips : Dest {
        override val route: String = "tips"
    }

    // === Your extra tips destinations ===

    @kotlinx.serialization.Serializable
    data object TipsNew : Dest {
        override val route: String = "tips_new"   // you can rename this if you want
    }

    @kotlinx.serialization.Serializable
    data class TipDetail(
        val threadId: String
    ) : Dest
    // (no need to override route for data class unless you really want a custom string)

    @kotlinx.serialization.Serializable
    data object Profile : Dest {
        override val route: String = "profile"
    }

    @kotlinx.serialization.Serializable
    data object ActivityStats : Dest {
        override val route: String = "activity_stats"
    }
}
