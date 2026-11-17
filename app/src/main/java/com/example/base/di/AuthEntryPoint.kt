package com.example.base.di

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
    fun authRepository(): AuthRepository
    fun loginUseCase(): LoginUseCase
    fun registerUseCase(): RegisterUseCase
    fun getMeUseCase(): GetMeUseCase
    fun logoutUseCase(): LogoutUseCase
    fun changePasswordUseCase(): ChangePasswordUseCase
}
