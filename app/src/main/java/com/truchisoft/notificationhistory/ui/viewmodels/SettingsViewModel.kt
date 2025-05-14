package com.truchisoft.notificationhistory.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    val themeSettings: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    val ignoredMessages: StateFlow<List<IgnoredMessageEntity>> =
        ignoredMessageRepository.getAllIgnoredMessages()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(themeMode)
        }
    }

    fun removeIgnoredMessage(id: Long) {
        viewModelScope.launch {
            ignoredMessageRepository.removeIgnoredMessage(id)
        }
    }
}
