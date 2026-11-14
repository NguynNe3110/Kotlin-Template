package com.example.base.ui.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.base.domain.model.User
import com.example.base.navigation.AppNavigator
import com.example.base.navigation.AppRoute

@Composable
fun UsersScreen(
    viewModel: UsersViewModel,
    modifier: Modifier = Modifier,
    navigator: AppNavigator
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (val current = state) {
        UsersUiState.Loading -> Text("Loading users...", modifier.padding(16.dp))
        is UsersUiState.Error -> Column(modifier.padding(16.dp)) {
            Text(current.error.message, color = MaterialTheme.colorScheme.error)
            Button(onClick = viewModel::loadUsers) { Text("Retry") }
        }
        is UsersUiState.Success -> Column(modifier.fillMaxSize()) {
            Button(
                onClick = { navigator.navigate(AppRoute.Todo) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Open Todo sample")
            }
            UsersList(current.users, Modifier.weight(1f))
            if (current.hasNext) {
                Button(
                    onClick = viewModel::loadNextPage,
                    enabled = !current.isLoadingMore,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(if (current.isLoadingMore) "Loading..." else "Load more")
                }
            }
        }
    }
}

@Composable
private fun UsersList(users: List<User>, modifier: Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(users, key = { it.id }) { user ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(user.name, style = MaterialTheme.typography.titleMedium)
                    Text("@${user.username}", style = MaterialTheme.typography.bodyMedium)
                    Text(user.email, style = MaterialTheme.typography.bodyMedium)
                    user.phone?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
    }
}
