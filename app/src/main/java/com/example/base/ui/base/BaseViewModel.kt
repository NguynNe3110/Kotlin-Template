package com.example.base.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.base.shared.error.AppException
import com.example.base.shared.error.DefaultErrorMapper
import com.example.base.shared.error.ErrorMapper
import com.example.base.shared.result.Result
import com.example.base.shared.retry.RetryConfig
import com.example.base.shared.retry.RetryExecutor
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PendingUiEffect(val id: Long, val effect: UiEffect)

abstract class BaseViewModel(
    protected val errorMapper: ErrorMapper = DefaultErrorMapper
) : ViewModel() {
    private val retryExecutor = RetryExecutor(errorMapper = errorMapper)
    private val loadingOperations = MutableStateFlow<Map<LoadingKey, Int>>(emptyMap())
    val loadingKeys = loadingOperations.asStateFlow()
    val isLoading = loadingOperations
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // Retain presentation effects until the UI acknowledges them, including across rotation.
    private val nextEffectId = AtomicLong()
    private val _effects = MutableStateFlow<List<PendingUiEffect>>(emptyList())
    val effects = _effects.asStateFlow()

    fun onEffectHandled(id: Long) {
        _effects.update { pending -> pending.filterNot { it.id == id } }
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) enqueue(UiEffect.ShowError(errorMapper.map(throwable)))
    }

    protected fun launchWithLoading(
        key: LoadingKey = LoadingKey.Initial,
        block: suspend CoroutineScope.() -> Unit
    ): Job = viewModelScope.launch(exceptionHandler) {
        updateLoading(key, 1)
        try {
            block()
        } finally {
            updateLoading(key, -1)
        }
    }

    protected fun <T> launchOperation(
        key: LoadingKey = LoadingKey.Initial,
        retryConfig: RetryConfig = RetryConfig(maxAttempts = 1),
        allowRetry: Boolean = false,
        showError: Boolean = true,
        action: suspend () -> Result<T>,
        onSuccess: suspend (T) -> Unit = {},
        onFailure: suspend (AppException) -> Unit = {}
    ): Job = launchWithLoading(key) {
        // Mutations must explicitly opt into replay; a network error alone is insufficient.
        val config = if (allowRetry) retryConfig else RetryConfig(maxAttempts = 1)
        when (val result = retryExecutor.execute(config, action)) {
            is Result.Success -> onSuccess(result.data)
            is Result.Failure -> {
                val error = result.exception
                onFailure(error)
                if (showError) {
                    val onRetry: (suspend () -> Unit)? = if (allowRetry && error.retryable) {
                        {
                            launchOperation(key, retryConfig, allowRetry, showError, action, onSuccess, onFailure)
                            Unit
                        }
                    } else null
                    emitError(error, onRetry)
                }
            }
        }
    }

    protected fun emitError(exception: AppException, onRetry: (suspend () -> Unit)? = null) {
        enqueue(
            if (exception is AppException.ValidationError) UiEffect.ShowDialog("Validation error", exception, onRetry)
            else UiEffect.ShowError(exception, onRetry)
        )
    }

    private fun enqueue(effect: UiEffect) {
        val pending = PendingUiEffect(nextEffectId.incrementAndGet(), effect)
        _effects.update { it + pending }
    }

    private fun updateLoading(key: LoadingKey, delta: Int) {
        loadingOperations.update { operations ->
            operations.toMutableMap().apply {
                val next = (get(key) ?: 0) + delta
                if (next > 0) put(key, next) else remove(key)
            }
        }
    }
}
