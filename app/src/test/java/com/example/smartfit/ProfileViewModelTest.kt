package com.example.smartfit.ui.profile

import android.util.Log
import com.example.smartfit.MainDispatcherRule
import com.example.smartfit.data.model.User
import com.example.smartfit.data.repository.PrefsRepository
import com.example.smartfit.data.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.security.MessageDigest

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val prefs: PrefsRepository = mockk(relaxed = true)
    private val userRepo: UserRepository = mockk(relaxed = true)

    // Helper: same hashing as in ProfileViewModel
    private fun sha256(input: String): String {
        return MessageDigest
            .getInstance("SHA-256")
            .digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }

    /** Helper: mock Log and create ViewModel safely for local unit tests */
    private fun createViewModel(): ProfileViewModel {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        return ProfileViewModel(
            prefs = prefs,
            userRepository = userRepo
        )
    }

    /**
     * Test 1:
     * updateUserProfile should send updated user to UserRepository.
     */
    @Test
    fun updateUserProfile_updatesUserInRepository() = runTest {
        val userId = 1L
        val currentUser = User(
            id = userId,
            username = "OldName",
            email = "old@example.com",
            passwordHash = sha256("oldpass"),
            avatarUrl = "old_avatar.png"
        )

        every { prefs.getUserId() } returns flowOf(userId)
        every { prefs.themeMode() } returns flowOf("SYSTEM")
        every { userRepo.getUserById(userId) } returns flowOf(currentUser)

        val vm = createViewModel()

        // Wait until user is loaded (not null) before updating
        vm.user.first { it != null }

        // Act
        vm.updateUserProfile(
            newName = "NewName",
            newEmail = "new@example.com",
            newAvatarUrl = "new_avatar.png"
        )

        // Assert: updateUser() is called once
        coVerify(exactly = 1) { userRepo.updateUser(any()) }
    }

    /**
     * Test 2:
     * changePassword with correct current password
     * should call updatePassword and invoke onSuccess.
     */
    @Test
    fun changePassword_withCorrectCurrent_callsUpdatePassword_andOnSuccess() = runTest {
        val userId = 1L
        val currentPassword = "123456"
        val newPassword = "abcdef"

        val currentUser = User(
            id = userId,
            username = "User",
            email = "user@example.com",
            passwordHash = sha256(currentPassword),
            avatarUrl = ""
        )

        every { prefs.getUserId() } returns flowOf(userId)
        every { prefs.themeMode() } returns flowOf("SYSTEM")
        every { userRepo.getUserById(userId) } returns flowOf(currentUser)
        coEvery { userRepo.updatePassword(userId, any()) } returns Unit

        val vm = createViewModel()

        var successCalled = false
        var errorMessage: String? = null

        vm.changePassword(
            currentPassword = currentPassword,
            newPassword = newPassword,
            onSuccess = { successCalled = true },
            onError = { msg -> errorMessage = msg }
        )

        assertTrue(successCalled)
        assertEquals(null, errorMessage)

        coVerify {
            userRepo.updatePassword(userId, sha256(newPassword))
        }
    }

    /**
     * Test 3:
     * changePassword with WRONG current password
     * should call onError and NOT update the password.
     */
    @Test
    fun changePassword_withWrongCurrent_callsOnError_andDoesNotUpdate() = runTest {
        val userId = 1L
        val realPassword = "123456"
        val wrongPassword = "654321"

        val currentUser = User(
            id = userId,
            username = "User",
            email = "user@example.com",
            passwordHash = sha256(realPassword),
            avatarUrl = ""
        )

        every { prefs.getUserId() } returns flowOf(userId)
        every { prefs.themeMode() } returns flowOf("SYSTEM")
        every { userRepo.getUserById(userId) } returns flowOf(currentUser)

        val vm = createViewModel()

        var successCalled = false
        var errorMessage: String? = null

        vm.changePassword(
            currentPassword = wrongPassword,
            newPassword = "anything",
            onSuccess = { successCalled = true },
            onError = { msg -> errorMessage = msg }
        )

        assertFalse(successCalled)
        assertTrue(errorMessage?.contains("incorrect", ignoreCase = true) == true)

        coVerify(exactly = 0) { userRepo.updatePassword(userId, any()) }
    }
}
