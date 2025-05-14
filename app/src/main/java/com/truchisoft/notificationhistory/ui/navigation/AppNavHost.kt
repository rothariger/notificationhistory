package com.truchisoft.notificationhistory.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.truchisoft.notificationhistory.ui.screens.AddAppsScreen
import com.truchisoft.notificationhistory.ui.screens.AppDetailScreen
import com.truchisoft.notificationhistory.ui.screens.MainScreen
import com.truchisoft.notificationhistory.ui.screens.SettingsScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onAppClick = { packageName ->
                    navController.navigate(Screen.AppDetail.createRoute(packageName))
                },
                onAddAppsClick = {
                    navController.navigate(Screen.AddApps.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.AddApps.route) {
            AddAppsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.AppDetail.route,
            arguments = Screen.AppDetail.arguments
        ) { backStackEntry ->
            val packageName = backStackEntry.arguments?.getString(Screen.AppDetail.PACKAGE_NAME_ARG) ?: ""
            AppDetailScreen(
                packageName = packageName,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
