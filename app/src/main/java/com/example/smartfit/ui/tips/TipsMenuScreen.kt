// app/src/main/java/com/example/smartfit/ui/tips/TipsMenuScreen.kt
package com.example.smartfit.ui.tips

import android.content.res.Configuration
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartfit.ui.AppViewModelProvider
import com.example.smartfit.ui.theme.*

@Composable
fun TipsMenuScreen(
    onNewQuestionClick: () -> Unit,
    onTipClick: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: TipsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.menuUiState.collectAsState()

    TipsMenuContent(
        uiState = uiState,
        onNewQuestionClick = onNewQuestionClick,
        onTipClick = onTipClick,
        onDeleteTip = { id -> viewModel.deleteTip(id) },
        modifier = modifier.padding(contentPadding)
    )
}

@Composable
fun TipsMenuContent(
    uiState: TipsMenuUiState,
    onNewQuestionClick: () -> Unit,
    onTipClick: (String) -> Unit,
    onDeleteTip: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            colorScheme.background,
            colorScheme.background.copy(alpha = 0.95f)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Tips",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Your saved AI fitness tips",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(Modifier.height(16.dp))

                if (uiState.threads.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tips yet.\nAsk a new question to get your first tip!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.threads, key = { it.id }) { thread ->
                            androidx.compose.animation.AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + scaleIn(initialScale = 0.95f)
                            ) {
                                TipCard(
                                    thread = thread,
                                    onClick = { onTipClick(thread.id) },
                                    onDeleteClick = { onDeleteTip(thread.id) }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                TipNewQuestionButton(onNewQuestionClick)
            }
        }
    }
}

@Composable
fun TipCard(
    thread: TipThreadUiState,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isDark = isSmartFitDarkTheme()
    val colorScheme = MaterialTheme.colorScheme

    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        modifier = Modifier.height(110.dp),
        shape = RoundedCornerShape(18.dp),
        color = if (isDark) DarkSurfaceGlass else LightSurfaceGlass,
        tonalElevation = 2.dp,
        border = BorderStroke(
            1.dp,
            if (isDark) GlassBorderDark else GlassBorderLight
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = thread.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    color = colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "More options"
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete tip") },
                            onClick = {
                                menuExpanded = false
                                showDeleteConfirm = true
                            }
                        )
                    }
                }
            }

            Text(
                text = thread.preview,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                color = colorScheme.onSurface.copy(alpha = 0.75f)
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete tip?") },
            text = {
                Text(
                    "This action cannot be undone. Do you really want to delete this tip?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteClick()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = LimePrimary   // bright, on-brand
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                ) {
                    Text("Cancel")
                }
            },
            // use individual color params instead of `colors = ...`
            containerColor = MaterialTheme.colorScheme.surface,
            iconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun TipNewQuestionButton(
    onNewQuestionClick: () -> Unit,
) {
    Button(
        onClick = onNewQuestionClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = ButtonDefaults.buttonColors(
            containerColor = LimePrimary,
            contentColor = Color(0xFF020617)
        )
    ) {
        Text(
            text = "New Question",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/* -------- PREVIEWS -------- */

@Preview(
    name = "Tips Menu - Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun TipsMenuPreviewLight() {
    SmartFitTheme(darkTheme = false) {
        val dummyState = TipsMenuUiState(
            threads = listOf(
                TipThreadUiState(
                    id = "1",
                    title = "Evening Stretch",
                    preview = "Short stretching routine before bed to relax muscles and improve sleep."
                ),
                TipThreadUiState(
                    id = "2",
                    title = "Office Posture",
                    preview = "How to keep a healthy posture at your desk during long work hours."
                ),
                TipThreadUiState(
                    id = "3",
                    title = "Cardio Starter",
                    preview = "Beginner-friendly cardio plan for 3 days per week."
                )
            )
        )

        TipsMenuContent(
            uiState = dummyState,
            onNewQuestionClick = {},
            onTipClick = {},
            onDeleteTip = {},
            modifier = Modifier
        )
    }
}

@Preview(
    name = "Tips Menu - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun TipsMenuPreviewDark() {
    SmartFitTheme(darkTheme = true) {
        val dummyState = TipsMenuUiState(
            threads = listOf(
                TipThreadUiState(
                    id = "1",
                    title = "Evening Stretch",
                    preview = "Short stretching routine before bed to relax muscles and improve sleep."
                ),
                TipThreadUiState(
                    id = "2",
                    title = "Office Posture",
                    preview = "How to keep a healthy posture at your desk during long work hours."
                )
            )
        )

        TipsMenuContent(
            uiState = dummyState,
            onNewQuestionClick = {},
            onTipClick = {},
            onDeleteTip = {},
            modifier = Modifier
        )
    }
}

@Preview(
    name = "Tips Menu - Dark (Empty)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun TipsMenuEmptyPreview() {
    SmartFitTheme(darkTheme = true) {
        val emptyState = TipsMenuUiState(
            isLoading = false,
            threads = emptyList()
        )

        TipsMenuContent(
            uiState = emptyState,
            onNewQuestionClick = {},
            onTipClick = {},
            onDeleteTip = {},
            modifier = Modifier
        )
    }
}

@Preview(
    name = "Single Tip Card",
    showBackground = true
)
@Composable
fun TipCardPreview() {
    SmartFitTheme {
        TipCard(
            thread = TipThreadUiState(
                id = "1",
                title = "Hydration Basics",
                preview = "How much water you should drink per day and how to spread it out."
            ),
            onClick = {},
            onDeleteClick = {}
        )
    }
}
