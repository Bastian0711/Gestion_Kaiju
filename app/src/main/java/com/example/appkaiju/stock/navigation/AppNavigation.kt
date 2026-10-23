package com.example.appkaiju.stock.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appkaiju.stock.ui.screens.CatalogScreen
import com.example.appkaiju.stock.ui.screens.HomeScreen
import com.example.appkaiju.stock.ui.screens.LoginScreen
import com.example.appkaiju.stock.ui.screens.ProductListScreen
import com.example.appkaiju.stock.viewmodel.AuthViewModel

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginExitoso = {
                    navController.navigate(Routes.HOME) {

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