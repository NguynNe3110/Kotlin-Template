package com.example.base.domain.usecase

import com.example.base.domain.model.Todo
import com.example.base.domain.repository.TodoRepository
import com.example.base.domain.usecase.base.BaseNoParamsUseCase
import com.example.base.domain.usecase.base.NoParams
import com.example.base.shared.extension.getOrThrow
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class GetTodosUseCase(
    private val todoRepository: TodoRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseNoParamsUseCase<List<Todo>>(dispatcher) {

    override suspend fun execute(params: NoParams): List<Todo> {
        return todoRepository.getTodos().getOrThrow()
    }
}
