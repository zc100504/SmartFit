// app/src/test/java/com/example/smartfit/ui/tips/TipsViewModelTest.kt
package com.example.smartfit

import android.util.Log
import com.example.smartfit.data.repository.PrefsRepository
import com.example.smartfit.data.repository.TipsRepository
import com.example.smartfit.ui.tips.TipsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TipsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tipsRepo: TipsRepository = mockk()
    private val prefsRepo: PrefsRepository = mockk()

    @Before
    fun setup() {
        // ✅ Mock android.util.Log so it doesn't crash in local unit tests
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
    }

    /**
     * Core feature: user submits a new tips question.
     */
    @Test
    fun submitNewQuestion_callsRepo_andStopsLoading() = runTest {
        val userId = 1L
        val question = "What should I eat today?"

        every { prefsRepo.getUserId() } returns flowOf(userId)
        every { tipsRepo.getThreadsForUser(userId) } returns flowOf(emptyList())

        coEvery {
            tipsRepo.createThreadWithFirstMessage(
                userId = userId,
                userQuestion = question
            )
        } returns 42L

        val viewModel = TipsViewModel(tipsRepo, prefsRepo)

        viewModel.submitNewQuestion(question)

        coVerify {
            tipsRepo.createThreadWithFirstMessage(
                userId = userId,
                userQuestion = question
            )
        }

        assertFalse(viewModel.menuUiState.value.isLoading)
    }

    @Test
    fun onInputChange_updatesConversationInputText() = runTest {
        every { prefsRepo.getUserId() } returns flowOf(1L)
        every { tipsRepo.getThreadsForUser(1L) } returns flowOf(emptyList())

        val viewModel = TipsViewModel(tipsRepo, prefsRepo)

        viewModel.onInputChange("Hello SmartFit")

        assertEquals("Hello SmartFit", viewModel.conversationUiState.value.inputText)
    }

    @Test
    fun deleteTip_callsRepositoryWithCorrectId() = runTest {
        every { prefsRepo.getUserId() } returns flowOf(1L)
        every { tipsRepo.getThreadsForUser(1L) } returns flowOf(emptyList())
        coEvery { tipsRepo.deleteThread(10L) } returns Unit

        val viewModel = TipsViewModel(tipsRepo, prefsRepo)

        viewModel.deleteTip("10")

        coVerify { tipsRepo.deleteThread(10L) }
    }
}
