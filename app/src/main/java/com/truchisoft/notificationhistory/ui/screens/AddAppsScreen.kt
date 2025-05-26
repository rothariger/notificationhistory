package com.truchisoft.notificationhistory.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.truchisoft.notificationhistory.R
import com.truchisoft.notificationhistory.ui.viewmodels.AddAppsViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppsScreen(
    onBackClick: () -> Unit,
    viewModel: AddAppsViewModel = hiltViewModel()
) {
    val installedApps by viewModel.filteredApps.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }

    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            if (isSearchActive) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    onSearch = { viewModel.setSearchQuery(it) },
                    active = true,
                    onActiveChange = { isSearchActive = it },
                    placeholder = { Text(stringResource(R.string.search_apps)) },
                    leadingIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            viewModel.setSearchQuery("") // Limpiar búsqueda al salir
                        }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = stringResource(R.string.clear)
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Contenido del SearchBar cuando está activo
                    if (isLoading && !isRefreshing) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (installedApps.isEmpty() && searchQuery.isNotEmpty()) {
                        Text(
                            text = "No hay resultados para \"$searchQuery\"",
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(installedApps) { appInfo ->
                                ListItem(
                                    headlineContent = { Text(appInfo.appName) },
                                    supportingContent = { Text(appInfo.packageName) },
                                    trailingContent = {
                                        Switch(
                                            checked = appInfo.isTracked,
                                            onCheckedChange = {
                                                viewModel.toggleAppTracking(appInfo.packageName, it)
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                TopAppBar(
                    title = { Text(stringResource(R.string.add_apps)) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.refreshData() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = stringResource(R.string.search)
                            )
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        // Contenido principal cuando el SearchBar no está activo
        if (!isSearchActive) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refreshData() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                state = pullToRefreshState
            ) {
                if (isLoading && !isRefreshing) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(installedApps) { appInfo ->
                            ListItem(
                                headlineContent = { Text(appInfo.appName) },
                                supportingContent = { Text(appInfo.packageName) },
                                trailingContent = {
                                    Switch(
                                        checked = appInfo.isTracked,
                                        onCheckedChange = {
                                            viewModel.toggleAppTracking(appInfo.packageName, it)
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}