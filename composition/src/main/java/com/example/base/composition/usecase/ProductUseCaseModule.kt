package com.example.base.composition.usecase

import com.example.base.domain.repository.ProductRepository
import com.example.base.domain.usecase.GetCategoriesUseCase
import com.example.base.domain.usecase.GetProductDetailUseCase
import com.example.base.domain.usecase.GetProductsUseCase
import com.example.base.shared.dispatcher.DispatcherProvider
import com.example.base.shared.error.ErrorMapper
import com.example.base.shared.logging.AppLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ProductUseCaseModule {

    @Provides
    fun provideGetProductsUseCase(
        repository: ProductRepository,
        dispatchers: DispatcherProvider,
        logger: AppLogger,
        errorMapper: ErrorMapper
    ): GetProductsUseCase = GetProductsUseCase(
        repository = repository,
        dispatcher = dispatchers.io,
        logger = logger,
        errorMapper = errorMapper
    )

    @Provides
    fun provideGetProductDetailUseCase(
        repository: ProductRepository,
        dispatchers: DispatcherProvider,
        logger: AppLogger,
        errorMapper: ErrorMapper
    ): GetProductDetailUseCase = GetProductDetailUseCase(
        repository = repository,
        dispatcher = dispatchers.io,
        logger = logger,
        errorMapper = errorMapper
    )

    @Provides
    fun provideGetCategoriesUseCase(
        repository: ProductRepository,
        dispatchers: DispatcherProvider,
        logger: AppLogger,
        errorMapper: ErrorMapper
    ): GetCategoriesUseCase = GetCategoriesUseCase(
        repository = repository,
        dispatcher = dispatchers.io,
        logger = logger,
        errorMapper = errorMapper
    )
}
