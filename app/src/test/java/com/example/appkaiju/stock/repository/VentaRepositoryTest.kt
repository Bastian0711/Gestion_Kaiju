package com.example.appkaiju.stock.repository

import com.example.appkaiju.stock.Datos
import com.example.appkaiju.stock.SinTransaccion
import com.example.appkaiju.stock.data.MovimientoDao
import com.example.appkaiju.stock.data.ProductoDao
import com.example.appkaiju.stock.data.VentaDao
import com.example.appkaiju.stock.model.DetalleConProducto
import com.example.appkaiju.stock.model.DetalleVenta
import com.example.appkaiju.stock.model.MovimientoInventario
import com.example.appkaiju.stock.model.TipoMovimiento
import com.example.appkaiju.stock.model.Venta
import com.example.appkaiju.stock.model.VentaConUsuario
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class VentaRepositoryTest : StringSpec({

    lateinit var ventaDao: VentaDao
    lateinit var productoDao: ProductoDao
    lateinit var movimientoDao: MovimientoDao
    lateinit var repo: VentaRepository

    beforeTest {
        ventaDao = mockk()
        productoDao = mockk()
        movimientoDao = mockk()
        repo = VentaRepository(ventaDao, productoDao, movimientoDao, SinTransaccion)
        coEvery { ventaDao.insertarVenta(any()) } returns 50L
        coEvery { ventaDao.insertarDetalles(any()) } just Runs
        coEvery { productoDao.actualizarStock(any(), any()) } just Runs
        coEvery { movimientoDao.insertar(any()) } returns 1L
    }

    "las consultas de ventas entregan los datos del DAO (RF-16, RF-17)" {
        val venta = VentaConUsuario(Venta(id = 50, fechaHora = 1L, total = 3000, usuarioId = 1), "Admin")
        val detalle = DetalleConProducto(
            DetalleVenta(ventaId = 50, productoId = 1, cantidad = 3, precioUnitario = 1000, subtotal = 3000),
            "Polera Kaiju"
        )
        every { ventaDao.observarVentas() } returns flowOf(listOf(venta))
        coEvery { ventaDao.ventaConUsuario(50) } returns venta
        coEvery { ventaDao.detalleConProducto(50) } returns listOf(detalle)

        runTest {
            repo.observarVentas().first() shouldBe listOf(venta)
            repo.obtenerVenta(50) shouldBe venta
            repo.detalleDeVenta(50) shouldBe listOf(detalle)
        }
    }

    "una venta guarda venta, detalle, stock y un movimiento por producto (RF-15)" {
        val venta = slot<Venta>()
        val detalles = slot<List<DetalleVenta>>()
        val movimientos = mutableListOf<MovimientoInventario>()
        coEvery { productoDao.buscarPorId(1) } returns Datos.producto(id = 1, stock = 10, precio = 1000)
        coEvery { productoDao.buscarPorId(2) } returns Datos.producto(id = 2, stock = 4, precio = 2500)
        coEvery { ventaDao.insertarVenta(capture(venta)) } returns 50L
        coEvery { ventaDao.insertarDetalles(capture(detalles)) } just Runs
        coEvery { movimientoDao.insertar(capture(movimientos)) } returns 1L

        runTest {
            repo.registrarVenta(listOf(LineaVenta(1, 3), LineaVenta(2, 2)), Datos.admin) shouldBe
                    ResultadoVenta.Ok(50L, 8000)
        }

        venta.captured.total shouldBe 8000
        venta.captured.usuarioId shouldBe Datos.admin.id
        detalles.captured.map { it.subtotal } shouldBe listOf(3000, 5000)
        detalles.captured.map { it.precioUnitario } shouldBe listOf(1000, 2500) // RN-06
        coVerify { productoDao.actualizarStock(1, 7) }
        coVerify { productoDao.actualizarStock(2, 2) }
        movimientos shouldHaveSize 2
        movimientos.all { it.tipo == TipoMovimiento.VENTA && it.ventaId == 50L } shouldBe true
        movimientos[0].motivo shouldBe "Venta N° 50"
    }

    "si un producto no tiene stock suficiente se cancela toda la venta (RN-07)" {
        coEvery { productoDao.buscarPorId(1) } returns Datos.producto(id = 1, stock = 10)
        coEvery { productoDao.buscarPorId(2) } returns Datos.producto(id = 2, nombre = "Gorro", stock = 1)

        runTest {
            repo.registrarVenta(listOf(LineaVenta(1, 3), LineaVenta(2, 2)), Datos.admin) shouldBe
                    ResultadoVenta.Error("Gorro: Stock insuficiente: disponible 1")
        }
        coVerify(exactly = 0) { ventaDao.insertarVenta(any()) }
        coVerify(exactly = 0) { productoDao.actualizarStock(any(), any()) }
    }

    "un cliente no puede registrar ventas (RN-08)" {
        runTest {
            repo.registrarVenta(listOf(LineaVenta(1, 1)), Datos.cliente) shouldBe
                    ResultadoVenta.Error("No tienes permisos para registrar ventas")
        }
    }

    "no se puede confirmar una venta vacía (RF-14)" {
        runTest {
            repo.registrarVenta(emptyList(), Datos.admin) shouldBe
                    ResultadoVenta.Error("Agrega al menos un producto a la venta")
        }
    }

    "una venta con un producto inexistente o desactivado se rechaza" {
        coEvery { productoDao.buscarPorId(1) } returns null
        coEvery { productoDao.buscarPorId(2) } returns Datos.producto(id = 2, nombre = "Taza", activo = false)
        runTest {
            repo.registrarVenta(listOf(LineaVenta(1, 1)), Datos.admin) shouldBe
                    ResultadoVenta.Error("Uno de los productos ya no existe")
            repo.registrarVenta(listOf(LineaVenta(2, 1)), Datos.admin) shouldBe
                    ResultadoVenta.Error("Taza está desactivado")
        }
    }
})