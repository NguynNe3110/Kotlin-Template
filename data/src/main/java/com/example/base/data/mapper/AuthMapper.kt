package com.example.base.data.mapper

import com.example.base.data.api.practice.auth.AuthDataDto
import com.example.base.data.api.practice.auth.TokensDto
import com.example.base.data.api.practice.auth.UserDto
import com.example.base.domain.model.AuthUser
import com.example.base.domain.model.SessionInfo
import com.example.base.domain.model.TokenPair

object AuthMapper {

    fun mapUser(dto: UserDto): AuthUser = AuthUser(
        id = dto.id,
        email = dto.email,
        name = dto.name,
        role = dto.role
    )

    fun mapTokens(dto: TokensDto): TokenPair = TokenPair(
        accessToken = dto.accessToken,
        refreshToken = dto.refreshToken,
        tokenType = dto.tokenType,
        expiresIn = dto.expiresIn,
        refreshExpiresIn = dto.refreshExpiresIn
    )

    fun mapSession(dto: AuthDataDto): SessionInfo = SessionInfo(
        user = mapUser(dto.user),
        tokens = mapTokens(dto.tokens)
    )
}
