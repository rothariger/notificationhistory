package com.truchisoft.notificationhistory.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truchisoft.notificationhistory.data.database.entities.AppEntity
import com.truchisoft.notificationhistory.data.repositories.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddAppsViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {

    private val _installedApps = MutableStateFlow<List<AppEntity>>(emptyList())
    val installedApps: StateFlow<List<AppEntity>> = _installedApps.asStateFlow()

    private val _trackedApps = MutableStateFlow<List<AppEntity>>(emptyList())
    val trackedApps: StateFlow<List<AppEntity>> = _trackedApps.asStateFlow()

    init {
        viewModelScope.launch {
            appRepository.getTrackedApps().collect { apps ->
                _trackedApps.value = apps
            }
        }
    }

    fun setInstalledApps(apps: List<AppEntity>) {
        _installedApps.value = apps
    }

    fun toggleAppTracking(packageName: String, isTracked: Boolean) {
        viewModelScope.launch {
            // Primero verificamos si la app ya existe en la base de datos
            val existingApp = appRepository.getAppByPackageName(packageName)

            if (existingApp != null) {
                // Si existe, actualizamos su estado
                existingApp.isTracked = isTracked
                appRepository.updateApp(existingApp)
            } else {
                // Si no existe, buscamos la app en la lista de instaladas y la añadimos
                val appToAdd = _installedApps.value.find { it.packageName == packageName }
                appToAdd?.let {
                    it.isTracked = isTracked
                    appRepository.insertApp(it)
                }
            }

            // Actualizamos la UI inmediatamente para mejor respuesta
            if (isTracked) {
                // Si estamos activando el tracking, añadimos la app a la lista de trackedApps si no está ya
                if (_trackedApps.value.none { it.packageName == packageName }) {
                    val appToAdd = _installedApps.value.find { it.packageName == packageName }
                    appToAdd?.let {
                        _trackedApps.value = _trackedApps.value + it.copy(isTracked = true)
                    }
                }
            } else {
                // Si estamos desactivando el tracking, quitamos la app de la lista de trackedApps
                _trackedApps.value = _trackedApps.value.filter { it.packageName != packageName }
            }
        }
    }
}
