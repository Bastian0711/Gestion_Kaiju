package com.example.appkaiju.stock.repository

import com.example.appkaiju.stock.Datos
import com.example.appkaiju.stock.data.CategoriaDao
import com.example.appkaiju.stock.data.ProductoDao
import com.example.appkaiju.stock.model.Categoria
import com.example.appkaiju.stock.model.EstadoStock
import com.example.appkaiju.stock.model.Producto
import com.example.appkaiju.stock.model.ProductoConCategoria
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class ProductoRepositoryTest : StringSpec({

    "buscar, catalogo y categorias entregan los datos del DAO" {
        val productoDao = mockk<ProductoDao>()
        val categoriaDao = mockk<CategoriaDao>()
        val p = Datos.producto()
        val conCategoria = ProductoConCategoria(p, "Vestuario")
        val categoria = Categoria(id = 1, nombre = "Vestuario")
        every { productoDao.buscar("pol") } returns flowOf(listOf(p))
        every { productoDao.observarCatalogoConCategoria("pol") } returns flowOf(listOf(conCategoria))
        every { categoriaDao.observarTodas() } returns flowOf(listOf(categoria))
        val repo = ProductoRepository(productoDao, categoriaDao)

        runTest {
            repo.buscar("pol").first() shouldBe listOf(p)
            repo.catalogo("pol").first() shouldBe listOf(conCategoria)
            repo.categorias().first() shouldBe listOf(categoria)
        }
    }

    "crearDatosIniciales carga 4 categorías y 12 productos con estados normal, bajo y agotado (RF-25)" {
        val productoDao = mockk<ProductoDao>()
        val categoriaDao = mockk<CategoriaDao>()
        val productos = mutableListOf<Producto>()
        coEvery { productoDao.contar() } returns 0
        coEvery { categoriaDao.insertar(any()) } returnsMany listOf(1L, 2L, 3L, 4L)
        coEvery { productoDao.insertar(capture(productos)) } returns 1L

        runTest { ProductoRepository(productoDao, categoriaDao).crearDatosIniciales() }

        coVerify(exactly = 4) { categoriaDao.insertar(any()) }
        productos shouldHaveSize 12
        productos.map { it.codigo }.toSet() shouldHaveSize 12
        productos.map { it.estadoStock() }.toSet() shouldBe
                setOf(EstadoStock.NORMAL, EstadoStock.BAJO, EstadoStock.AGOTADO)
    }

    "crearDatosIniciales no duplica datos si ya existen productos" {
        val productoDao = mockk<ProductoDao>()
        val categoriaDao = mockk<CategoriaDao>()
        coEvery { productoDao.contar() } returns 12

        runTest { ProductoRepository(productoDao, categoriaDao).crearDatosIniciales() }

        coVerify(exactly = 0) { categoriaDao.insertar(any()) }
        coVerify(exactly = 0) { productoDao.insertar(any()) }
    }
})