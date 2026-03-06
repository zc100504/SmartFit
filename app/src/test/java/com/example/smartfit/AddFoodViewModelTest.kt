package com.example.smartfit.ui.logs

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.example.smartfit.MainDispatcherRule
import com.example.smartfit.data.repository.FoodRepository
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
class AddFoodViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val foodRepo: FoodRepository = mockk(relaxed = true)
    private val prefsRepo: PrefsRepository = mockk()

    /**
     * Helper to create ViewModel with Log.d mocked,
     * so it doesn't crash in local unit tests.
     */
    private fun createViewModel(): AddFoodViewModel {
        // Mock static android.util.Log.d
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        return AddFoodViewModel(
            savedStateHandle = SavedStateHandle(),   // logId = -1 → new mode
            foodRepository = foodRepo,
            prefsRepository = prefsRepo
        )
    }

    /**
     * Test 1:
     * onCaloriesChange should keep only digits.
     */
    @Test
    fun onCaloriesChange_keepsOnlyDigits() = runTest {
        every { prefsRepo.getUserId() } returns flowOf(1L)

        val vm = createViewModel()

        vm.onCaloriesChange("100kcal")
        assertEquals("100", vm.uiState.value.calories)

        vm.onCaloriesChange("2a5b0")
        assertEquals("250", vm.uiState.value.calories)
    }

    /**
     * Test 2 (core feature):
     * When the form is valid and isReady = true (NEW log),
     * saveFoodLog() should call foodRepository.insert().
     */
    @Test
    fun saveFoodLog_whenFormValidAndReady_callsInsert() = runTest {
        val userId = 1L

        every { prefsRepo.getUserId() } returns flowOf(userId)
        coEvery { foodRepo.insert(any()) } returns Unit

        val vm = createViewModel()

        vm.onFoodNameChange("Chicken Rice")
        vm.onCaloriesChange("650")
        vm.onMealTypeChange("Lunch")
        vm.onDescriptionChange("Nice lunch")

        vm.isFormValid.first { it }
        vm.isReady.first { it }

        vm.saveFoodLog()

        coVerify(exactly = 1) { foodRepo.insert(any()) }
    }
}
