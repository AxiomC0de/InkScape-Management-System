package com.joshua.inkscape.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.joshua.inkscape.navigation.NavigationManager
import com.joshua.inkscape.navigation.Screen

sealed class NavigationItem(val route: String, val icon: ImageVector, val title: String) {
    data object Dashboard : NavigationItem(Screen.Dashboard.route, Icons.Filled.Home, "Dashboard")
    data object Sales : NavigationItem(Screen.Sales.route, Icons.Filled.ShoppingCart, "Sales")
    data object Inventory : NavigationItem(Screen.Inventory.route, Icons.Filled.List, "Inventory")
}

@Composable
fun BottomNavigationBar(navController: NavController, navigationManager: NavigationManager) {
    val items = listOf(
        NavigationItem.Dashboard,
        NavigationItem.Sales,
        NavigationItem.Inventory
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navigationManager.navigate {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
} 