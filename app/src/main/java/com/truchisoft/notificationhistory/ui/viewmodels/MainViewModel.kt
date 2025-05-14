package com.truchisoft.notificationhistory.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truchisoft.notificationhistory.data.database.entities.AppEntity
import com.truchisoft.notificationhistory.data.repositories.AppRepository
import com.truchisoft.notificationhistory.data.repositories.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _trackedApps = MutableStateFlow<List<AppEntity>>(emptyList())
    val trackedApps: StateFlow<List<AppEntity>> = _trackedApps.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Mapa de packageName a par (no leídos, total)
    private val _appNotificationCounts = MutableStateFlow<Map<String, Pair<Int, Int>>>(emptyMap())
    val appNotificationCounts: StateFlow<Map<String, Pair<Int, Int>>> = _appNotificationCounts.asStateFlow()

    init {
        viewModelScope.launch {
            appRepository.getTrackedApps().collect { apps ->
                _trackedApps.value = apps
                updateNotificationCounts(apps)
                _isLoading.value = false
            }
        }
    }

    private fun updateNotificationCounts(apps: List<AppEntity>) {
        viewModelScope.launch {
            val countsMap = mutableMapOf<String, Pair<Int, Int>>()

            for (app in apps) {
                val packageName = app.packageName

                try {
                    val unreadCount = notificationRepository.getUnreadNotificationCountByApp(packageName).first()
                    val totalCount = notificationRepository.getTotalNotificationCountByApp(packageName).first()

                    countsMap[packageName] = Pair(unreadCount, totalCount)
                } catch (e: Exception) {
                    // En caso de error, asumimos 0/0
                    countsMap[packageName] = Pair(0, 0)
                }
            }

            _appNotificationCounts.value = countsMap
        }
    }
}
