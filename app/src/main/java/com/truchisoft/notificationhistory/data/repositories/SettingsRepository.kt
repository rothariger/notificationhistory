package com.truchisoft.notificationhistory.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.truchisoft.notificationhistory.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val themeKey = intPreferencesKey("theme_mode")
    
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val themeOrdinal = preferences[themeKey] ?: ThemeMode.SYSTEM.ordinal
        ThemeMode.values()[themeOrdinal]
    }
    
    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[themeKey] = themeMode.ordinal
        }
    }
}
