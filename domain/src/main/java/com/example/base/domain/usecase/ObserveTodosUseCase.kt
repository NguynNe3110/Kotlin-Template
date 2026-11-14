package com.example.base.domain.usecase

import com.example.base.domain.model.Todo
import com.example.base.domain.repository.TodoRepository
import com.example.base.domain.usecase.base.BaseFlowUseCase
import com.example.base.domain.usecase.base.NoParams
import com.example.base.shared.error.ErrorMapper
import com.example.base.shared.logging.AppLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow

class ObserveTodosUseCase(
    private val repository: TodoRepository,
    dispatcher: CoroutineDispatcher,
    logger: AppLogger,
    errorMapper: ErrorMapper
) : BaseFlowUseCase<NoParams, List<Todo>>(dispatcher, logger, errorMapper) {
    override fun execute(params: NoParams): Flow<List<Todo>> = repository.getTodosFlow()
}
