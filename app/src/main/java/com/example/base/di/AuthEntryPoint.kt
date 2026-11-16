package com.example.base.di

import com.example.base.data.api.practice.auth.AuthInterceptor
import com.example.base.data.api.practice.auth.PracticeAuthApi
import com.example.base.data.api.practice.auth.TokenStorage
import com.example.base.data.repository.AuthRepositoryImpl
import com.example.base.domain.repository.AuthRepository
import com.example.base.domain.usecase.auth.ChangePasswordUseCase
import com.example.base.domain.usecase.auth.GetMeUseCase
import com.example.base.domain.usecase.auth.LoginUseCase
import com.example.base.domain.usecase.auth.LogoutUseCase
import com.example.base.domain.usecase.auth.RegisterUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt EntryPoint for retrieving Auth dependencies from [SingletonComponent].
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface AuthEntryPoint {
    fun tokenStorage(): TokenStorage
    fun authInterceptor(): AuthInterceptor
    fun practiceAuthApi(): PracticeAuthApi
    fun authRepository(): AuthRepository
    fun authRepositoryImpl(): AuthRepositoryImpl
    fun loginUseCase(): LoginUseCase
    fun registerUseCase(): RegisterUseCase
    fun getMeUseCase(): GetMeUseCase
    fun logoutUseCase(): LogoutUseCase
    fun changePasswordUseCase(): ChangePasswordUseCase
}
