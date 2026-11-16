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

data class RegisterInput(
    val email: String,
    val password: String,
    val name: String
) : BaseInput

data class RegisterOutput(
    val session: SessionInfo
) : BaseOutput

class RegisterUseCase(
    private val repository: AuthRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    logger: AppLogger = NoOpLogger,
    errorMapper: ErrorMapper = DefaultErrorMapper
) : BaseUseCase<RegisterInput, RegisterOutput>(dispatcher, logger, errorMapper) {

    override suspend fun execute(params: RegisterInput): RegisterOutput {
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
        if (params.name.isBlank()) {
            throw AppException.ValidationError(
                field = "name",
                message = "Name cannot be blank",
                code = "INVALID_NAME"
            )
        }
        val session = repository.register(params.email.trim(), params.password, params.name.trim()).getOrThrow()
        return RegisterOutput(session)
    }

    suspend operator fun invoke(email: String, password: String, name: String): Result<RegisterOutput> =
        invoke(RegisterInput(email, password, name))
}
