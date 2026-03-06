package com.example.smartfit.ui.components

import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartfit.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    dark: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val bg = if (dark) GlassDark else GlassLight
    val border = if (dark) GlassBorderDark else GlassBorderLight

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}
