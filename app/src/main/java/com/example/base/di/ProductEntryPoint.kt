package com.example.base.di

import com.example.base.domain.repository.ProductRepository
import com.example.base.domain.usecase.GetCategoriesUseCase
import com.example.base.domain.usecase.GetProductDetailUseCase
import com.example.base.domain.usecase.GetProductsUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt EntryPoint for retrieving Product dependencies from [SingletonComponent].
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ProductEntryPoint {
    fun productRepository(): ProductRepository
    fun getProductsUseCase(): GetProductsUseCase
    fun getProductDetailUseCase(): GetProductDetailUseCase
    fun getCategoriesUseCase(): GetCategoriesUseCase
}
