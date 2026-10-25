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
import com.example.appkaiju.stock.ui.screens.CartScreen
import com.example.appkaiju.stock.ui.screens.CatalogScreen
import com.example.appkaiju.stock.ui.screens.HomeScreen
import com.example.appkaiju.stock.ui.screens.KardexScreen
import com.example.appkaiju.stock.ui.screens.LoginScreen
import com.example.appkaiju.stock.ui.screens.MovementScreen
import com.example.appkaiju.stock.ui.screens.ProductDetailScreen
import com.example.appkaiju.stock.ui.screens.ProductFormScreen
import com.example.appkaiju.stock.ui.screens.ProductListScreen
import com.example.appkaiju.stock.ui.screens.RegisterScreen
import com.example.appkaiju.stock.ui.screens.SaleDetailScreen
import com.example.appkaiju.stock.ui.screens.SaleHistoryScreen
import com.example.appkaiju.stock.ui.screens.SaleScreen
import com.example.appkaiju.stock.viewmodel.AuthViewModel
import com.example.appkaiju.stock.viewmodel.CartViewModel
import com.example.appkaiju.stock.viewmodel.CatalogViewModel
import com.example.appkaiju.stock.viewmodel.KardexViewModel
import com.example.appkaiju.stock.viewmodel.MovementViewModel
import com.example.appkaiju.stock.viewmodel.ProductDetailViewModel
import com.example.appkaiju.stock.viewmodel.ProductFormViewModel
import com.example.appkaiju.stock.viewmodel.ProductListViewModel
import com.example.appkaiju.stock.viewmodel.RegisterViewModel
import com.example.appkaiju.stock.viewmodel.SaleDetailViewModel
import com.example.appkaiju.stock.viewmodel.SaleHistoryViewModel
import com.example.appkaiju.stock.viewmodel.SaleViewModel
import com.example.appkaiju.stock.viewmodel.HomeViewModel

@Composable
fun AppNavigation(authViewModel: AuthViewModel, container: AppContainer) {
    val navController = rememberNavController()
    val sesion by authViewModel.estado.collectAsState()

    val cartViewModel: CartViewModel = viewModel()
    val carrito by cartViewModel.estado.collectAsState()

    val argProducto = listOf(navArgument(Routes.ARG_PRODUCTO_ID) { type = NavType.LongType })
    val argVenta = listOf(navArgument(Routes.ARG_VENTA_ID) { type = NavType.LongType })

    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginExitoso = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onCatalogClick = { navController.navigate(Routes.CATALOG) },
                onRegistroClick = { navController.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.REGISTER) {
            val viewModel: RegisterViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { RegisterViewModel(container.usuarioRepository) }
                }
            )
            RegisterScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onRegistroExitoso = { usuario ->
                    authViewModel.establecerSesion(usuario)
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            val homeViewModel: HomeViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        HomeViewModel(container.productoRepository, container.ventaRepository)
                    }
                }
            )
            val resumen by homeViewModel.resumen.collectAsState()
            HomeScreen(
                usuario = sesion.usuario,
                resumen = resumen,
                onProductsClick = { navController.navigate(Routes.PRODUCTS) },
                onVentasClick = { navController.navigate(Routes.SALES) },
                onCatalogClick = { navController.navigate(Routes.CATALOG) },
                onCartClick = { navController.navigate(Routes.CART) },
                onCerrarSesion = {
                    authViewModel.cerrarSesion()
                    cartViewModel.limpiar()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
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
        composable(Routes.SALES) {
            val viewModel: SaleHistoryViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { SaleHistoryViewModel(container.ventaRepository) }
                }
            )
            SaleHistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNuevaVenta = { navController.navigate(Routes.SALE_NEW) },
                onVentaClick = { id -> navController.navigate(Routes.saleDetail(id)) }
            )
        }
        composable(Routes.SALE_NEW) {
            val usuario = sesion.usuario ?: return@composable
            val viewModel: SaleViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { SaleViewModel(container.productoRepository, container.ventaRepository) }
                }
            )
            SaleScreen(
                viewModel = viewModel,
                usuario = usuario,
                onBack = { navController.popBackStack() },
                onVentaRegistrada = { id ->
                    navController.navigate(Routes.saleDetail(id)) {
                        popUpTo(Routes.SALE_NEW) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.SALE_DETAIL, arguments = argVenta) { entrada ->
            val ventaId = entrada.arguments?.getLong(Routes.ARG_VENTA_ID) ?: 0L
            val viewModel: SaleDetailViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { SaleDetailViewModel(container.ventaRepository, ventaId) }
                }
            )
            SaleDetailScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.CATALOG) {
            val viewModel: CatalogViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { CatalogViewModel(container.productoRepository) }
                }
            )
            CatalogScreen(
                viewModel = viewModel,
                puedeComprar = sesion.usuario?.rol == Rol.CLIENTE,
                unidadesEnCarrito = carrito.cantidadTotal,
                cantidades = carrito.items.associate { it.producto.id to it.cantidad },
                onAgregar = cartViewModel::agregar,
                onCarrito = { navController.navigate(Routes.CART) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.CART) {
            CartScreen(
                viewModel = cartViewModel,
                onBack = { navController.popBackStack() },
                onIrAlCatalogo = { navController.navigate(Routes.CATALOG) }
            )
        }
    }
}