package com.example.base.ui.base

import androidx.compose.material3.SnackbarHostState
import com.example.base.navigation.AppNavigator
import com.example.base.shared.error.AppException

fun interface ErrorMessageMapper {
    fun map(exception: AppException): String
}

interface AppEffectHandler {
    suspend fun handle(
        effect: UiEffect,
        snackbarHostState: SnackbarHostState,
        navigator: AppNavigator
    )
}

class DefaultAppEffectHandler(
    private val messageMapper: ErrorMessageMapper = ErrorMessageMapper { it.message }
) : AppEffectHandler {
    override suspend fun handle(
        effect: UiEffect,
        snackbarHostState: SnackbarHostState,
        navigator: AppNavigator
    ) {
        when (effect) {
            is UiEffect.ShowError -> {
                val action = if (effect.onRetry == null) null else "Retry"
                val result = snackbarHostState.showSnackbar(
                    message = messageMapper.map(effect.exception),
                    actionLabel = action
                )
                if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                    effect.onRetry?.invoke()
                }
            }
            is UiEffect.ShowDialog -> Unit
            is UiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            is UiEffect.Navigate -> navigator.navigate(effect.route)
        }
    }
}
