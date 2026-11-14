package com.example.base.ui.base

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.base.navigation.AppNavigator
import com.example.base.navigation.NoOpAppNavigator
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun BaseScreen(
    viewModel: BaseViewModel,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    navigator: AppNavigator = remember { NoOpAppNavigator() },
    effectHandler: AppEffectHandler? = null,
    content: @Composable () -> Unit
) {
    val loadingKeys by viewModel.loadingKeys.collectAsStateWithLifecycle()
    val pendingEffects by viewModel.effects.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val defaultEffectHandler = remember { DefaultAppEffectHandler() }
    val resolvedEffectHandler = effectHandler ?: defaultEffectHandler

    LaunchedEffect(viewModel, resolvedEffectHandler, navigator, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.map { it.firstOrNull() }.distinctUntilChanged().filterNotNull().collect { pending ->
                if (pending.effect !is UiEffect.ShowDialog) {
                    resolvedEffectHandler.handle(pending.effect, snackbarHostState, navigator)
                    viewModel.onEffectHandled(pending.id)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            content()
            if (LoadingKey.Initial in loadingKeys || LoadingKey.Submit in loadingKeys) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            val pending = pendingEffects.firstOrNull()
            val dialog = pending?.effect as? UiEffect.ShowDialog
            if (dialog != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.onEffectHandled(pending.id) },
                    title = { Text(dialog.title) },
                    text = { Text(dialog.exception.message) },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.onEffectHandled(pending.id)
                            coroutineScope.launch { dialog.onRetry?.invoke() }
                        }) { Text("OK") }
                    }
                )
            }
        }
    }
}
