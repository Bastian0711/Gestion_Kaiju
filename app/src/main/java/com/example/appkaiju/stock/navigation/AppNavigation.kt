package com.example.appkaiju.stock.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appkaiju.stock.data.AppContainer
import com.example.appkaiju.stock.model.Rol
import com.example.appkaiju.stock.model.TipoMovimiento
import com.example.appkaiju.stock.ui.screens.CatalogScreen
import com.example.appkaiju.stock.ui.screens.HomeScreen
import com.example.appkaiju.stock.ui.screens.KardexScreen
import com.example.appkaiju.stock.ui.screens.LoginScreen
import com.example.appkaiju.stock.ui.screens.MovementScreen
import com.example.appkaiju.stock.ui.screens.ProductDetailScreen
import com.example.appkaiju.stock.ui.screens.ProductFormScreen
import com.example.appkaiju.stock.ui.screens.ProductListScreen
import com.example.appkaiju.stock.viewmodel.AuthViewModel
import com.example.appkaiju.stock.viewmodel.KardexViewModel
import com.example.appkaiju.stock.viewmodel.MovementViewModel
import com.example.appkaiju.stock.viewmodel.ProductDetailViewModel
import com.example.appkaiju.stock.viewmodel.ProductFormViewModel
import com.example.appkaiju.stock.viewmodel.ProductListViewModel

@Composable
fun AppNavigation(authViewModel: AuthViewModel, container: AppContainer) {
    val navController = rememberNavController()
    val sesion by authViewModel.estado.collectAsState()

    val argProducto = listOf(navArgument(Routes.ARG_PRODUCTO_ID) { type = NavType.LongType })

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
                onNuevoProducto = { navController.navigate(Routes.PRODUCT_FORM) },
                onProductoClick = { id -> navController.navigate(Routes.productDetail(id)) }
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
        composable(Routes.PRODUCT_DETAIL, arguments = argProducto) { entrada ->
            val productoId = entrada.arguments?.getLong(Routes.ARG_PRODUCTO_ID) ?: 0L
            val viewModel: ProductDetailViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { ProductDetailViewModel(container.inventarioRepository, productoId) }
                }
            )
            ProductDetailScreen(
                viewModel = viewModel,
                esAdmin = sesion.usuario?.rol == Rol.ADMIN,
                onBack = { navController.popBackStack() },
                onRegistrar = { tipo -> navController.navigate(Routes.movement(productoId, tipo)) },
                onKardex = { navController.navigate(Routes.kardex(productoId)) }
            )
        }
        composable(
            Routes.MOVEMENT,
            arguments = argProducto + navArgument(Routes.ARG_TIPO) { type = NavType.StringType }
        ) { entrada ->
            val usuario = sesion.usuario ?: return@composable
            val productoId = entrada.arguments?.getLong(Routes.ARG_PRODUCTO_ID) ?: 0L
            val tipo = runCatching {
                TipoMovimiento.valueOf(entrada.arguments?.getString(Routes.ARG_TIPO).orEmpty())
            }.getOrDefault(TipoMovimiento.ENTRADA)
            val viewModel: MovementViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { MovementViewModel(container.inventarioRepository, productoId, tipo) }
                }
            )
            MovementScreen(
                viewModel = viewModel,
                usuario = usuario,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.KARDEX, arguments = argProducto) { entrada ->
            val productoId = entrada.arguments?.getLong(Routes.ARG_PRODUCTO_ID) ?: 0L
            val viewModel: KardexViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { KardexViewModel(container.inventarioRepository, productoId) }
                }
            )
            KardexScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.CATALOG) {
            CatalogScreen(onBack = { navController.popBackStack() })
        }
    }
}