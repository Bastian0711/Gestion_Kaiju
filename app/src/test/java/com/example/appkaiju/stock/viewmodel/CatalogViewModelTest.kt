@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.appkaiju.stock.viewmodel

import com.example.appkaiju.stock.Datos
import com.example.appkaiju.stock.model.Categoria
import com.example.appkaiju.stock.model.ProductoConCategoria
import com.example.appkaiju.stock.repository.ProductoRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
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

class CatalogViewModelTest : StringSpec({

    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    "el catálogo muestra los productos y permite filtrar por categoría y texto (RF-21)" {
        val polera = ProductoConCategoria(Datos.producto(id = 1, nombre = "Polera", categoriaId = 1), "Vestuario")
        val taza = ProductoConCategoria(Datos.producto(id = 2, nombre = "Taza", categoriaId = 2), "Accesorios")
        val categorias = listOf(Categoria(id = 1, nombre = "Vestuario"), Categoria(id = 2, nombre = "Accesorios"))
        val repo = mockk<ProductoRepository>()
        every { repo.catalogo(any()) } returns flowOf(listOf(polera, taza))
        every { repo.categorias() } returns flowOf(categorias)

        runTest {
            val vm = CatalogViewModel(repo)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.estado.collect {} }
            advanceUntilIdle()
            vm.estado.value.productos shouldBe listOf(polera, taza)
            vm.estado.value.categorias shouldBe categorias

            vm.onCategoriaChange(1)
            advanceUntilIdle()
            vm.estado.value.categoriaId shouldBe 1L
            vm.estado.value.productos shouldBe listOf(polera)

            vm.onCategoriaChange(null)
            vm.onTextoChange(" taza ")
            advanceUntilIdle()
            vm.estado.value.texto shouldBe " taza "
            vm.estado.value.productos shouldBe listOf(polera, taza)
            verify { repo.catalogo("taza") }
        }
    }
})