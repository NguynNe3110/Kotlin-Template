package com.example.base.composition.repository

import com.example.base.data.api.practice.PracticeDataErrorMapper
import com.example.base.data.api.practice.PracticeProductApi
import com.example.base.data.repository.ProductRepositoryImpl
import com.example.base.domain.repository.ProductRepository
import com.example.base.shared.logging.AppLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProductRepositoryModule {

    @Provides
    @Singleton
    fun provideProductRepositoryImpl(
        api: PracticeProductApi,
        errorMapper: PracticeDataErrorMapper,
        logger: AppLogger
    ): ProductRepositoryImpl = ProductRepositoryImpl(
        api = api,
        errorMapper = errorMapper,
        logger = logger
    )

    @Provides
    @Singleton
    fun provideProductRepository(
        impl: ProductRepositoryImpl
    ): ProductRepository = impl
}
