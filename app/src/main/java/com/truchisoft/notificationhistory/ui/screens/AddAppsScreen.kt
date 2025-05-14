package com.truchisoft.notificationhistory.ui.screens

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.truchisoft.notificationhistory.data.database.entities.AppEntity
import com.truchisoft.notificationhistory.ui.viewmodels.AddAppsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppsScreen(
    onBackClick: () -> Unit,
    viewModel: AddAppsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val installedApps by viewModel.installedApps.collectAsState()
    val trackedApps by viewModel.trackedApps.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Cargar todas las aplicaciones instaladas, incluidas las del sistema
        withContext(Dispatchers.IO) {
            val packageManager = context.packageManager
            val installedPackages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

            val allApps = installedPackages
                .map { packageInfo ->
                    val packageName = packageInfo.packageName
                    val appInfo = try {
                        packageManager.getApplicationInfo(packageName, 0)
                    } catch (e: PackageManager.NameNotFoundException) {
                        null
                    }

                    val appName = appInfo?.let {
                        packageManager.getApplicationLabel(it).toString()
                    } ?: packageName

                    AppEntity(
                        packageName = packageName,
                        appName = appName.toString(),
                        isTracked = false
                    )
                }
                // Excluir nuestra propia aplicación
                .filter { it.packageName != context.packageName }
                .sortedBy { it.appName } // Ordenar por nombre

            withContext(Dispatchers.Main) {
                viewModel.setInstalledApps(allApps)
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir Apps para Rastrear") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { isSearchActive = false },
                active = isSearchActive,
                onActiveChange = { isSearchActive = it },
                placeholder = { Text("Buscar apps") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") }
            ) {
                // Resultados de búsqueda irían aquí
            }

            if (isLoading) {
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
                    val filteredApps = installedApps.filter {
                        (it.appName.contains(searchQuery, ignoreCase = true) ||
                                it.packageName.contains(searchQuery, ignoreCase = true))
                    }

                    item {
                        Text(
                            text = "Total: ${filteredApps.size} aplicaciones",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    items(filteredApps) { app ->
                        val isTracked = trackedApps.any { it.packageName == app.packageName }

                        ListItem(
                            headlineContent = { Text(app.appName) },
                            supportingContent = { Text(app.packageName, style = MaterialTheme.typography.bodySmall) },
                            trailingContent = {
                                Checkbox(
                                    checked = isTracked,
                                    onCheckedChange = { checked ->
                                        viewModel.toggleAppTracking(app.packageName, checked)
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
