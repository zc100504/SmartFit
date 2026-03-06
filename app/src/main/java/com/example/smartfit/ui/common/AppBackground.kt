package com.example.smartfit.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.smartfit.R

@Composable
fun RegisterBackground(isDark: Boolean) {
    val resId = if (isDark) R.drawable.register_bg_pattern_dark else R.drawable.register_bg_pattern_light
    Box(Modifier.fillMaxSize()) {
        Image(painterResource(resId), null, contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    if (isDark)
                        listOf(Color.Black.copy(0.10f), Color.Black.copy(0.45f), Color.Black.copy(0.70f))
                    else
                        listOf(Color.Black.copy(0.50f), Color.Black.copy(0.50f), Color.Black.copy(0.50f))
                )
            )
        )
    }
}

@Composable
fun LoginBackground(isDark: Boolean) {
    val resId = if (isDark) R.drawable.login_bg_pattern_dark else R.drawable.login_bg_pattern_light
    Box(Modifier.fillMaxSize()) {
        Image(painterResource(resId), null, contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    if (isDark)
                        listOf(Color.Black.copy(0.10f), Color.Black.copy(0.45f), Color.Black.copy(0.70f))
                    else
                        listOf(Color.Black.copy(0.50f), Color.Black.copy(0.50f), Color.Black.copy(0.50f))
                )
            )
        )
    }
}
