package com.example.base.ui.auth

import com.example.base.domain.model.AuthUser
import com.example.base.domain.model.SessionInfo
import com.example.base.shared.error.AppException

/**
 * UI state representing authentication inputs, submission state, profile view,
 * and change password dialog (REST-3 & REST-4).
 */
data class AuthUiState(
    // Mode: Login vs Register
    val isLoginMode: Boolean = true,

    // Form inputs
    val email: String = "",
    val password: String = "",
    val name: String = "",

    // Field-level validation errors
    val emailError: String? = null,
    val passwordError: String? = null,
    val nameError: String? = null,

    // Form submission
    val isSubmitting: Boolean = false,
    val submitError: AppException? = null,

    // Session and user profile
    val session: SessionInfo? = null,
    val user: AuthUser? = null,
    val isLoadingProfile: Boolean = false,
    val profileError: AppException? = null,

    // Change password modal dialog
    val isChangePasswordOpen: Boolean = false,
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val changePasswordError: String? = null,
    val isChangingPassword: Boolean = false,
    val isPasswordChangedSuccess: Boolean = false
) {
    val isLoggedIn: Boolean
        get() = session != null && session.tokens.accessToken.isNotBlank()
}
