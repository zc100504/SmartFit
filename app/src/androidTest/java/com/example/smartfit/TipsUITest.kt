package com.example.smartfit

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartfit.ui.tips.*
import com.example.smartfit.ui.theme.SmartFitTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TipsUITest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    /** ⭐ TEST 1 — Tips Menu header + subtitle 出现 */
    @Test
    fun tipsMenu_showsHeader() {

        val uiState = TipsMenuUiState(
            isLoading = false,
            threads = emptyList()
        )

        composeRule.setContent {
            SmartFitTheme {
                TipsMenuContent(
                    uiState = uiState,
                    onNewQuestionClick = {},
                    onTipClick = {},
                    onDeleteTip = {}
                )
            }
        }

        composeRule.onNodeWithText("Tips").assertIsDisplayed()
        composeRule.onNodeWithText("Your saved AI fitness tips").assertIsDisplayed()
    }


    /** ⭐ TEST 2 — Tips Menu 空状态显示 */
    @Test
    fun tipsMenu_showsEmptyState() {

        val uiState = TipsMenuUiState(
            isLoading = false,
            threads = emptyList()
        )

        composeRule.setContent {
            SmartFitTheme {
                TipsMenuContent(
                    uiState = uiState,
                    onNewQuestionClick = {},
                    onTipClick = {},
                    onDeleteTip = {}
                )
            }
        }

        composeRule.onNodeWithText("No tips yet.\nAsk a new question to get your first tip!").assertIsDisplayed()
    }


    /** ⭐ TEST 3 — 显示多个 TipCard */
    @Test
    fun tipsMenu_showsMultipleTipCards() {

        val uiState = TipsMenuUiState(
            isLoading = false,
            threads = listOf(
                TipThreadUiState("1", "Evening Stretch", "preview A"),
                TipThreadUiState("2", "Office Posture", "preview B")
            )
        )

        composeRule.setContent {
            SmartFitTheme {
                TipsMenuContent(
                    uiState = uiState,
                    onNewQuestionClick = {},
                    onTipClick = {},
                    onDeleteTip = {}
                )
            }
        }

        composeRule.onNodeWithText("Evening Stretch").assertIsDisplayed()
        composeRule.onNodeWithText("Office Posture").assertIsDisplayed()
    }
}
