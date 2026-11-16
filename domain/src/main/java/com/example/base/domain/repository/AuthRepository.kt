package com.example.base.domain.repository

import com.example.base.domain.model.AuthUser
import com.example.base.domain.model.SessionInfo
import com.example.base.domain.model.TokenPair
import com.example.base.shared.result.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<SessionInfo>
    suspend fun register(email: String, password: String, name: String): Result<SessionInfo>
    suspend fun refreshToken(refreshToken: String): Result<TokenPair>
    suspend fun getMe(): Result<AuthUser>
    suspend fun logout(): Result<Unit>
    suspend fun logoutAll(): Result<Unit>
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
    fun getSession(): Flow<SessionInfo?>
}
