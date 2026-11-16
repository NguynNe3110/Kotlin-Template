package com.example.base.domain.usecase.auth

import com.example.base.domain.model.SessionInfo
import com.example.base.domain.repository.AuthRepository
import com.example.base.domain.usecase.base.BaseInput
import com.example.base.domain.usecase.base.BaseOutput
import com.example.base.domain.usecase.base.BaseUseCase
import com.example.base.shared.error.AppException
import com.example.base.shared.error.DefaultErrorMapper
import com.example.base.shared.error.ErrorMapper
import com.example.base.shared.extension.getOrThrow
import com.example.base.shared.logging.AppLogger
import com.example.base.shared.logging.NoOpLogger
import com.example.base.shared.result.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

data class LoginInput(
    val email: String,
    val password: String
) : BaseInput

data class LoginOutput(
    val session: SessionInfo
) : BaseOutput

class LoginUseCase(
    private val repository: AuthRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    logger: AppLogger = NoOpLogger,
    errorMapper: ErrorMapper = DefaultErrorMapper
) : BaseUseCase<LoginInput, LoginOutput>(dispatcher, logger, errorMapper) {

    override suspend fun execute(params: LoginInput): LoginOutput {
        if (params.email.isBlank()) {
            throw AppException.ValidationError(
                field = "email",
                message = "Email cannot be blank",
                code = "INVALID_EMAIL"
            )
        }
        if (params.password.isBlank()) {
            throw AppException.ValidationError(
                field = "password",
                message = "Password cannot be blank",
                code = "INVALID_PASSWORD"
            )
        }
        val session = repository.login(params.email.trim(), params.password).getOrThrow()
        return LoginOutput(session)
    }

    suspend operator fun invoke(email: String, password: String): Result<LoginOutput> =
        invoke(LoginInput(email, password))
}
