// app/src/main/java/com/example/smartfit/ui/tips/NewTipQuestionScreen.kt
package com.example.smartfit.ui.tips

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smartfit.ui.theme.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign


@Composable
fun NewTipQuestionScreen(
    onBackClick: () -> Unit,
    onSubmitQuestion: (String) -> Unit,
    isSubmitting: Boolean,
    modifier: Modifier = Modifier
) {
    var input by remember { mutableStateOf("") }
    val colorScheme = MaterialTheme.colorScheme

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            colorScheme.background,
            colorScheme.background.copy(alpha = 0.96f)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        Text(
            text = "What kind of fitness tip\nwould you like to know?",
            style = MaterialTheme.typography.headlineMedium,
            color = colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )

        TipInputBar(
            value = input,
            onValueChange = { if (!isSubmitting) input = it },
            onSendClick = {
                if (input.isNotBlank() && !isSubmitting) {
                    onSubmitQuestion(input.trim())

                }
            },
            isSending = isSubmitting,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .imePadding()
        )
    }
}

@Composable
fun TipInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Ask a fitness question...",
    isSending: Boolean = false
) {
    val isDark = isSmartFitDarkTheme()
    val colorScheme = MaterialTheme.colorScheme

    val barColor =
        if (isDark) DarkSurfaceGlass else LightSurfaceGlass

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 4.dp,
        color = barColor,
        border = BorderStroke(
            1.dp,
            if (isDark) GlassBorderDark else GlassBorderLight
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        placeholder,
                        color = colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                maxLines = 3,
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface
                )
            )

            Spacer(Modifier.width(8.dp))

            IconButton(
                onClick = onSendClick,
                enabled = value.isNotBlank() && !isSending   // ✅ 发送中禁用
            ) {
                if (isSending) {
                    // 小小一个 loading 圈，不会太夸张
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Send",
                        tint = if (value.isNotBlank())
                            colorScheme.onPrimary
                        else
                            colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

// ---- PREVIEWS ----

@Preview(
    name = "New Question - Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun NewTipQuestionPreviewLight() {
    SmartFitTheme(darkTheme = false) {
        NewTipQuestionScreen(
            onBackClick = {},
            onSubmitQuestion = {},
            isSubmitting = false
        )
    }
}

@Preview(
    name = "New Question - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun NewTipQuestionPreviewDark() {
    SmartFitTheme(darkTheme = true) {
        NewTipQuestionScreen(
            onBackClick = {},
            onSubmitQuestion = {},
            isSubmitting = false
        )
    }
}

@Preview(
    name = "Tip Input Bar",
    showBackground = true
)
@Composable
fun TipInputBarPreview() {
    SmartFitTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            TipInputBar(
                value = "What can I do to reduce knee pain when running?",
                onValueChange = {},
                onSendClick = {}
            )
        }
    }
}
