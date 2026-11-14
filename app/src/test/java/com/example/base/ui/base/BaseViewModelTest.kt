package com.example.base.ui.base

import androidx.lifecycle.ViewModelStore
import com.example.base.shared.error.AppException
import com.example.base.shared.result.Result
import com.example.base.shared.retry.RetryConfig
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {
    private val store = ViewModelStore()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    private fun model() =
        TestViewModel().also { store.put("model", it) }

    @Test
    fun retainsOneErrorUntilAcknowledgedWithoutAnActiveScreen() = runTest {
        val model = model()
        model.fail(allowRetry = true)
        advanceUntilIdle()
        val pending = model.effects.value.single()
        assertIs<UiEffect.ShowError>(pending.effect)
        assertEquals(1, model.failureCallbacks)
        model.onEffectHandled(pending.id)
        assertTrue(model.effects.value.isEmpty())
    }

    @Test
    fun mutationDoesNotReplayOrExposeRetryEvenWhenErrorIsRetryable() = runTest {
        val model = model()
        model.fail()
        advanceUntilIdle()
        assertEquals(1, model.actionCalls)
        assertNull(assertIs<UiEffect.ShowError>(model.effects.value.single().effect).onRetry)
    }

    @Test
    fun readRetryReexecutesOnceAndPublishesOneNewError() = runTest {
        val model = model()
        model.fail(allowRetry = true, attempts = 1)
        advanceUntilIdle()
        val pending = model.effects.value.single()
        val error = assertIs<UiEffect.ShowError>(pending.effect)
        model.onEffectHandled(pending.id)
        requireNotNull(error.onRetry).invoke()
        advanceUntilIdle()
        assertEquals(2, model.actionCalls)
        assertEquals(1, model.effects.value.size)
    }

    @Test
    fun loadingCountsOverlappingJobsAndCleansUpOnCancellation() = runTest {
        val model = model()
        val gate = CompletableDeferred<Unit>()
        val first = model.waitFor(gate)
        val second = model.waitFor(gate)
        advanceUntilIdle()
        assertEquals(2, model.loadingKeys.value[LoadingKey.Submit])
        first.cancel()
        advanceUntilIdle()
        assertEquals(1, model.loadingKeys.value[LoadingKey.Submit])
        second.cancel()
        advanceUntilIdle()
        assertTrue(model.loadingKeys.value.isEmpty())
        assertTrue(model.effects.value.isEmpty())
    }

    private class TestViewModel : BaseViewModel() {
        var actionCalls = 0
        var failureCallbacks = 0

        fun fail(
            allowRetry: Boolean = false,
            attempts: Int = 3,
            exception: AppException = AppException.NetworkError()
        ): Job = launchOperation<Unit>(
            retryConfig = RetryConfig(maxAttempts = attempts),
            allowRetry = allowRetry,
            action = { actionCalls++; Result.Failure(exception) },
            onFailure = { failureCallbacks++ }
        )

        fun waitFor(gate: CompletableDeferred<Unit>): Job = launchWithLoading(LoadingKey.Submit) { gate.await() }
    }
}
