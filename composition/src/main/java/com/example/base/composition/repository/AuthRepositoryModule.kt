package com.example.base.composition.repository

import com.example.base.data.api.practice.PracticeDataErrorMapper
import com.example.base.data.api.practice.auth.PracticeAuthApi
import com.example.base.data.api.practice.auth.TokenStorage
import com.example.base.data.repository.AuthRepositoryImpl
import com.example.base.domain.repository.AuthRepository
import com.example.base.shared.logging.AppLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthRepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepositoryImpl(
        api: PracticeAuthApi,
        tokenStorage: TokenStorage,
        errorMapper: PracticeDataErrorMapper,
        logger: AppLogger
    ): AuthRepositoryImpl = AuthRepositoryImpl(
        api = api,
        tokenStorage = tokenStorage,
        errorMapper = errorMapper,
        logger = logger
    )

    @Provides
    @Singleton
    fun provideAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository = impl
}
