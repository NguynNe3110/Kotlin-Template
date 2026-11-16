package com.example.base.domain.usecase.auth

import com.example.base.domain.repository.AuthRepository
import com.example.base.domain.usecase.base.BaseInput
import com.example.base.domain.usecase.base.BaseOutput
import com.example.base.domain.usecase.base.BaseUseCase
import com.example.base.shared.error.DefaultErrorMapper
import com.example.base.shared.error.ErrorMapper
import com.example.base.shared.extension.getOrThrow
import com.example.base.shared.logging.AppLogger
import com.example.base.shared.logging.NoOpLogger
import com.example.base.shared.result.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

data class LogoutInput(
    val allDevices: Boolean = false
) : BaseInput

data class LogoutOutput(
    val success: Boolean = true
) : BaseOutput

class LogoutUseCase(
    private val repository: AuthRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    logger: AppLogger = NoOpLogger,
    errorMapper: ErrorMapper = DefaultErrorMapper
) : BaseUseCase<LogoutInput, LogoutOutput>(dispatcher, logger, errorMapper) {

    override suspend fun execute(params: LogoutInput): LogoutOutput {
        if (params.allDevices) {
            repository.logoutAll().getOrThrow()
        } else {
            repository.logout().getOrThrow()
        }
        return LogoutOutput(success = true)
    }

    suspend operator fun invoke(allDevices: Boolean = false): Result<LogoutOutput> =
        invoke(LogoutInput(allDevices))
}
