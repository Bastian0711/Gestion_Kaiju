package com.example.appkaiju.stock.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appkaiju.stock.ui.screens.CatalogScreen
import com.example.appkaiju.stock.ui.screens.HomeScreen
import com.example.appkaiju.stock.ui.screens.LoginScreen
import com.example.appkaiju.stock.ui.screens.ProductListScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginClick = {
                    navController.navigate(Routes.HOME) {
                        // Evita volver al login con el botón Atrás (RF-02)
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onCatalogClick = { navController.navigate(Routes.CATALOG) }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onProductsClick = { navController.navigate(Routes.PRODUCTS) },
                onCatalogClick = { navController.navigate(Routes.CATALOG) }
            )
        }
        composable(Routes.PRODUCTS) {
            ProductListScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.CATALOG) {
            CatalogScreen(onBack = { navController.popBackStack() })
        }
    }
}
