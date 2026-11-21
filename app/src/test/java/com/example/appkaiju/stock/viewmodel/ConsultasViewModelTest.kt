@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.appkaiju.stock.viewmodel

import com.example.appkaiju.stock.Datos
import com.example.appkaiju.stock.model.DetalleConProducto
import com.example.appkaiju.stock.model.DetalleVenta
import com.example.appkaiju.stock.model.MovimientoInventario
import com.example.appkaiju.stock.model.TipoMovimiento
import com.example.appkaiju.stock.model.Venta
import com.example.appkaiju.stock.model.VentaConUsuario
import com.example.appkaiju.stock.repository.InventarioRepository
import com.example.appkaiju.stock.repository.ProductoRepository
import com.example.appkaiju.stock.repository.VentaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class ConsultasViewModelTest : StringSpec({

    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    val producto = Datos.producto(id = 1)

    fun movimiento(id: Long, tipo: TipoMovimiento) = MovimientoInventario(
        id = id, productoId = 1, tipo = tipo, cantidad = 1, stockResultante = 10,
        fechaHora = id, motivo = "Motivo $id", usuarioId = 1
    )

    "el listado de productos busca por el texto escrito (RF-07)" {
        val repo = mockk<ProductoRepository>()
        every { repo.buscar(any()) } returns flowOf(listOf(producto))

        runTest {
            val vm = ProductListViewModel(repo)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.productos.collect {} }
            vm.onTextoChange("  cod-1 ")
            advanceUntilIdle()

            vm.texto.value shouldBe "  cod-1 "
            vm.productos.value shouldBe listOf(producto)
            verify { repo.buscar("cod-1") }
        }
    }

    "el detalle de producto muestra solo los últimos 5 movimientos (RF-08)" {
        val movimientos = (1L..7L).map { movimiento(it, TipoMovimiento.ENTRADA) }
        val repo = mockk<InventarioRepository>()
        every { repo.observarProducto(1) } returns flowOf(producto)
        every { repo.observarMovimientos(1) } returns flowOf(movimientos)

        runTest {
            val vm = ProductDetailViewModel(repo, 1)
            vm.estado.value.cargando shouldBe true
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.estado.collect {} }
            advanceUntilIdle()

            vm.estado.value.producto shouldBe producto
            vm.estado.value.ultimosMovimientos shouldBe movimientos.take(5)
            vm.estado.value.cargando shouldBe false
        }
    }

    "el kardex muestra el historial con responsables y se puede filtrar por tipo (RF-12)" {
        val movimientos = listOf(
            movimiento(3, TipoMovimiento.ENTRADA),
            movimiento(2, TipoMovimiento.SALIDA),
            movimiento(1, TipoMovimiento.ENTRADA)
        )
        val repo = mockk<InventarioRepository>()
        every { repo.observarProducto(1) } returns flowOf(producto)
        every { repo.observarMovimientos(1) } returns flowOf(movimientos)
        coEvery { repo.nombresUsuarios() } returns mapOf(1L to "Administrador Kaiju")

        runTest {
            val vm = KardexViewModel(repo, 1)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.estado.collect {} }
            advanceUntilIdle()
            vm.estado.value.movimientos shouldBe movimientos
            vm.estado.value.nombresUsuarios shouldBe mapOf(1L to "Administrador Kaiju")

            vm.onFiltroChange(TipoMovimiento.ENTRADA)
            advanceUntilIdle()
            vm.estado.value.filtro shouldBe TipoMovimiento.ENTRADA
            vm.estado.value.movimientos.map { it.id } shouldBe listOf(3L, 1L)

            vm.onFiltroChange(null)
            advanceUntilIdle()
            vm.estado.value.movimientos.size shouldBe 3
        }
    }

    "el historial de ventas expone las ventas del repositorio (RF-16)" {
        val venta = VentaConUsuario(Venta(id = 5, fechaHora = 1L, total = 1000, usuarioId = 1), "Admin")
        val repo = mockk<VentaRepository>()
        every { repo.observarVentas() } returns flowOf(listOf(venta))

        runTest {
            val vm = SaleHistoryViewModel(repo)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.ventas.collect {} }
            advanceUntilIdle()
            vm.ventas.value shouldBe listOf(venta)
        }
    }

    "el detalle de venta carga la venta y sus líneas (RF-17)" {
        val venta = VentaConUsuario(Venta(id = 5, fechaHora = 1L, total = 2000, usuarioId = 1), "Admin")
        val linea = DetalleConProducto(
            DetalleVenta(ventaId = 5, productoId = 1, cantidad = 2, precioUnitario = 1000, subtotal = 2000),
            "Polera Kaiju"
        )
        val repo = mockk<VentaRepository>()
        coEvery { repo.obtenerVenta(5) } returns venta
        coEvery { repo.detalleDeVenta(5) } returns listOf(linea)

        val vm = SaleDetailViewModel(repo, 5)

        vm.estado.value shouldBe SaleDetailState(venta = venta, detalles = listOf(linea), cargando = false)
    }
})