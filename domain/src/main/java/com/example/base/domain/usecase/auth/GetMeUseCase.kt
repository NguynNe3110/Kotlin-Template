package com.example.base.domain.usecase.auth

import com.example.base.domain.model.AuthUser
import com.example.base.domain.repository.AuthRepository
import com.example.base.domain.usecase.base.BaseNoInputUseCase
import com.example.base.domain.usecase.base.BaseOutput
import com.example.base.domain.usecase.base.NoInput
import com.example.base.shared.error.DefaultErrorMapper
import com.example.base.shared.error.ErrorMapper
import com.example.base.shared.extension.getOrThrow
import com.example.base.shared.logging.AppLogger
import com.example.base.shared.logging.NoOpLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

data class GetMeOutput(
    val user: AuthUser
) : BaseOutput

class GetMeUseCase(
    private val repository: AuthRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    logger: AppLogger = NoOpLogger,
    errorMapper: ErrorMapper = DefaultErrorMapper
) : BaseNoInputUseCase<GetMeOutput>(dispatcher, logger, errorMapper) {

    override suspend fun execute(params: NoInput): GetMeOutput {
        val user = repository.getMe().getOrThrow()
        return GetMeOutput(user)
    }
}
