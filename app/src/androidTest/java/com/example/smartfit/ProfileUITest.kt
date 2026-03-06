package com.example.smartfit

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartfit.ui.profile.*
import com.example.smartfit.ui.theme.SmartFitTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileUITest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    /** ⭐ TEST 1 — Profile Header 显示 username 与 avatar */
    @Test
    fun profileHeader_showsUsername() {
        composeRule.setContent {
            SmartFitTheme {
                ProfileHeaderCard(
                    name = "Alex",
                    avatarUrl = "https://i.pravatar.cc/150?img=12",
                    onEdit = {}
                )
            }
        }

        composeRule.onNodeWithText("Alex").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Avatar").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Edit Profile").assertIsDisplayed()
    }

    /** ⭐ TEST 2 — AccountCard 显示 Email 与 Manage Password */
    @Test
    fun accountCard_showsEmail() {
        composeRule.setContent {
            SmartFitTheme {
                AccountCard(
                    email = "alex@example.com",
                    onManagePassword = {}
                )
            }
        }

        composeRule.onNodeWithText("Account").assertIsDisplayed()
        composeRule.onNodeWithText("Email").assertIsDisplayed()
        composeRule.onNodeWithText("alex@example.com").assertIsDisplayed()
        composeRule.onNodeWithText("Manage Password").assertIsDisplayed()
    }

    /** ⭐ TEST 3 — AppearanceSettingsCard 显示 3 theme options */
    @Test
    fun appearanceCard_showsThemeOptions() {
        composeRule.setContent {
            SmartFitTheme {
                AppearanceSettingsCard(
                    selectedTheme = "LIGHT",
                    onThemeChange = {}
                )
            }
        }

        composeRule.onNodeWithText("Appearance").assertIsDisplayed()

        composeRule.onNodeWithText("Light").assertIsDisplayed()
        composeRule.onNodeWithText("Dark").assertIsDisplayed()
        composeRule.onNodeWithText("System").assertIsDisplayed()
    }
}
