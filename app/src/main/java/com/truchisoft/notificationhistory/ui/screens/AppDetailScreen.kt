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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.activity.compose.BackHandler
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

    var isInSelectionMode by rememberSaveable { mutableStateOf(false) }
    val selectedNotificationIds = remember { mutableStateListOf<Long>() }

    // Handle back press to exit selection mode
    BackHandler(enabled = isInSelectionMode) {
        isInSelectionMode = false
        selectedNotificationIds.clear()
    }

    LaunchedEffect(packageName) {
        viewModel.loadAppDetails(packageName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isInSelectionMode) {
                        Text("${selectedNotificationIds.size} seleccionadas")
                    } else {
                        Text(app?.appName ?: "Detalles de la App")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isInSelectionMode) {
                            isInSelectionMode = false
                            selectedNotificationIds.clear()
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (isInSelectionMode) {
                        IconButton(onClick = {
                            if (selectedNotificationIds.size == notifications.size) {
                                // Deselect all
                                selectedNotificationIds.clear()
                            } else {
                                // Select all
                                selectedNotificationIds.clear()
                                selectedNotificationIds.addAll(notifications.map { it.id })
                            }
                        }) {
                            if (selectedNotificationIds.size == notifications.size) {
                                Icon(Icons.Default.Clear, contentDescription = "Deseleccionar todo")
                            } else {
                                Icon(Icons.Default.SelectAll, contentDescription = "Seleccionar todo")
                            }
                        }
                        IconButton(
                            onClick = {
                                viewModel.deleteNotifications(selectedNotificationIds.toList())
                                isInSelectionMode = false
                                selectedNotificationIds.clear()
                            },
                            enabled = selectedNotificationIds.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar seleccionadas")
                        }
                    } else {
                        IconButton(onClick = { viewModel.markAllAsRead() }) {
                            Icon(Icons.Default.DoneAll, contentDescription = "Marcar todo como leído")
                        }
                        IconButton(onClick = {
                            // TODO: Add confirmation dialog before deleting all
                            viewModel.clearNotifications()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Borrar Todas las Notificaciones")
                        }
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
                        items(notifications, key = { it.id }) { notification ->
                            val isSelected = selectedNotificationIds.contains(notification.id)
                            NotificationCard(
                                notification = notification,
                                dateFormat = dateFormat,
                                isInSelectionMode = isInSelectionMode,
                                isSelected = isSelected,
                                onToggleSelection = {
                                    if (selectedNotificationIds.contains(notification.id)) {
                                        selectedNotificationIds.remove(notification.id)
                                    } else {
                                        selectedNotificationIds.add(notification.id)
                                    }
                                    // If all items are deselected, exit selection mode
                                    if (selectedNotificationIds.isEmpty()) {
                                        isInSelectionMode = false
                                    }
                                },
                                onEnableSelectionMode = {
                                    isInSelectionMode = true
                                    selectedNotificationIds.add(notification.id)
                                },
                                onCopy = {
                                    copyToClipboard(context, notification)
                                    Toast.makeText(context, "Notificación copiada", Toast.LENGTH_SHORT).show()
                                },
                                onDelete = {
                                    viewModel.deleteNotification(notification.id)
                                    // Ensure ID is removed from selection if it was selected
                                    selectedNotificationIds.remove(notification.id)
                                    if (selectedNotificationIds.isEmpty() && isInSelectionMode) {
                                        isInSelectionMode = false
                                    }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationCard(
    notification: NotificationEntity,
    dateFormat: SimpleDateFormat,
    isInSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onEnableSelectionMode: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onMarkAsRead: () -> Unit,
    onAddToIgnoreList: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }

    val cardColors = if (isSelected) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) // Color for selected items
        )
    } else if (!notification.isRead) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f) // Color for unread items
        )
    } else {
        CardDefaults.cardColors() // Default color
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .combinedClickable(
                onClick = {
                    if (isInSelectionMode) {
                        onToggleSelection()
                    } else {
                        // Original click action: show details or similar, handled by dropdown for now
                        // For simplicity, we can make the whole card clickable to show details
                        // or rely on the menu. If relying on menu, this can be empty.
                        // Alternatively, directly toggle details dialog:
                        // showDetailsDialog = true
                    }
                },
                onLongClick = {
                    if (!isInSelectionMode) {
                        onEnableSelectionMode()
                    }
                }
            ),
        colors = cardColors
    ) {
        Row(
            modifier = Modifier
                .padding(if (isInSelectionMode) 0.dp else 16.dp) // No padding when checkbox is visible for alignment
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isInSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelection() },
                    modifier = Modifier.padding(start = 16.dp, end = 8.dp) // Adjust padding as needed
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp, horizontal = if (isInSelectionMode) 0.dp else 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!notification.isRead && !isInSelectionMode) { // Hide "Nuevo" when in selection mode or read
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
                        modifier = Modifier.weight(if (isInSelectionMode) 1f else 0.8f) // Adjust weight if menu is present
                    )

                    if (!isInSelectionMode) {
                        IconButton(
                            onClick = { showMenu = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Más opciones"
                            )
                        }
                    }
                }

                DropdownMenu(
                    expanded = showMenu, // This will only be true if !isInSelectionMode
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
                            onDelete() // This already handles removing from selection if needed
                            showMenu = false
                        }
                    )
                }

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
