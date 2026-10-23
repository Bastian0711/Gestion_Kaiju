package com.example.appkaiju.stock.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appkaiju.stock.data.AppContainer
import com.example.appkaiju.stock.ui.screens.CatalogScreen
import com.example.appkaiju.stock.ui.screens.HomeScreen
import com.example.appkaiju.stock.ui.screens.LoginScreen
import com.example.appkaiju.stock.ui.screens.ProductFormScreen
import com.example.appkaiju.stock.ui.screens.ProductListScreen
import com.example.appkaiju.stock.viewmodel.AuthViewModel
import com.example.appkaiju.stock.viewmodel.ProductFormViewModel
import com.example.appkaiju.stock.viewmodel.ProductListViewModel

@Composable
fun AppNavigation(authViewModel: AuthViewModel, container: AppContainer) {
    val navController = rememberNavController()
    val sesion by authViewModel.estado.collectAsState()

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
            val viewModel: ProductListViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { ProductListViewModel(container.productoRepository) }
                }
            )
            ProductListScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNuevoProducto = { navController.navigate(Routes.PRODUCT_FORM) }
            )
        }
        composable(Routes.PRODUCT_FORM) {
            val viewModel: ProductFormViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { ProductFormViewModel(container.inventarioRepository) }
                }
            )
            ProductFormScreen(
                viewModel = viewModel,
                usuarioId = sesion.usuario?.id ?: 0L,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.CATALOG) {
            CatalogScreen(onBack = { navController.popBackStack() })
        }
    }
}