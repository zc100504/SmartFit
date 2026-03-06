package com.example.smartfit.ui.auth

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
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.security.MessageDigest

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepo: UserRepository = mockk()
    private val prefs: PrefsRepository = mockk(relaxed = true)

    // same hashing as in LoginViewModel
    private fun sha256(input: String): String {
        return MessageDigest
            .getInstance("SHA-256")
            .digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }

    // ✅ helper to mock android.util.Log
    private fun mockAndroidLog() {
        mockkStatic(Log::class)

        every { Log.d(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.i(any(), any<String>()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
    }


    @Test
    fun login_withEmptyFields_setsErrorAndDoesNotCallRepo() = runTest {
        mockAndroidLog()

        val vm = LoginViewModel(userRepo, prefs)

        vm.onEmail("")          // blank
        vm.onPassword("")       // blank
        vm.login()

        val state = vm.state.value
        assertEquals("Please enter username and password.", state.error)

        coVerify(exactly = 0) { userRepo.login(any(), any()) }
    }

    @Test
    fun login_withCorrectCredentials_setsPrefs_andEmitsNavigateHome() = runTest {
        mockAndroidLog()

        val email = "user@example.com"
        val password = "secret123"
        val passwordHash = sha256(password)

        val fakeUser = User(
            id = 1L,
            username = "User",
            email = email,
            passwordHash = passwordHash
        )

        coEvery { userRepo.login(email, passwordHash) } returns fakeUser

        val vm = LoginViewModel(userRepo, prefs)
        vm.onEmail(email)
        vm.onPassword(password)

        vm.login()

        val effect = vm.effect.first()
        assertEquals(LoginEffect.NavigateHome, effect)

        coVerify { userRepo.login(email, passwordHash) }
        coVerify { prefs.setLoggedIn(true) }
        coVerify { prefs.setUserId(fakeUser.id) }

        val state = vm.state.value
        assertEquals(false, state.loading)
        assertNull(state.error)
    }

    @Test
    fun login_withWrongCredentials_setsErrorAndDoesNotTouchPrefs() = runTest {
        mockAndroidLog()

        val email = "wrong@example.com"
        val password = "badpass"
        val passwordHash = sha256(password)

        coEvery { userRepo.login(email, passwordHash) } returns null

        val vm = LoginViewModel(userRepo, prefs)
        vm.onEmail(email)
        vm.onPassword(password)
        vm.login()

        val state = vm.state.value
        assertEquals("Invalid email or password", state.error)

        coVerify(exactly = 0) { prefs.setLoggedIn(true) }
        coVerify(exactly = 0) { prefs.setUserId(any()) }
    }
}
