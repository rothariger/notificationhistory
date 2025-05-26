package com.truchisoft.notificationhistory.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truchisoft.notificationhistory.LocaleMode
import com.truchisoft.notificationhistory.ThemeMode
import com.truchisoft.notificationhistory.data.database.entities.IgnoredMessageEntity
import com.truchisoft.notificationhistory.data.repositories.IgnoredMessageRepository
import com.truchisoft.notificationhistory.data.repositories.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val ignoredMessageRepository: IgnoredMessageRepository
) : ViewModel() {

    val themeSettings: StateFlow<ThemeMode> = settingsRepository.themeSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    val localeSettings: StateFlow<LocaleMode> = settingsRepository.localeSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LocaleMode.SYSTEM
        )

    val ignoredMessages: StateFlow<List<IgnoredMessageEntity>> =
        ignoredMessageRepository.getAllIgnoredMessages()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true

            try {
                // No necesitamos hacer nada especial aquí ya que los flujos se actualizarán automáticamente
                // cuando cambien los datos subyacentes. Solo simulamos una actualización.
                kotlinx.coroutines.delay(500) // Simular una actualización
            } catch (e: Exception) {
                // Manejar error si es necesario
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(themeMode)
        }
    }

    fun setLocaleMode(localeMode: LocaleMode) {
        viewModelScope.launch {
            settingsRepository.setLocaleMode(localeMode)
        }
    }

    fun removeIgnoredMessage(id: Long) {
        viewModelScope.launch {
            ignoredMessageRepository.removeIgnoredMessage(id)
        }
    }

    fun addIgnoredMessage(pattern: String, isExactMatch: Boolean = false) {
        viewModelScope.launch {
            ignoredMessageRepository.addIgnoredMessage(pattern, isExactMatch)
        }
    }
}
