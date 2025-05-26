package com.truchisoft.notificationhistory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.truchisoft.notificationhistory.ui.navigation.AppNavHost
import com.truchisoft.notificationhistory.ui.theme.NotificationHistoryTheme
import com.truchisoft.notificationhistory.ui.viewmodels.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeSettings by settingsViewModel.themeSettings.collectAsState()
            val localeSettings by settingsViewModel.localeSettings.collectAsState()

            val isDarkTheme = when (themeSettings) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }

            NotificationHistoryTheme(darkTheme = isDarkTheme) {
                AppNavHost(navController = navController)
            }
        }
    }
}
