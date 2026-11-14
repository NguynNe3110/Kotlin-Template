package com.example.base.domain.usecase

import com.example.base.domain.model.Todo
import com.example.base.domain.repository.TodoRepository
import com.example.base.domain.usecase.base.BaseUseCase
import com.example.base.shared.extension.getOrThrow
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class ToggleTodoUseCase(
    private val todoRepository: TodoRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseUseCase<String, Todo>(dispatcher) {

    override suspend fun execute(params: String): Todo {
        return todoRepository.toggleTodo(params).getOrThrow()
    }
}
