// app/src/main/java/com/example/smartfit/ui/tips/TipConversationScreen.kt
package com.example.smartfit.ui.tips

import android.content.res.Configuration
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartfit.ui.theme.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.LaunchedEffect
import com.example.smartfit.ui.AppViewModelProvider


@Composable
fun TipConversationScreen(
    threadId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TipsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    LaunchedEffect(threadId) {
        viewModel.openThread(threadId)
    }

    val uiState by viewModel.conversationUiState.collectAsState()

    TipConversationContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onInputChange = viewModel::onInputChange,
        onSendClick = { text ->
            viewModel.sendMessageInCurrentThread(
                userQuestion = text
            )
        },
        modifier = modifier
    )
}

@Composable
fun TipConversationContent(
    uiState: TipConversationUiState,
    onBackClick: () -> Unit,
    onInputChange: (String) -> Unit,
    onSendClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val thread = uiState.thread

    val backgroundBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = thread?.title ?: "Tip",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(8.dp))

            if (uiState.isLoading || thread == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(thread.messages, key = { it.id }) { msg ->
                        TipMessageBubble(message = msg)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TipInputBar(
                    value = uiState.inputText,
                    onValueChange = onInputChange,
                    onSendClick = {
                        if (uiState.inputText.isNotBlank() && !uiState.isSending) {
                            onSendClick(uiState.inputText.trim())
                        }
                    },
                    isSending = uiState.isSending,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                )
            }
        }
    }
}

@Composable
private fun TipMessageBubble(
    message: TipMessageUiState
) {
    val isUser = message.author == TipMessageAuthor.USER
    val isDark = isSmartFitDarkTheme()
    val colorScheme = MaterialTheme.colorScheme

    val bubbleColor = when {
        isUser -> LimePrimary.copy(alpha = 0.95f)
        !isUser && isDark -> DarkSurfaceGlass
        else -> Color.White
    }

    val textColor = when {
        isUser -> Color(0xFF020617)
        isDark -> Color.White
        else -> Color(0xFF020617)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = bubbleColor,
            tonalElevation = 2.dp,
            border = if (isUser) {
                BorderStroke(1.dp, LimePrimary)
            } else {
                BorderStroke(
                    1.dp,
                    if (isDark) GlassBorderDark else GlassBorderLight
                )
            }
        ) {
            if (message.isPlaceholder && !isUser) {
                TypingIndicator(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            } else {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun TypingIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 600, delayMillis = 0),
            RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 600, delayMillis = 150),
            RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 600, delayMillis = 300),
            RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Dot(alpha1)
        Dot(alpha2)
        Dot(alpha3)
    }
}


@Composable
private fun Dot(alpha: Float) {
    Box(
        modifier = Modifier
            .size(6.dp)
            .background(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                shape = CircleShape
            )
    )
}



// ---- PREVIEWS ----

@Preview(
    name = "Conversation - Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun TipConversationPreviewLight() {
    SmartFitTheme(darkTheme = false) {
        val dummyThread = TipThreadUiState(
            id = "1",
            title = "Evening Stretch",
            preview = "Short stretching tips...",
            messages = listOf(
                TipMessageUiState(
                    author = TipMessageAuthor.USER,
                    text = "What can I do to relax my muscles after a long day?"
                ),
                TipMessageUiState(
                    author = TipMessageAuthor.ASSISTANT,
                    text = "Try a 10-minute full-body stretch focusing on neck, shoulders and lower back."
                ),
                TipMessageUiState(
                    author = TipMessageAuthor.USER,
                    text = "Can I do it before sleep?"
                ),
                TipMessageUiState(
                    author = TipMessageAuthor.ASSISTANT,
                    text = "Yes, doing it 30–60 minutes before sleep can improve your sleep quality."
                )
            )
        )

        val uiState = TipConversationUiState(
            isLoading = false,
            thread = dummyThread,
            inputText = "Any suggestion for weekends?",
            isSending = false
        )

        TipConversationContent(
            uiState = uiState,
            onBackClick = {},
            onInputChange = {},
            onSendClick = {},
        )
    }
}

@Preview(
    name = "Conversation - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun TipConversationPreviewDark() {
    SmartFitTheme(darkTheme = true) {
        val dummyThread = TipThreadUiState(
            id = "1",
            title = "Evening Stretch",
            preview = "Short stretching tips...",
            messages = listOf(
                TipMessageUiState(
                    author = TipMessageAuthor.USER,
                    text = "What can I do to relax my muscles after a long day?"
                ),
                TipMessageUiState(
                    author = TipMessageAuthor.ASSISTANT,
                    text = "Try a 10-minute full-body stretch focusing on neck, shoulders and lower back."
                )
            )
        )

        val uiState = TipConversationUiState(
            isLoading = false,
            thread = dummyThread,
            inputText = "",
            isSending = false
        )

        TipConversationContent(
            uiState = uiState,
            onBackClick = {},
            onInputChange = {},
            onSendClick = {},
        )
    }
}
