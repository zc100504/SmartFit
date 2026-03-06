// app/src/main/java/com/example/smartfit/ui/navigation/SmartFitAdaptiveRoot.kt
package com.example.smartfit.ui.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.smartfit.ui.theme.DarkBackground
import com.example.smartfit.ui.theme.LightBackground   // 如果你的 theme 文件名字不一样，改成对应的

/**
 * Adaptive root:
 * - Phone: single pane
 * - Tablet portrait: nav rail + main (2 panes)
 * - Tablet landscape:
 *      - no detail: nav rail + main (2 panes)
 *      - with detail: nav rail + main + detail (3 panes, width animated)
 */
@Composable
fun SmartFitAdaptiveRoot(
    windowSizeClass: WindowSizeClass,
    isTabletLandscape: Boolean,
    hasDetailPane: Boolean,
    navigationBar: @Composable () -> Unit,
    mainPane: @Composable () -> Unit,
    detailPane: @Composable () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val widthClass = windowSizeClass.widthSizeClass
    val isTablet = widthClass >= WindowWidthSizeClass.Medium

    // ✅ 跟 bottom bar 一样的深色判断
    val isDark = colorScheme.background == DarkBackground
    val rootBg = if (isDark) DarkBackground else LightBackground

    // ---- Phone: single pane ----
    if (!isTablet) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(rootBg)
        ) {
            mainPane()
        }
        return
    }

    // ---- Tablet portrait: nav + main (2 panes) ----
    if (!isTabletLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(rootBg)
        ) {
            //rail panel
            Box(
                modifier = Modifier
                    .width(88.dp)
                    .fillMaxHeight()
            ) {
                navigationBar()
            }

            // main panel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                mainPane()
            }
        }
        return
    }

    // ---- Tablet landscape: nav + main + animated detail (2 → 3 panes) ----

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp

    val targetDetailWidth =
        if (hasDetailPane) screenWidthDp * 0.33f else 0.dp

    val detailWidth by animateDpAsState(
        targetValue = targetDetailWidth,
        label = "detailWidth"
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(rootBg)
    ) {
        // rail panel
        Box(
            modifier = Modifier
                .width(88.dp)
                .fillMaxHeight()
        ) {
            navigationBar()
        }

        // main panel
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            mainPane()
        }

        // detail panel
        Box(
            modifier = Modifier
                .width(detailWidth)
                .fillMaxHeight()
                .padding(start = if (detailWidth > 0.dp) 8.dp else 0.dp)
        ) {
            if (detailWidth > 0.dp) {
                Box(Modifier.fillMaxSize()) {
                    detailPane()
                }
            }
        }
    }
}
