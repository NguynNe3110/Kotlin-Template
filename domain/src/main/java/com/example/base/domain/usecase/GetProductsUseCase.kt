package com.example.base.domain.usecase

import com.example.base.domain.model.PagedResult
import com.example.base.domain.model.Product
import com.example.base.domain.model.ProductFilter
import com.example.base.domain.repository.ProductRepository
import com.example.base.domain.usecase.base.BaseInput
import com.example.base.domain.usecase.base.BaseOutput
import com.example.base.domain.usecase.base.BaseUseCase
import com.example.base.domain.usecase.base.PagingInput
import com.example.base.shared.error.AppException
import com.example.base.shared.error.DefaultErrorMapper
import com.example.base.shared.error.ErrorMapper
import com.example.base.shared.extension.getOrThrow
import com.example.base.shared.logging.AppLogger
import com.example.base.shared.logging.NoOpLogger
import com.example.base.shared.result.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

data class GetProductsInput(
    override val page: Int = 1,
    override val pageSize: Int = 10,
    val filter: ProductFilter? = null
) : BaseInput, PagingInput

data class GetProductsOutput(
    val pagedProducts: PagedResult<Product>
) : BaseOutput

class GetProductsUseCase(
    private val repository: ProductRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    logger: AppLogger = NoOpLogger,
    errorMapper: ErrorMapper = DefaultErrorMapper
) : BaseUseCase<GetProductsInput, GetProductsOutput>(dispatcher, logger, errorMapper) {

    override suspend fun execute(params: GetProductsInput): GetProductsOutput {
        if (params.page <= 0 || params.pageSize <= 0) {
            throw AppException.ValidationError(
                field = if (params.page <= 0) "page" else "pageSize",
                message = "Page and pageSize must be positive",
                code = "INVALID_PAGING"
            )
        }
        val pagedProducts = repository.getProducts(
            page = params.page,
            pageSize = params.pageSize,
            filter = params.filter
        ).getOrThrow()
        return GetProductsOutput(pagedProducts)
    }

    suspend operator fun invoke(
        page: Int = 1,
        pageSize: Int = 10,
        filter: ProductFilter? = null
    ): Result<GetProductsOutput> = invoke(GetProductsInput(page = page, pageSize = pageSize, filter = filter))
}
