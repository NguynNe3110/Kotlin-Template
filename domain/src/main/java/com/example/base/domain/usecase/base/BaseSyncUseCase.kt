package com.example.base.domain.usecase.base

import com.example.base.shared.error.DefaultErrorMapper
import com.example.base.shared.error.ErrorMapper
import com.example.base.shared.logging.AppLogger
import com.example.base.shared.logging.NoOpLogger
import com.example.base.shared.result.Result
import kotlinx.coroutines.CancellationException

abstract class BaseSyncUseCase<in P, out R>(
    private val logger: AppLogger = NoOpLogger,
    private val errorMapper: ErrorMapper = DefaultErrorMapper
) {
    operator fun invoke(params: P): Result<R> {
        return try {
            Result.Success(execute(params))
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            logger.error("Sync use case failed", throwable)
            Result.Failure(errorMapper.map(throwable))
        }
    }

    protected abstract fun execute(params: P): R
}
