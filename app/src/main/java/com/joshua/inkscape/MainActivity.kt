package com.joshua.inkscape

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.joshua.inkscape.navigation.BottomNavigationBar
import com.joshua.inkscape.navigation.NavigationManager
import com.joshua.inkscape.navigation.Screen
import com.joshua.inkscape.ui.screens.AddProductScreen
import com.joshua.inkscape.ui.screens.CreateSaleScreen
import com.joshua.inkscape.ui.screens.DashboardScreen
import com.joshua.inkscape.ui.screens.EditProductScreen
import com.joshua.inkscape.ui.screens.InventoryScreen
import com.joshua.inkscape.ui.screens.ManageCategoriesScreen
import com.joshua.inkscape.ui.screens.SalesScreen
import com.joshua.inkscape.ui.screens.UnsavedChangesDialog
import com.joshua.inkscape.ui.theme.InkScapeV10Theme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private val navigationManager = NavigationManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigationManager.handleBackPress()
            }
        })

        enableEdgeToEdge()
        setContent {
            InkScapeV10Theme {
                val navController = rememberNavController()
                val showUnsavedDialog by navigationManager.showUnsavedChangesDialog.collectAsState()

                LaunchedEffect(navController) {
                    navigationManager.setNavController(navController)
                }

                if (showUnsavedDialog) {
                    UnsavedChangesDialog(
                        onDismiss = { navigationManager.onDismissUnsavedChangesDialog() },
                        onConfirm = { navigationManager.onConfirmDiscardChanges() }
                    )
                }

                Scaffold(
                    bottomBar = {
                        BottomNavigationBar(
                            navController = navController,
                            navigationManager = navigationManager
                        )
                    }
                ) { innerPadding ->
                    InkscapeNavHost(
                        navController = navController,
                        navigationManager = navigationManager,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun InkscapeNavHost(
    navController: NavHostController,
    navigationManager: NavigationManager,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen()
        }
        composable(Screen.Sales.route) {
            SalesScreen(navController = navController)
        }
        composable(Screen.Inventory.route) {
            InventoryScreen(navController = navController)
        }
        composable(Screen.AddProduct.route) {
            AddProductScreen(
                navigationManager = navigationManager
            )
        }
        composable(Screen.ManageCategories.route) {
            ManageCategoriesScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.EditProduct.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            if (productId != null) {
                EditProductScreen(
                    productId = productId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        composable(Screen.CreateSale.route) {
            CreateSaleScreen(
                navigationManager = navigationManager
            )
        }
    }
} 