package com.example.base.domain.usecase.base

import com.example.base.shared.error.DefaultErrorMapper
import com.example.base.shared.error.ErrorMapper
import com.example.base.shared.logging.AppLogger
import com.example.base.shared.logging.NoOpLogger
import com.example.base.shared.result.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

abstract class BaseFlowUseCase<in P, out R>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val logger: AppLogger = NoOpLogger,
    private val errorMapper: ErrorMapper = DefaultErrorMapper
) {
    operator fun invoke(params: P): Flow<Result<R>> = flow { emitAll(execute(params)) }
        .map<R, Result<R>> { Result.Success(it) }
        .catch { throwable ->
            if (throwable is CancellationException) throw throwable
            logger.error("Flow use case failed", throwable)
            emit(Result.Failure(errorMapper.map(throwable)))
        }
        .flowOn(dispatcher)

    protected abstract fun execute(params: P): Flow<R>
}
