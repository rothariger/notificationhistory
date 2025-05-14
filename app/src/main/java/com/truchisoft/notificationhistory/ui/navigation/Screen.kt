package com.truchisoft.notificationhistory.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object AddApps : Screen("add_apps")
    object Settings : Screen("settings")

    object AppDetail : Screen("app_detail/{packageName}") {
        const val PACKAGE_NAME_ARG = "packageName"

        val arguments = listOf(
            navArgument(PACKAGE_NAME_ARG) {
                type = NavType.StringType
            }
        )

        fun createRoute(packageName: String): String {
            return "app_detail/$packageName"
        }
    }
}
