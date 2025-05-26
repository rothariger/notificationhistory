package com.truchisoft.notificationhistory.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
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
    private val ignoredMessageRepository: IgnoredMessageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val packageName: String = savedStateHandle.get<String>("packageName") ?: ""

    private val _app = MutableStateFlow<AppEntity?>(null)
    val app: StateFlow<AppEntity?> = _app.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val notifications: StateFlow<List<NotificationEntity>> = _notifications.asStateFlow()

    private val _filteredNotifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val filteredNotifications: StateFlow<List<NotificationEntity>> = _filteredNotifications.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _selectionMode = MutableStateFlow(false)
    val selectionMode: StateFlow<Boolean> = _selectionMode.asStateFlow()

    private val _selectedItems = MutableStateFlow<Set<Long>>(emptySet())
    val selectedItems: StateFlow<Set<Long>> = _selectedItems.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val appEntity = appRepository.getAppByPackageName(packageName)
                _app.value = appEntity

                notificationRepository.getNotificationsByApp(packageName).collect { notificationList ->
                    _notifications.value = notificationList
                    updateFilteredNotifications()
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                // Manejar error si es necesario
                _isLoading.value = false
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true

            try {
                val appEntity = appRepository.getAppByPackageName(packageName)
                _app.value = appEntity

                val notificationList = notificationRepository.getNotificationsByApp(packageName).first()
                _notifications.value = notificationList
                updateFilteredNotifications()
            } catch (e: Exception) {
                // Manejar error si es necesario
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead(packageName)
        }
    }

    fun deleteNotification(notificationId: Long) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)
        }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch {
            notificationRepository.deleteNotificationsByApp(packageName)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        updateFilteredNotifications()
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
            updateFilteredNotifications()
        }
    }

    private fun updateFilteredNotifications() {
        val query = _searchQuery.value.trim().lowercase()
        val allNotifications = _notifications.value

        _filteredNotifications.value = if (query.isEmpty()) {
            allNotifications
        } else {
            allNotifications.filter { notification ->
                (notification.title?.lowercase()?.contains(query) == true) ||
                        (notification.content?.lowercase()?.contains(query) == true)
            }
        }
    }

    fun toggleSelectionMode() {
        _selectionMode.value = !_selectionMode.value
        if (!_selectionMode.value) {
            _selectedItems.value = emptySet()
        }
    }

    fun toggleItemSelection(notificationId: Long) {
        val currentSelection = _selectedItems.value.toMutableSet()
        if (currentSelection.contains(notificationId)) {
            currentSelection.remove(notificationId)
        } else {
            currentSelection.add(notificationId)
        }
        _selectedItems.value = currentSelection

        // Si no hay elementos seleccionados, salir del modo selección
        if (currentSelection.isEmpty()) {
            _selectionMode.value = false
        }
    }

    fun selectAllItems() {
        val allIds = _filteredNotifications.value.map { it.id }.toSet()
        _selectedItems.value = allIds
    }

    fun deleteSelectedItems() {
        viewModelScope.launch {
            val itemsToDelete = _selectedItems.value.toList()
            notificationRepository.deleteNotifications(itemsToDelete)
            _selectedItems.value = emptySet()
            _selectionMode.value = false
        }
    }

    fun addToIgnoreList(pattern: String) {
        viewModelScope.launch {
            if (pattern.isNotBlank()) {
                ignoredMessageRepository.addIgnoredMessage(pattern, isExactMatch = true)
            }
        }
    }
}
