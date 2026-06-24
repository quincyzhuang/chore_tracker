package com.choretracker.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.choretracker.app.ui.screens.ChoresScreen
import com.choretracker.app.ui.screens.HistoryScreen
import com.choretracker.app.ui.screens.ManageChoresScreen
import com.choretracker.app.ui.screens.OverviewScreen
import com.choretracker.app.viewmodel.ChoreViewModel

@Composable
fun NavGraph(navController: NavHostController, viewModel: ChoreViewModel) {
    NavHost(navController = navController, startDestination = BottomNavItem.Overview.route) {
        composable(BottomNavItem.Overview.route) {
            OverviewScreen(viewModel = viewModel)
        }
        composable(BottomNavItem.Chores.route) {
            ChoresScreen(viewModel = viewModel)
        }
        composable(BottomNavItem.Manage.route) {
            ManageChoresScreen(viewModel = viewModel)
        }
        composable(BottomNavItem.History.route) {
            HistoryScreen(viewModel = viewModel)
        }
    }
}
