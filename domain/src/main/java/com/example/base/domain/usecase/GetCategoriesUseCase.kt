package com.example.base.domain.usecase

import com.example.base.domain.model.Category
import com.example.base.domain.repository.ProductRepository
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

data class GetCategoriesOutput(
    val categories: List<Category>
) : BaseOutput, List<Category> by categories

class GetCategoriesUseCase(
    private val repository: ProductRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    logger: AppLogger = NoOpLogger,
    errorMapper: ErrorMapper = DefaultErrorMapper
) : BaseNoInputUseCase<GetCategoriesOutput>(dispatcher, logger, errorMapper) {

    override suspend fun execute(params: NoInput): GetCategoriesOutput {
        val categories = repository.getCategories().getOrThrow()
        return GetCategoriesOutput(categories)
    }
}
