package com.example.appkaiju.stock.repository

import com.example.appkaiju.stock.Datos
import com.example.appkaiju.stock.SinTransaccion
import com.example.appkaiju.stock.data.CategoriaDao
import com.example.appkaiju.stock.data.MovimientoDao
import com.example.appkaiju.stock.data.ProductoDao
import com.example.appkaiju.stock.data.UsuarioDao
import com.example.appkaiju.stock.model.Categoria
import com.example.appkaiju.stock.model.EstadoStock
import com.example.appkaiju.stock.model.MovimientoInventario
import com.example.appkaiju.stock.model.TipoMovimiento
import io.kotest.core.spec.style.StringSpec
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

class InventarioRepositoryTest : StringSpec({

    lateinit var productoDao: ProductoDao
    lateinit var movimientoDao: MovimientoDao
    lateinit var categoriaDao: CategoriaDao
    lateinit var usuarioDao: UsuarioDao
    lateinit var repo: InventarioRepository

    beforeTest {
        productoDao = mockk()
        movimientoDao = mockk()
        categoriaDao = mockk()
        usuarioDao = mockk()
        repo = InventarioRepository(productoDao, movimientoDao, categoriaDao, usuarioDao, SinTransaccion)
        coEvery { productoDao.actualizarStock(any(), any()) } just Runs
        coEvery { movimientoDao.insertar(any()) } returns 1L
    }

    "las consultas entregan los datos de los DAO" {
        val p = Datos.producto(id = 5)
        val mov = MovimientoInventario(
            productoId = 5, tipo = TipoMovimiento.ENTRADA, cantidad = 2, stockResultante = 12,
            fechaHora = 1L, motivo = "Compra", usuarioId = 1
        )
        every { categoriaDao.observarTodas() } returns flowOf(listOf(Categoria(id = 1, nombre = "Vestuario")))
        every { productoDao.observarPorId(5) } returns flowOf(p)
        every { movimientoDao.observarPorProducto(5) } returns flowOf(listOf(mov))
        coEvery { usuarioDao.listar() } returns listOf(Datos.admin, Datos.cliente)
        coEvery { productoDao.buscarPorCodigo("COD-5") } returns p
        coEvery { productoDao.buscarPorCodigo("NO-EXISTE") } returns null

        runTest {
            repo.observarCategorias().first().map { it.nombre } shouldBe listOf("Vestuario")
            repo.observarProducto(5).first() shouldBe p
            repo.observarMovimientos(5).first() shouldBe listOf(mov)
            repo.nombresUsuarios() shouldBe mapOf(1L to "Administrador Kaiju", 2L to "Cliente de Prueba")
            repo.existeCodigo("COD-5") shouldBe true
            repo.existeCodigo("NO-EXISTE") shouldBe false
        }
    }

    "crearProducto con stock inicial registra el movimiento «Stock inicial» (RF-05)" {
        val movimiento = slot<MovimientoInventario>()
        coEvery { productoDao.insertar(any()) } returns 7L
        coEvery { movimientoDao.insertar(capture(movimiento)) } returns 1L

        runTest {
            repo.crearProducto(Datos.producto(id = 0, stock = 8), usuarioId = 1) shouldBe 7L
        }
        movimiento.captured.productoId shouldBe 7L
        movimiento.captured.tipo shouldBe TipoMovimiento.ENTRADA
        movimiento.captured.cantidad shouldBe 8
        movimiento.captured.motivo shouldBe "Stock inicial"
    }

    "crearProducto con stock 0 no registra movimientos" {
        coEvery { productoDao.insertar(any()) } returns 7L
        runTest { repo.crearProducto(Datos.producto(id = 0, stock = 0), usuarioId = 1) }
        coVerify(exactly = 0) { movimientoDao.insertar(any()) }
    }

    "una entrada actualiza el stock y deja el movimiento con su responsable (RF-09, RNF-05)" {
        val movimiento = slot<MovimientoInventario>()
        coEvery { productoDao.buscarPorId(1) } returns Datos.producto(id = 1, stock = 10, minimo = 3)
        coEvery { movimientoDao.insertar(capture(movimiento)) } returns 1L

        runTest {
            repo.registrarMovimiento(1, TipoMovimiento.ENTRADA, 5, "Compra", Datos.admin) shouldBe
                    ResultadoMovimiento.Ok(15, EstadoStock.NORMAL)
        }
        coVerify { productoDao.actualizarStock(1, 15) }
        movimiento.captured.stockResultante shouldBe 15
        movimiento.captured.usuarioId shouldBe Datos.admin.id
        movimiento.captured.motivo shouldBe "Compra"
    }

    "una salida que deja el stock bajo informa el nuevo estado (RF-10)" {
        coEvery { productoDao.buscarPorId(1) } returns Datos.producto(id = 1, stock = 5, minimo = 3)
        runTest {
            repo.registrarMovimiento(1, TipoMovimiento.SALIDA, 3, "Merma", Datos.admin) shouldBe
                    ResultadoMovimiento.Ok(2, EstadoStock.BAJO)
        }
    }

    "una salida mayor al stock se rechaza y no cambia nada (RN-04)" {
        coEvery { productoDao.buscarPorId(1) } returns Datos.producto(id = 1, stock = 2)
        runTest {
            repo.registrarMovimiento(1, TipoMovimiento.SALIDA, 5, "Merma", Datos.admin) shouldBe
                    ResultadoMovimiento.Error("Stock insuficiente: disponible 2")
        }
        coVerify(exactly = 0) { productoDao.actualizarStock(any(), any()) }
        coVerify(exactly = 0) { movimientoDao.insertar(any()) }
    }

    "un cliente no puede registrar movimientos (RN-08)" {
        runTest {
            repo.registrarMovimiento(1, TipoMovimiento.ENTRADA, 5, "Compra", Datos.cliente) shouldBe
                    ResultadoMovimiento.Error("No tienes permisos para registrar movimientos")
        }
        coVerify(exactly = 0) { productoDao.buscarPorId(any()) }
    }

    "las ventas no se registran como movimiento manual" {
        runTest {
            repo.registrarMovimiento(1, TipoMovimiento.VENTA, 1, "x", Datos.admin) shouldBe
                    ResultadoMovimiento.Error("Este movimiento se genera desde Ventas")
            repo.registrarMovimiento(1, TipoMovimiento.ANULACION_VENTA, 1, "x", Datos.admin) shouldBe
                    ResultadoMovimiento.Error("Este movimiento se genera desde Ventas")
        }
    }

    "no se registran movimientos de productos inexistentes o desactivados" {
        coEvery { productoDao.buscarPorId(1) } returns null
        coEvery { productoDao.buscarPorId(2) } returns Datos.producto(id = 2, activo = false)
        runTest {
            repo.registrarMovimiento(1, TipoMovimiento.ENTRADA, 1, "Compra", Datos.admin) shouldBe
                    ResultadoMovimiento.Error("El producto no existe")
            repo.registrarMovimiento(2, TipoMovimiento.ENTRADA, 1, "Compra", Datos.admin) shouldBe
                    ResultadoMovimiento.Error("El producto está desactivado")
        }
    }
})