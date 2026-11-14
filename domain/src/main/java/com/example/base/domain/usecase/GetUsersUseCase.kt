package com.example.base.domain.usecase

import com.example.base.domain.model.User
import com.example.base.domain.repository.UserRepository
import com.example.base.domain.usecase.base.BasePagingUseCase
import com.example.base.domain.usecase.base.PageResult
import com.example.base.domain.usecase.base.PagingInput
import com.example.base.shared.extension.getOrThrow
import com.example.base.shared.error.ErrorMapper
import com.example.base.shared.logging.AppLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

data class GetUsersInput(
    override val page: Int = 1,
    override val pageSize: Int = 10
) : PagingInput

class GetUsersUseCase(
    private val repository: UserRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    logger: AppLogger,
    errorMapper: ErrorMapper
) : BasePagingUseCase<GetUsersInput, User>(dispatcher, logger, errorMapper) {
    override suspend fun execute(params: GetUsersInput): PageResult<User> =
        repository.getUsers(params.page, params.pageSize).getOrThrow()
}

