package com.truchisoft.notificationhistory.data.repositories

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.truchisoft.notificationhistory.LocaleMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private val Context.localeDataStore: DataStore<Preferences> by preferencesDataStore(name = "locale_settings")

@Singleton
class LocaleRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val localeKey = intPreferencesKey("locale_mode")
    
    val localeMode: Flow<LocaleMode> = context.localeDataStore.data.map { preferences ->
        val localeOrdinal = preferences[localeKey] ?: LocaleMode.SYSTEM.ordinal
        LocaleMode.values()[localeOrdinal]
    }
    
    suspend fun setLocaleMode(localeMode: LocaleMode) {
        // Guardar la preferencia
        context.localeDataStore.edit { preferences ->
            preferences[localeKey] = localeMode.ordinal
        }
        
        // Aplicar inmediatamente el cambio
        applyLocale(localeMode)
    }
    
    fun applyLocale(localeMode: LocaleMode) {
        val localeList = when (localeMode) {
            LocaleMode.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
            else -> LocaleListCompat.create(localeMode.locale)
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }
    
    fun getCurrentLocale(): LocaleMode {
        val currentLocale = AppCompatDelegate.getApplicationLocales()
        if (currentLocale.isEmpty) {
            return LocaleMode.SYSTEM
        }
        val locale = currentLocale[0] ?: Locale.getDefault()
        return LocaleMode.fromLocale(locale)
    }
}
