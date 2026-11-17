package com.example.base.data.repository

import com.example.base.data.api.practice.PracticeDataErrorMapper
import com.example.base.data.api.practice.auth.ChangePasswordRequestDto
import com.example.base.data.api.practice.auth.LoginRequestDto
import com.example.base.data.api.practice.auth.PracticeAuthApi
import com.example.base.data.api.practice.auth.RefreshRequestDto
import com.example.base.data.api.practice.auth.RegisterRequestDto
import com.example.base.data.api.practice.auth.TokenStorage
import com.example.base.data.api.requireBody
import com.example.base.data.mapper.AuthMapper
import com.example.base.domain.model.AuthUser
import com.example.base.domain.model.SessionInfo
import com.example.base.domain.model.TokenPair
import com.example.base.domain.repository.AuthRepository
import com.example.base.shared.logging.AppLogger
import com.example.base.shared.logging.NoOpLogger
import com.example.base.shared.result.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

/**
 * Implementation of [AuthRepository] communicating with Practice REST API.
 * Uses [TokenStorage] to maintain session state and handles 204 No-Content safely.
 */
class AuthRepositoryImpl(
    private val api: PracticeAuthApi,
    private val tokenStorage: TokenStorage,
    private val errorMapper: PracticeDataErrorMapper = PracticeDataErrorMapper(),
    private val logger: AppLogger = NoOpLogger
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<SessionInfo> {
        logger.info("Executing login")
        return try {
            val response = api.login(LoginRequestDto(email = email, password = password))
                .requireBody("POST /auth/login")
            val session = AuthMapper.mapSession(response.data)
            tokenStorage.saveSession(session)
            Result.Success(session)
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            logger.error("Login failed")
            Result.Failure(errorMapper.map(throwable))
        }
    }

    override suspend fun register(email: String, password: String, name: String): Result<SessionInfo> {
        logger.info("Executing registration")
        return try {
            val response = api.register(RegisterRequestDto(email = email, password = password, name = name))
                .requireBody("POST /auth/register")
            val session = AuthMapper.mapSession(response.data)
            tokenStorage.saveSession(session)
            Result.Success(session)
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            logger.error("Registration failed")
            Result.Failure(errorMapper.map(throwable))
        }
    }

    override suspend fun refreshToken(refreshToken: String): Result<TokenPair> {
        logger.info("Executing token refresh")
        return try {
            val response = api.refresh(RefreshRequestDto(refreshToken = refreshToken))
                .requireBody("POST /auth/refresh")
            val tokenPair = AuthMapper.mapTokens(response.data.tokens)
            tokenStorage.updateTokens(tokenPair)
            Result.Success(tokenPair)
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            logger.error("Token refresh failed")
            Result.Failure(errorMapper.map(throwable))
        }
    }

    override suspend fun getMe(): Result<AuthUser> {
        logger.info("Executing getMe")
        return try {
            val response = api.getMe().requireBody("GET /auth/me")
            val user = AuthMapper.mapUser(response.data.user)
            val currentSession = tokenStorage.getSession()
            if (currentSession != null) {
                tokenStorage.saveSession(currentSession.copy(user = user))
            }
            Result.Success(user)
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            logger.error("getMe failed")
            Result.Failure(errorMapper.map(throwable))
        }
    }

    override suspend fun logout(): Result<Unit> {
        logger.info("Executing logout")
        return try {
            val response = api.logout()
            if (!response.isSuccessful) {
                throw HttpException(response)
            }
            tokenStorage.clearSession()
            Result.Success(Unit)
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            tokenStorage.clearSession()
            logger.error("Logout failed on server, session cleared locally")
            Result.Failure(errorMapper.map(throwable))
        }
    }

    override suspend fun logoutAll(): Result<Unit> {
        logger.info("Executing logoutAll")
        return try {
            val response = api.logoutAll()
            if (!response.isSuccessful) {
                throw HttpException(response)
            }
            tokenStorage.clearSession()
            Result.Success(Unit)
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            tokenStorage.clearSession()
            logger.error("LogoutAll failed on server, session cleared locally")
            Result.Failure(errorMapper.map(throwable))
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        logger.info("Executing changePassword")
        return try {
            val response = api.changePassword(
                ChangePasswordRequestDto(currentPassword = currentPassword, newPassword = newPassword)
            )
            if (!response.isSuccessful) {
                throw HttpException(response)
            }
            Result.Success(Unit)
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            logger.error("Change password failed")
            Result.Failure(errorMapper.map(throwable))
        }
    }

    override fun getSession(): Flow<SessionInfo?> = tokenStorage.sessionFlow
}
