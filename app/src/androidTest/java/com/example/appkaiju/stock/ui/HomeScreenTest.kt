package com.example.appkaiju.stock.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.appkaiju.stock.model.Rol
import com.example.appkaiju.stock.model.Usuario
import com.example.appkaiju.stock.ui.screens.HomeScreen
import com.example.appkaiju.stock.viewmodel.ResumenState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val admin = Usuario(1, "Administrador Kaiju", "admin@kaiju.cl", "h", Rol.ADMIN)
    private val cliente = Usuario(2, "Cliente de Prueba", "cliente@kaiju.cl", "h", Rol.CLIENTE)

    private fun mostrar(usuario: Usuario, onProductos: () -> Unit = {}) {
        composeRule.setContent {
            HomeScreen(
                usuario = usuario,
                resumen = ResumenState(productosActivos = 12, stockBajo = 2, agotados = 1),
                onProductsClick = onProductos,
                onVentasClick = {},
                onCatalogClick = {},
                onCartClick = {},
                onCerrarSesion = {}
            )
        }
    }

    @Test
    fun administrador_veProductosYVentas() {
        var clicks = 0
        mostrar(admin) { clicks++ }

        composeRule.onNodeWithText("Perfil: Administrador").assertIsDisplayed()
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("Productos"))
        composeRule.onNodeWithText("Productos").assertIsDisplayed().performClick()
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("Ventas"))
        composeRule.onNodeWithText("Ventas").assertIsDisplayed()
        composeRule.onAllNodesWithText("Mi carrito").assertCountEquals(0)
        assertEquals(1, clicks)
    }

    @Test
    fun cliente_veCatalogoYCarritoPeroNoLaAdministracion() {
        mostrar(cliente)

        composeRule.onNodeWithText("Perfil: Cliente").assertIsDisplayed()
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("Mi carrito"))
        composeRule.onNodeWithText("Mi carrito").assertIsDisplayed()
        composeRule.onAllNodesWithText("Productos").assertCountEquals(0)
        composeRule.onAllNodesWithText("Ventas").assertCountEquals(0)
    }
}