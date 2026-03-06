package com.example.smartfit.ui.logs

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.example.smartfit.MainDispatcherRule
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.data.repository.PrefsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddLogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val activityRepo: ActivityRepository = mockk(relaxed = true)
    private val prefsRepo: PrefsRepository = mockk()

    /** Helper: mock Log.d and create ViewModel safely for local unit tests */
    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()): AddLogViewModel {
        // Mock static android.util.Log.d so it doesn't crash
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        return AddLogViewModel(
            activityRepository = activityRepo,
            prefsRepository = prefsRepo,
            savedStateHandle = savedStateHandle
        )
    }

    /**
     * Test 1:
     * onDurationChange should keep only digits.
     */
    @Test
    fun onDurationChange_keepsOnlyDigits() = runTest {
        every { prefsRepo.getUserId() } returns flowOf(1L)

        val vm = createViewModel()

        vm.onDurationChange("30min")
        assertEquals("30", vm.state.value.durationMin)

        vm.onDurationChange("1h20m")
        assertEquals("120", vm.state.value.durationMin)
    }

    /**
     * Test 2 (core feature):
     * When the form is valid and isReady is true in NEW mode,
     * calling save() should insert a new ActivityLog.
     */
    @Test
    fun save_whenFormValidAndReady_callsInsert() = runTest {
        val userId = 1L

        every { prefsRepo.getUserId() } returns flowOf(userId)
        coEvery { activityRepo.insert(any()) } returns Unit

        val vm = createViewModel(SavedStateHandle()) // logId = -1 → new log

        vm.onTitleChange("Morning Run")
        vm.onTypeChange("Running")
        vm.onDurationChange("30")
        vm.onCaloriesChange("200")
        vm.onDistanceChange("5.0")
        vm.onNotesChange("Nice weather")

        vm.isFormValid.first { it }
        vm.isReady.first { it }

        vm.save()

        coVerify(exactly = 1) { activityRepo.insert(any()) }
    }
}
