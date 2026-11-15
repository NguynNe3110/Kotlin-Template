package com.example.base.shared.flow

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Handle for a sequential action without parameters.
 */
interface SequentialAction : () -> Unit {
    /**
     * Closes the underlying queue and cancels the processor job.
     */
    fun close()
}

/**
 * Handle for a sequential action with parameter of type [T].
 */
interface SequentialHandler<in T> : (T) -> Unit {
    /**
     * Closes the underlying queue and cancels the processor job.
     */
    fun close()
}

/**
 * Creates a sequential callback wrapper that buffers invocations and executes them
 * strictly sequentially in FIFO order on [scope].
 *
 * Buffer capacity and overflow policy can be declared explicitly.
 */
fun sequential(
    scope: CoroutineScope,
    capacity: Int = Channel.UNLIMITED,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
    onError: ((Throwable) -> Unit)? = null,
    action: suspend () -> Unit
): SequentialAction {
    val channel = Channel<Unit>(capacity, onBufferOverflow)
    val job = scope.launch {
        try {
            for (item in channel) {
                try {
                    action()
                } catch (throwable: Throwable) {
                    if (throwable is CancellationException) throw throwable
                    if (onError != null) onError(throwable) else throw throwable
                }
            }
        } finally {
            channel.close()
        }
    }
    return object : SequentialAction {
        override fun invoke() {
            channel.trySend(Unit)
        }

        override fun close() {
            channel.close()
            job.cancel()
        }
    }
}

/**
 * Parameterized overload of [sequential].
 */
fun <T> sequential(
    scope: CoroutineScope,
    capacity: Int = Channel.UNLIMITED,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
    onError: ((Throwable) -> Unit)? = null,
    action: suspend (T) -> Unit
): SequentialHandler<T> {
    val channel = Channel<T>(capacity, onBufferOverflow)
    val job = scope.launch {
        try {
            for (item in channel) {
                try {
                    action(item)
                } catch (throwable: Throwable) {
                    if (throwable is CancellationException) throw throwable
                    if (onError != null) onError(throwable) else throw throwable
                }
            }
        } finally {
            channel.close()
        }
    }
    return object : SequentialHandler<T> {
        override fun invoke(param: T) {
            channel.trySend(param)
        }

        override fun close() {
            channel.close()
            job.cancel()
        }
    }
}

/**
 * Processes elements from this [Flow] sequentially using [action] on [scope].
 */
fun <T> Flow<T>.sequential(
    scope: CoroutineScope,
    action: suspend (T) -> Unit
): Job = scope.launch {
    collect { value ->
        action(value)
    }
}
