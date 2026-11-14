package com.example.base.domain

import com.example.base.domain.usecase.base.BaseSyncUseCase
import com.example.base.domain.usecase.base.BaseUseCase
import com.example.base.domain.usecase.base.BaseFlowUseCase
import com.example.base.shared.result.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class UseCaseContractsTest {
    @Test
    fun syncUseCaseReturnsFailureForThrownError() {
        val useCase = object : BaseSyncUseCase<Unit, String>() {
            override fun execute(params: Unit): String = error("boom")
        }

        val result = useCase(Unit)
        assertEquals(true, result is Result.Failure)
    }

    @Test
    fun suspendUseCasePropagatesCancellation() = runTest {
        val useCase = object : BaseUseCase<Unit, String>(StandardTestDispatcher(testScheduler)) {
            override suspend fun execute(params: Unit): String = throw CancellationException("cancelled")
        }
        assertFailsWith<CancellationException> { useCase(Unit) }
    }

    @Test
    fun flowConstructionErrorsAreMappedDuringCollection() = runTest {
        val useCase = object : BaseFlowUseCase<Unit, String>(StandardTestDispatcher(testScheduler)) {
            override fun execute(params: Unit): Flow<String> = error("failed before flow was created")
        }
        assertIs<Result.Failure>(useCase(Unit).single())
    }
}
