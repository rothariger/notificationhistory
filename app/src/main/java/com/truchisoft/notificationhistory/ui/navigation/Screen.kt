package com.truchisoft.notificationhistory.ui.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object AppDetail : Screen("app_detail/{packageName}") {
        fun createRoute(packageName: String) = "app_detail/$packageName"
    }
    object AddApps : Screen("add_apps")
    object Settings : Screen("settings")
}
