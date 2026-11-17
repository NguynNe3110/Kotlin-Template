package com.example.base.composition.usecase

import com.example.base.domain.repository.AuthRepository
import com.example.base.domain.usecase.auth.ChangePasswordUseCase
import com.example.base.domain.usecase.auth.GetMeUseCase
import com.example.base.domain.usecase.auth.LoginUseCase
import com.example.base.domain.usecase.auth.LogoutUseCase
import com.example.base.domain.usecase.auth.RegisterUseCase
import com.example.base.shared.dispatcher.DispatcherProvider
import com.example.base.shared.error.ErrorMapper
import com.example.base.shared.logging.AppLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AuthUseCaseModule {

    @Provides
    fun provideLoginUseCase(
        repository: AuthRepository,
        dispatchers: DispatcherProvider,
        logger: AppLogger,
        errorMapper: ErrorMapper
    ): LoginUseCase = LoginUseCase(
        repository = repository,
        dispatcher = dispatchers.io,
        logger = logger,
        errorMapper = errorMapper
    )

    @Provides
    fun provideRegisterUseCase(
        repository: AuthRepository,
        dispatchers: DispatcherProvider,
        logger: AppLogger,
        errorMapper: ErrorMapper
    ): RegisterUseCase = RegisterUseCase(
        repository = repository,
        dispatcher = dispatchers.io,
        logger = logger,
        errorMapper = errorMapper
    )

    @Provides
    fun provideGetMeUseCase(
        repository: AuthRepository,
        dispatchers: DispatcherProvider,
        logger: AppLogger,
        errorMapper: ErrorMapper
    ): GetMeUseCase = GetMeUseCase(
        repository = repository,
        dispatcher = dispatchers.io,
        logger = logger,
        errorMapper = errorMapper
    )

    @Provides
    fun provideLogoutUseCase(
        repository: AuthRepository,
        dispatchers: DispatcherProvider,
        logger: AppLogger,
        errorMapper: ErrorMapper
    ): LogoutUseCase = LogoutUseCase(
        repository = repository,
        dispatcher = dispatchers.io,
        logger = logger,
        errorMapper = errorMapper
    )

    @Provides
    fun provideChangePasswordUseCase(
        repository: AuthRepository,
        dispatchers: DispatcherProvider,
        logger: AppLogger,
        errorMapper: ErrorMapper
    ): ChangePasswordUseCase = ChangePasswordUseCase(
        repository = repository,
        dispatcher = dispatchers.io,
        logger = logger,
        errorMapper = errorMapper
    )
}
