package com.truchisoft.notificationhistory.ui.viewmodels

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truchisoft.notificationhistory.data.database.entities.AppEntity
import com.truchisoft.notificationhistory.data.repositories.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddAppsViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val packageManager: PackageManager
) : ViewModel() {

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    private val _filteredApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val filteredApps: StateFlow<List<AppInfo>> = _filteredApps.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val installedPackages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                val trackedApps = appRepository.getAllApps().first()
                val trackedPackages = trackedApps.associateBy { it.packageName }

                val appInfoList = installedPackages
                    .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 } // Solo apps de usuario
                    .map { appInfo ->
                        val packageName = appInfo.packageName
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        val isTracked = trackedPackages[packageName]?.isTracked ?: false

                        AppInfo(
                            packageName = packageName,
                            appName = appName,
                            isTracked = isTracked
                        )
                    }
                    .sortedBy { it.appName }

                _installedApps.value = appInfoList
                updateFilteredApps()
            } catch (e: Exception) {
                // Manejar error si es necesario
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true

            try {
                val installedPackages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                val trackedApps = appRepository.getAllApps().first()
                val trackedPackages = trackedApps.associateBy { it.packageName }

                val appInfoList = installedPackages
                    .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 } // Solo apps de usuario
                    .map { appInfo ->
                        val packageName = appInfo.packageName
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        val isTracked = trackedPackages[packageName]?.isTracked ?: false

                        AppInfo(
                            packageName = packageName,
                            appName = appName,
                            isTracked = isTracked
                        )
                    }
                    .sortedBy { it.appName }

                _installedApps.value = appInfoList
                updateFilteredApps()
            } catch (e: Exception) {
                // Manejar error si es necesario
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun toggleAppTracking(packageName: String, isTracked: Boolean) {
        viewModelScope.launch {
            // Verificar si la app ya existe en la base de datos
            val existingApp = appRepository.getAppByPackageName(packageName)

            if (existingApp != null) {
                // Actualizar el estado de seguimiento
                appRepository.toggleAppTracking(packageName, isTracked)
            } else {
                // Crear una nueva entrada para la app
                val appInfo = _installedApps.value.find { it.packageName == packageName }
                if (appInfo != null) {
                    val newApp = AppEntity(
                        packageName = packageName,
                        appName = appInfo.appName,
                        isTracked = isTracked
                    )
                    appRepository.insertApp(newApp)
                }
            }

            // Actualizar la lista de apps instaladas
            updateAppTrackingState(packageName, isTracked)
        }
    }

    private fun updateAppTrackingState(packageName: String, isTracked: Boolean) {
        val updatedList = _installedApps.value.map { appInfo ->
            if (appInfo.packageName == packageName) {
                appInfo.copy(isTracked = isTracked)
            } else {
                appInfo
            }
        }
        _installedApps.value = updatedList
        updateFilteredApps()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        updateFilteredApps()
    }

    private fun updateFilteredApps() {
        val query = _searchQuery.value.trim().lowercase()
        val allApps = _installedApps.value

        _filteredApps.value = if (query.isEmpty()) {
            allApps
        } else {
            allApps.filter { appInfo ->
                appInfo.appName.lowercase().contains(query) ||
                        appInfo.packageName.lowercase().contains(query)
            }
        }
    }

    data class AppInfo(
        val packageName: String,
        val appName: String,
        val isTracked: Boolean
    )
}
