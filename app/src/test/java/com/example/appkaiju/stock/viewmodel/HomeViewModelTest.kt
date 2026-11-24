@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.appkaiju.stock.viewmodel

import com.example.appkaiju.stock.Datos
import com.example.appkaiju.stock.model.EstadoVenta
import com.example.appkaiju.stock.model.Venta
import com.example.appkaiju.stock.model.VentaConUsuario
import com.example.appkaiju.stock.repository.ProductoRepository
import com.example.appkaiju.stock.repository.VentaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class HomeViewModelTest : StringSpec({

    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    "el panel calcula los indicadores y la lista de reposición (RF-18, RF-19)" {
        val normal = Datos.producto(id = 1, nombre = "Taza", stock = 10, minimo = 3)
        val bajoB = Datos.producto(id = 2, nombre = "B Gorro", stock = 2, minimo = 3)
        val agotado = Datos.producto(id = 3, nombre = "Polerón", stock = 0, minimo = 3)
        val bajoA = Datos.producto(id = 4, nombre = "A Cargador", stock = 2, minimo = 5)

        val ahora = System.currentTimeMillis()
        val dosDiasAtras = ahora - 2 * 24 * 60 * 60 * 1000L
        val ventas = listOf(
            VentaConUsuario(Venta(id = 1, fechaHora = ahora, total = 5000, usuarioId = 1), "Admin"),
            VentaConUsuario(
                Venta(id = 2, fechaHora = ahora, total = 9000, estado = EstadoVenta.ANULADA, usuarioId = 1),
                "Admin"
            ),
            VentaConUsuario(Venta(id = 3, fechaHora = dosDiasAtras, total = 7000, usuarioId = 1), "Admin")
        )
        val productos = mockk<ProductoRepository>()
        val ventasRepo = mockk<VentaRepository>()
        every { productos.buscar("") } returns flowOf(listOf(normal, bajoB, agotado, bajoA))
        every { ventasRepo.observarVentas() } returns flowOf(ventas)

        runTest {
            val vm = HomeViewModel(productos, ventasRepo)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.resumen.collect {} }
            advanceUntilIdle()

            val r = vm.resumen.value
            r.productosActivos shouldBe 4
            r.stockBajo shouldBe 2
            r.agotados shouldBe 1
            r.ventasHoy shouldBe 1          // la anulada y la de hace dos días no cuentan
            r.montoHoy shouldBe 5000
            // Agotados primero, luego por stock y nombre
            r.reposicion.map { it.id } shouldBe listOf(3L, 4L, 2L)
        }
    }
})