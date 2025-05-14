package com.truchisoft.notificationhistory.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.truchisoft.notificationhistory.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onAppClick: (String) -> Unit,
    onAddAppsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val trackedApps by viewModel.trackedApps.collectAsState()
    val appNotificationCounts by viewModel.appNotificationCounts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Verificar si tenemos permiso para acceder a las notificaciones
    LaunchedEffect(Unit) {
        val notificationListenerString = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        val hasPermission = notificationListenerString?.contains(context.packageName) == true

        if (!hasPermission) {
            showPermissionDialog = true
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permiso requerido") },
            text = {
                Text("Esta aplicación necesita acceso a las notificaciones para funcionar. " +
                        "Por favor, habilita el acceso a notificaciones en la configuración del sistema.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                        startActivity(context, intent, null)
                    }
                ) {
                    Text("Ir a Configuración")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Más tarde")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Notificaciones") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAppsClick) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Apps")
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (trackedApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No hay apps siendo rastreadas",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Toca el botón + para añadir apps",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Filtrar nuestra propia aplicación
                val filteredApps = trackedApps.filter { it.packageName != context.packageName }

                items(filteredApps) { app ->
                    val counts = appNotificationCounts[app.packageName]
                    val unreadCount = counts?.first ?: 0
                    val totalCount = counts?.second ?: 0

                    ListItem(
                        headlineContent = { Text(app.appName) },
                        supportingContent = {
                            Row {
                                Text("No leídos: $unreadCount")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Total: $totalCount")
                            }
                        },
                        trailingContent = {
                            if (unreadCount > 0) {
                                BadgedBox(
                                    badge = { Badge { Text(unreadCount.toString()) } }
                                ) {
                                    // Placeholder para el badge
                                    Spacer(modifier = Modifier.width(24.dp))
                                }
                            }
                        },
                        modifier = Modifier.clickable {
                            onAppClick(app.packageName)
                        }
                    )
                }
            }
        }
    }
}
