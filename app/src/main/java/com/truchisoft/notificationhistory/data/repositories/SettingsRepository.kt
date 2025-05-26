package com.truchisoft.notificationhistory.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.truchisoft.notificationhistory.LocaleMode
import com.truchisoft.notificationhistory.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val themeKey = stringPreferencesKey("theme_mode")
    private val localeKey = stringPreferencesKey("locale_mode")

    val themeSettings: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[themeKey] ?: ThemeMode.SYSTEM.name
            try {
                ThemeMode.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
        }

    val localeSettings: Flow<LocaleMode> = context.dataStore.data
        .map { preferences ->
            val localeName = preferences[localeKey] ?: LocaleMode.SYSTEM.name
            try {
                LocaleMode.valueOf(localeName)
            } catch (e: IllegalArgumentException) {
                LocaleMode.SYSTEM
            }
        }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[themeKey] = themeMode.name
        }
    }

    suspend fun setLocaleMode(localeMode: LocaleMode) {
        context.dataStore.edit { preferences ->
            preferences[localeKey] = localeMode.name
        }
    }
}
