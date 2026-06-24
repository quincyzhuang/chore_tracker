package com.choretracker.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Overview : BottomNavItem("overview", "Overview", Icons.Default.Home)
    object Chores : BottomNavItem("chores", "Chores", Icons.Default.CheckCircle)
    object Manage : BottomNavItem("manage", "Manage", Icons.Default.Assignment)
    object History : BottomNavItem("history", "History", Icons.Default.History)

    companion object {
        val items = listOf(Overview, Chores, Manage, History)
    }
}
