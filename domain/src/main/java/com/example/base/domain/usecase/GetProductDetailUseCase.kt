package com.example.base.domain.usecase

import com.example.base.domain.model.Product
import com.example.base.domain.repository.ProductRepository
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

data class GetProductDetailInput(val id: String) : BaseInput

data class GetProductDetailOutput(val product: Product) : BaseOutput

class GetProductDetailUseCase(
    private val repository: ProductRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    logger: AppLogger = NoOpLogger,
    errorMapper: ErrorMapper = DefaultErrorMapper
) : BaseUseCase<GetProductDetailInput, GetProductDetailOutput>(dispatcher, logger, errorMapper) {

    override suspend fun execute(params: GetProductDetailInput): GetProductDetailOutput {
        if (params.id.isBlank()) {
            throw AppException.ValidationError(
                field = "id",
                message = "Product ID cannot be blank",
                code = "BLANK_ID"
            )
        }
        val product = repository.getProductById(params.id).getOrThrow()
        return GetProductDetailOutput(product)
    }

    suspend operator fun invoke(id: String): Result<GetProductDetailOutput> = invoke(GetProductDetailInput(id))
}
