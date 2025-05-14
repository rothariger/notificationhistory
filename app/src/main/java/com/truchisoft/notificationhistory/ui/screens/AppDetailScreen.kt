package com.truchisoft.notificationhistory.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.truchisoft.notificationhistory.data.database.entities.NotificationEntity
import com.truchisoft.notificationhistory.ui.viewmodels.AppDetailViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    packageName: String,
    onBackClick: () -> Unit,
    viewModel: AppDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val app by viewModel.app.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val showOnlyUnread by viewModel.showOnlyUnread.collectAsState()
    val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())

    LaunchedEffect(packageName) {
        viewModel.loadAppDetails(packageName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(app?.appName ?: "Detalles de la App") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.markAllAsRead() }) {
                        Icon(Icons.Default.DoneAll, contentDescription = "Marcar todo como leído")
                    }
                    IconButton(onClick = { viewModel.clearNotifications() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Borrar Todas las Notificaciones")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (app == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Filtro para mostrar solo no leídos
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = showOnlyUnread,
                        onCheckedChange = { viewModel.toggleShowOnlyUnread() }
                    )
                    Text(
                        text = "Mostrar solo no leídos",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (showOnlyUnread)
                                "No hay notificaciones no leídas para esta app"
                            else
                                "No hay notificaciones guardadas para esta app"
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        items(notifications) { notification ->
                            NotificationCard(
                                notification = notification,
                                dateFormat = dateFormat,
                                onCopy = {
                                    copyToClipboard(context, notification)
                                    Toast.makeText(context, "Notificación copiada", Toast.LENGTH_SHORT).show()
                                },
                                onDelete = {
                                    viewModel.deleteNotification(notification.id)
                                },
                                onMarkAsRead = {
                                    viewModel.markAsRead(notification.id)
                                },
                                onAddToIgnoreList = {
                                    viewModel.addToIgnoreList(notification.title ?: "", notification.content ?: "")
                                    Toast.makeText(context, "Añadido al diccionario de ignorados", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: NotificationEntity,
    dateFormat: SimpleDateFormat,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onMarkAsRead: () -> Unit,
    onAddToIgnoreList: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }

    // Indicador visual para notificaciones no leídas - Ahora más destacado
    val cardColors = if (!notification.isRead) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        )
    } else {
        CardDefaults.cardColors()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = cardColors
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicador visual adicional para no leídos
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Nuevo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Text(
                    text = notification.title ?: "Sin Título",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { showMenu = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Más opciones"
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Copiar") },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                        onClick = {
                            onCopy()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Ver detalles técnicos") },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                        onClick = {
                            showDetailsDialog = true
                            showMenu = false
                        }
                    )
                    if (!notification.isRead) {
                        DropdownMenuItem(
                            text = { Text("Marcar como leído") },
                            leadingIcon = { Icon(Icons.Default.DoneAll, contentDescription = null) },
                            onClick = {
                                onMarkAsRead()
                                showMenu = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Añadir a ignorados") },
                        onClick = {
                            onAddToIgnoreList()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                }
            }

            // Mostrar el contenido completo si está disponible, o el contenido normal si no
            Text(
                text = notification.fullContent ?: notification.content ?: "Sin Contenido",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = dateFormat.format(notification.timestamp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    if (showDetailsDialog) {
        Dialog(onDismissRequest = { showDetailsDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    Text(
                        text = "Detalles técnicos",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        val scrollState = rememberScrollState()
                        val extraData = notification.extraData
                        val (detailsContent, hasError) = remember(extraData) {
                            try {
                                if (!extraData.isNullOrEmpty()) {
                                    val jsonObject = JSONObject(extraData)
                                    val keys = jsonObject.keys()
                                    val contentList = mutableListOf<Pair<String, String>>()

                                    while (keys.hasNext()) {
                                        val key = keys.next()
                                        val value = jsonObject.getString(key)
                                        contentList.add(key to value)
                                    }

                                    contentList to false
                                } else {
                                    emptyList<Pair<String, String>>() to false
                                }
                            } catch (e: Exception) {
                                emptyList<Pair<String, String>>() to true
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                        ) {
                            if (hasError) {
                                Text("Error al procesar los datos técnicos")
                            } else if (detailsContent.isEmpty()) {
                                Text("No hay datos técnicos disponibles")
                            } else {
                                detailsContent.forEach { (key, value) ->
                                    Text(
                                        text = "$key:",
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                    Text(
                                        text = value,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showDetailsDialog = false },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 16.dp)
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, notification: NotificationEntity) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText(
        "Notificación",
        """
        Título: ${notification.title ?: "Sin título"}
        Contenido: ${notification.fullContent ?: notification.content ?: "Sin contenido"}
        Fecha: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(notification.timestamp)}
        """.trimIndent()
    )
    clipboardManager.setPrimaryClip(clipData)
}
