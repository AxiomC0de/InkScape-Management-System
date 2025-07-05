package com.joshua.inkscape.navigation
 
sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Sales : Screen("sales")
    data object Inventory : Screen("inventory")
    data object AddProduct : Screen("addProduct")
    data object ManageCategories : Screen("manageCategories")
    data object EditProduct : Screen("editProduct/{productId}") {
        fun createRoute(productId: String) = "editProduct/$productId"
    }
    data object CreateSale : Screen("createSale")
} 