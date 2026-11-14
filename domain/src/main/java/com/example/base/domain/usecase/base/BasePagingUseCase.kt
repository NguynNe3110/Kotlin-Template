package com.example.base.domain.usecase.base

import com.example.base.shared.error.DefaultErrorMapper
import com.example.base.shared.error.ErrorMapper
import com.example.base.shared.logging.AppLogger
import com.example.base.shared.logging.NoOpLogger
import com.example.base.shared.result.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BasePagingUseCase<in P : PagingInput, out R>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val logger: AppLogger = NoOpLogger,
    private val errorMapper: ErrorMapper = DefaultErrorMapper
) {
    suspend operator fun invoke(params: P): Result<PageResult<R>> =
        withContext(dispatcher) {
            try {
                require(params.page > 0 && params.pageSize > 0) { "Page and pageSize must be positive" }
                Result.Success(execute(params))
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                logger.error("Paging use case failed", throwable)
                Result.Failure(errorMapper.map(throwable))
            }
        }

    protected abstract suspend fun execute(params: P): PageResult<R>
}

