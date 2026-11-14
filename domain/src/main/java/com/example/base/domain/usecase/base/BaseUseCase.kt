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

/**
 * BaseUseCase trừu tượng hóa việc thực thi logic nghiệp vụ.
 * Cung cấp operator `invoke` giúp gọi UseCase trực tiếp như một hàm.
 * Tự động chuyển Coroutine Context sang [dispatcher] (mặc định là Dispatchers.IO).
 */
abstract class BaseUseCase<in P, out R>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val logger: AppLogger = NoOpLogger,
    private val errorMapper: ErrorMapper = DefaultErrorMapper
) {
    suspend operator fun invoke(params: P): Result<R> {
        logger.debug("UseCase started", mapOf("useCase" to this@BaseUseCase::class.simpleName))
        return withContext(dispatcher) {
            try {
                Result.Success(execute(params)).also {
                    logger.debug("UseCase succeeded", mapOf("useCase" to this@BaseUseCase::class.simpleName))
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                logger.error("UseCase failed", throwable)
                Result.Failure(errorMapper.map(throwable))
            }
        }
    }

    protected abstract suspend fun execute(params: P): R
}

/**
 * BaseUseCase không yêu cầu tham số truyền vào (NoParams).
 */
abstract class BaseNoParamsUseCase<R>(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    logger: AppLogger = NoOpLogger,
    errorMapper: ErrorMapper = DefaultErrorMapper
) : BaseUseCase<NoParams, R>(dispatcher, logger, errorMapper) {

    suspend operator fun invoke() = invoke(NoParams)
}
