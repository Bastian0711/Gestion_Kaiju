@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.appkaiju.stock.viewmodel

import com.example.appkaiju.stock.Datos
import com.example.appkaiju.stock.model.Producto
import com.example.appkaiju.stock.repository.LineaVenta
import com.example.appkaiju.stock.repository.ProductoRepository
import com.example.appkaiju.stock.repository.ResultadoVenta
import com.example.appkaiju.stock.repository.VentaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

class SaleViewModelTest : StringSpec({

    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    val polera = Datos.producto(id = 1, nombre = "Polera", stock = 5, minimo = 3, precio = 1000)
    val gorro = Datos.producto(id = 2, nombre = "Gorro", stock = 2, minimo = 2, precio = 2500)
    val agotado = Datos.producto(id = 3, nombre = "Polerón", stock = 0, minimo = 1)

    fun productos(): ProductoRepository = mockk<ProductoRepository>().also {
        every { it.buscar(any()) } returns flowOf(listOf(polera, gorro, agotado))
    }

    "el buscador solo ofrece productos con stock (RF-14)" {
        val repo = productos()
        val vm = SaleViewModel(repo, mockk(), MutableSharedFlow())
        vm.estado.value.resultados shouldBe listOf(polera, gorro)

        vm.onTextoChange("  pol ")
        vm.estado.value.texto shouldBe "  pol "
        verify { repo.buscar("pol") }
    }

    "agregar suma unidades sin superar el stock y calcula el total" {
        val vm = SaleViewModel(productos(), mockk(), MutableSharedFlow())
        vm.agregar(gorro)
        vm.agregar(gorro)
        vm.agregar(gorro)      // stock 2: no sube a 3
        vm.agregar(polera)
        vm.agregar(agotado)    // sin stock: se ignora

        vm.estado.value.lineas.map { it.producto.id to it.cantidad } shouldBe listOf(2L to 2, 1L to 1)
        vm.estado.value.total shouldBe 6000
    }

    "cambiarCantidad respeta el stock y elimina la línea al llegar a 0" {
        val vm = SaleViewModel(productos(), mockk(), MutableSharedFlow())
        vm.agregar(gorro)
        vm.cambiarCantidad(2, +1)
        vm.cambiarCantidad(2, +1)   // superaría el stock
        vm.estado.value.lineas.first().cantidad shouldBe 2

        vm.agregar(polera)
        vm.cambiarCantidad(1, -1)
        vm.estado.value.lineas.map { it.producto.id } shouldBe listOf(2L)

        vm.quitar(2)
        vm.estado.value.lineas.shouldBeEmpty()
    }

    "no se puede confirmar una venta vacía" {
        val ventas = mockk<VentaRepository>()
        val vm = SaleViewModel(productos(), ventas, MutableSharedFlow())
        vm.confirmar(Datos.admin)

        vm.estado.value.errorGeneral shouldBe "Agrega al menos un producto a la venta"
        coVerify(exactly = 0) { ventas.registrarVenta(any(), any()) }
    }

    "al confirmar registra la venta, vacía las líneas y avisa los productos que quedan con stock bajo" {
        val ventas = mockk<VentaRepository>()
        coEvery { ventas.registrarVenta(listOf(LineaVenta(1, 3), LineaVenta(2, 1)), Datos.admin) } returns
                ResultadoVenta.Ok(ventaId = 10, total = 5500)
        val alertas = MutableSharedFlow<Producto>(replay = 5)
        val vm = SaleViewModel(productos(), ventas, alertas)
        repeat(3) { vm.agregar(polera) }  // 5 -> 2: pasa de NORMAL a BAJO
        vm.agregar(gorro)                 // 2 -> 1: ya era BAJO, no avisa

        vm.confirmar(Datos.admin)

        vm.estado.value.ventaRegistradaId shouldBe 10L
        vm.estado.value.lineas.shouldBeEmpty()
        vm.estado.value.guardando shouldBe false
        alertas.replayCache.map { it.id } shouldBe listOf(1L)
    }

    "si la venta falla se muestra el error y se conservan las líneas" {
        val ventas = mockk<VentaRepository>()
        coEvery { ventas.registrarVenta(any(), any()) } returns ResultadoVenta.Error("Polera: Stock insuficiente: disponible 1")
        val vm = SaleViewModel(productos(), ventas, MutableSharedFlow())
        vm.agregar(polera)
        vm.confirmar(Datos.admin)

        vm.estado.value.errorGeneral shouldBe "Polera: Stock insuficiente: disponible 1"
        vm.estado.value.lineas.size shouldBe 1
        vm.estado.value.ventaRegistradaId.shouldBeNull()
    }
})