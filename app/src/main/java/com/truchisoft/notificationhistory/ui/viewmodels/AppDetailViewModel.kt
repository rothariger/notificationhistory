package com.truchisoft.notificationhistory.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truchisoft.notificationhistory.data.database.entities.AppEntity
import com.truchisoft.notificationhistory.data.database.entities.NotificationEntity
import com.truchisoft.notificationhistory.data.repositories.AppRepository
import com.truchisoft.notificationhistory.data.repositories.IgnoredMessageRepository
import com.truchisoft.notificationhistory.data.repositories.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppDetailViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val notificationRepository: NotificationRepository,
    private val ignoredMessageRepository: IgnoredMessageRepository
) : ViewModel() {

    private val _app = MutableStateFlow<AppEntity?>(null)
    val app: StateFlow<AppEntity?> = _app.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val notifications: StateFlow<List<NotificationEntity>> = _notifications.asStateFlow()

    private val _showOnlyUnread = MutableStateFlow(false)
    val showOnlyUnread: StateFlow<Boolean> = _showOnlyUnread.asStateFlow()

    private var currentPackageName: String? = null
    private var allNotifications: List<NotificationEntity> = emptyList()

    fun loadAppDetails(packageName: String) {
        currentPackageName = packageName

        viewModelScope.launch {
            appRepository.getAppByPackageName(packageName)?.let { app ->
                _app.value = app
            }

            // Asegurarnos de que estamos filtrando por el packageName correcto
            try {
                val notificationsList = notificationRepository.getNotificationsByApp(packageName).first()
                allNotifications = notificationsList
                updateFilteredNotifications()

                // Configurar un collector para actualizaciones futuras
                notificationRepository.getNotificationsByApp(packageName).collect { notifications ->
                    allNotifications = notifications
                    updateFilteredNotifications()
                }
            } catch (e: Exception) {
                // Si hay un error, intentamos obtener todas las notificaciones y filtrar manualmente
                notificationRepository.getAllNotifications().collect { allNotifications ->
                    val filtered = allNotifications.filter { it.appPackageName == packageName }
                    this@AppDetailViewModel.allNotifications = filtered
                    updateFilteredNotifications()
                }
            }
        }
    }

    private fun updateFilteredNotifications() {
        _notifications.value = if (_showOnlyUnread.value) {
            allNotifications.filter { !it.isRead }
        } else {
            allNotifications
        }
    }

    fun toggleShowOnlyUnread() {
        _showOnlyUnread.value = !_showOnlyUnread.value
        updateFilteredNotifications()
    }

    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)

            // Actualizar la lista después de marcar como leído
            allNotifications = allNotifications.map {
                if (it.id == notificationId) it.copy(isRead = true) else it
            }
            updateFilteredNotifications()
        }
    }

    fun markAllAsRead() {
        currentPackageName?.let { packageName ->
            viewModelScope.launch {
                try {
                    notificationRepository.markAllAsRead(packageName)

                    // Actualizar la lista después de marcar todo como leído
                    allNotifications = allNotifications.map {
                        it.copy(isRead = true, uniqueKey = it.uniqueKey ?: "")
                    }
                    updateFilteredNotifications()
                } catch (e: Exception) {
                    // Si hay un error, actualizamos manualmente
                    allNotifications.forEach { notification ->
                        if (!notification.isRead) {
                            notificationRepository.markAsRead(notification.id)
                        }
                    }
                    allNotifications = allNotifications.map {
                        it.copy(isRead = true, uniqueKey = it.uniqueKey ?: "")
                    }
                    updateFilteredNotifications()
                }
            }
        }
    }

    fun clearNotifications() {
        currentPackageName?.let { packageName ->
            viewModelScope.launch {
                try {
                    notificationRepository.deleteNotificationsByApp(packageName)
                    allNotifications = emptyList()
                    updateFilteredNotifications()
                } catch (e: Exception) {
                    // Si hay un error, intentamos obtener todas las notificaciones y eliminar manualmente
                    val notificationsToDelete = allNotifications
                    for (notification in notificationsToDelete) {
                        notificationRepository.deleteNotification(notification.id)
                    }
                    allNotifications = emptyList()
                    updateFilteredNotifications()
                }
            }
        }
    }

    fun deleteNotification(notificationId: Long) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)

            // Actualizar la lista después de eliminar
            allNotifications = allNotifications.filter { it.id != notificationId }
            updateFilteredNotifications()
        }
    }

    fun addToIgnoreList(title: String, content: String) {
        viewModelScope.launch {
            // Decidir qué patrón añadir al diccionario de ignorados
            val pattern = when {
                title.isNotEmpty() -> title
                content.isNotEmpty() -> content
                else -> return@launch // No hay nada que añadir
            }

            ignoredMessageRepository.addIgnoredMessage(pattern, isExactMatch = true)
        }
    }
}
