package com.example.smartfit

import android.graphics.Color as AColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import com.example.smartfit.ui.navigation.AppNavHost
import com.example.smartfit.ui.theme.SmartFitTheme
import com.example.smartfit.ui.theme.ThemeViewModel

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val graph = (application as SmartFitApplication).graph


        setContent {
            // 1) Window size (phone / tablet)
            val windowSizeClass: WindowSizeClass = calculateWindowSizeClass(this)

            // 2) Theme (same as before)
            val themeVm = remember { ThemeViewModel(graph.prefsRepo) }

            val mode by themeVm.themeMode.collectAsState()
            val darkTheme = when (mode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            LaunchedEffect(darkTheme) {
                val transparent = AColor.TRANSPARENT
                val darkScrim = AColor.argb(0x66, 0, 0, 0)
                enableEdgeToEdge(
                    statusBarStyle = if (darkTheme)
                        SystemBarStyle.dark(transparent)
                    else
                        SystemBarStyle.light(transparent, darkScrim),
                    navigationBarStyle = if (darkTheme)
                        SystemBarStyle.dark(transparent)
                    else
                        SystemBarStyle.light(transparent, darkScrim)
                )
            }

            SmartFitTheme(darkTheme = darkTheme) {
                Surface {
                    AppNavHost(
                        graph = graph,
                        windowSizeClass = windowSizeClass
                    )
                }
            }
        }
    }
}
