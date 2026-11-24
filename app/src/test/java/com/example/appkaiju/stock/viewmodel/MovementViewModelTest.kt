@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.appkaiju.stock.viewmodel

import com.example.appkaiju.stock.Datos
import com.example.appkaiju.stock.model.EstadoStock
import com.example.appkaiju.stock.model.TipoMovimiento
import com.example.appkaiju.stock.repository.InventarioRepository
import com.example.appkaiju.stock.repository.ResultadoMovimiento
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

class MovementViewModelTest : StringSpec({

    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    // Producto con 5 unidades y mínimo 2
    val producto = Datos.producto(id = 1, stock = 5, minimo = 2)

    fun repositorio(): InventarioRepository = mockk<InventarioRepository>().also {
        every { it.observarProducto(1) } returns flowOf(producto)
        coEvery { it.registrarMovimiento(any(), any(), any(), any(), any()) } returns
                ResultadoMovimiento.Ok(8, EstadoStock.NORMAL)
    }

    "al crearse carga el producto" {
        MovementViewModel(repositorio(), 1, TipoMovimiento.ENTRADA).estado.value.producto shouldBe producto
    }

    "una entrada válida se registra con su motivo (RF-09)" {
        val repo = repositorio()
        val vm = MovementViewModel(repo, 1, TipoMovimiento.ENTRADA)
        vm.onCantidadChange("3")
        vm.onMotivoChange("Compra")
        vm.estado.value.stockResultante shouldBe 8

        vm.guardar(Datos.admin)

        coVerify { repo.registrarMovimiento(1, TipoMovimiento.ENTRADA, 3, "Compra", Datos.admin) }
        vm.estado.value.guardadoOk shouldBe true
        vm.estado.value.mensajeExito shouldBe "Movimiento registrado. Nuevo stock: 8"
    }

    "no se registra dos veces el mismo movimiento" {
        val repo = repositorio()
        val vm = MovementViewModel(repo, 1, TipoMovimiento.ENTRADA)
        vm.onCantidadChange("3")
        vm.onMotivoChange("Compra")
        vm.guardar(Datos.admin)
        vm.guardar(Datos.admin)
        coVerify(exactly = 1) { repo.registrarMovimiento(any(), any(), any(), any(), any()) }
    }

    "sin cantidad ni motivo muestra los errores" {
        val repo = repositorio()
        val vm = MovementViewModel(repo, 1, TipoMovimiento.ENTRADA)
        vm.guardar(Datos.admin)

        vm.estado.value.errores[CampoMovimiento.CANTIDAD] shouldBe "Ingresa una cantidad entera de al menos 1"
        vm.estado.value.errores[CampoMovimiento.MOTIVO] shouldBe "Selecciona un motivo"
        coVerify(exactly = 0) { repo.registrarMovimiento(any(), any(), any(), any(), any()) }
    }

    "el motivo «Otro» exige una descripción de al menos 5 caracteres" {
        val repo = repositorio()
        val vm = MovementViewModel(repo, 1, TipoMovimiento.ENTRADA)
        vm.onCantidadChange("2")
        vm.onMotivoChange(MotivosMovimiento.OTRO)
        vm.onMotivoTextoChange("abc")
        vm.estado.value.requiereTexto shouldBe true
        vm.guardar(Datos.admin)
        vm.estado.value.errores[CampoMovimiento.MOTIVO_TEXTO] shouldBe "Escribe al menos 5 caracteres"

        vm.onMotivoTextoChange("Regalo de proveedor")
        vm.guardar(Datos.admin)
        coVerify { repo.registrarMovimiento(1, TipoMovimiento.ENTRADA, 2, "Otro: Regalo de proveedor", Datos.admin) }
    }

    "una salida mayor al stock se rechaza en el formulario (RF-10)" {
        val vm = MovementViewModel(repositorio(), 1, TipoMovimiento.SALIDA)
        vm.onCantidadChange("9")
        vm.onMotivoChange("Merma")
        vm.estado.value.stockResultante.shouldBeNull()
        vm.guardar(Datos.admin)
        vm.estado.value.errores[CampoMovimiento.CANTIDAD] shouldBe "Stock insuficiente: disponible 5"
    }

    "un ajuste acepta conteo 0, muestra la diferencia y guarda el motivo del conteo (RF-11)" {
        val repo = repositorio()
        val vm = MovementViewModel(repo, 1, TipoMovimiento.AJUSTE)
        vm.onCantidadChange("0")
        vm.onMotivoTextoChange("Conteo mensual")

        vm.estado.value.diferencia shouldBe -5
        vm.estado.value.stockResultante shouldBe 0
        vm.guardar(Datos.admin)

        coVerify { repo.registrarMovimiento(1, TipoMovimiento.AJUSTE, 0, "Conteo físico: Conteo mensual", Datos.admin) }
    }

    "un ajuste vacío o igual al stock registrado se rechaza" {
        val vm = MovementViewModel(repositorio(), 1, TipoMovimiento.AJUSTE)
        vm.guardar(Datos.admin)
        vm.estado.value.errores[CampoMovimiento.CANTIDAD] shouldBe "Ingresa el stock contado (puede ser 0)"

        vm.onCantidadChange("5")
        vm.onMotivoTextoChange("Conteo mensual")
        vm.guardar(Datos.admin)
        vm.estado.value.errores[CampoMovimiento.CANTIDAD] shouldBe "No hay diferencia con el stock registrado"
    }

    "si el repositorio rechaza el movimiento se muestra el mensaje" {
        val repo = repositorio()
        coEvery { repo.registrarMovimiento(any(), any(), any(), any(), any()) } returns
                ResultadoMovimiento.Error("No tienes permisos para registrar movimientos")
        val vm = MovementViewModel(repo, 1, TipoMovimiento.ENTRADA)
        vm.onCantidadChange("1")
        vm.onMotivoChange("Compra")
        vm.guardar(Datos.cliente)

        vm.estado.value.errorGeneral shouldBe "No tienes permisos para registrar movimientos"
        vm.estado.value.guardadoOk shouldBe false
    }

    "la cantidad solo acepta hasta 6 dígitos y el motivo hasta 80 caracteres" {
        val vm = MovementViewModel(repositorio(), 1, TipoMovimiento.ENTRADA)
        vm.onCantidadChange("12a34567")
        vm.onMotivoTextoChange("x".repeat(100))
        vm.estado.value.cantidad shouldBe "123456"
        vm.estado.value.motivoTexto.length shouldBe 80
    }

    "sin producto cargado no se calcula nada ni se guarda" {
        val repo = mockk<InventarioRepository>()
        every { repo.observarProducto(1) } returns flowOf(null)
        val vm = MovementViewModel(repo, 1, TipoMovimiento.ENTRADA)
        vm.onCantidadChange("2")
        vm.onMotivoChange("Compra")

        vm.estado.value.stockResultante.shouldBeNull()
        vm.estado.value.diferencia.shouldBeNull()
        vm.guardar(Datos.admin)
        coVerify(exactly = 0) { repo.registrarMovimiento(any(), any(), any(), any(), any()) }
    }

    "los motivos dependen del tipo de movimiento" {
        MotivosMovimiento.para(TipoMovimiento.ENTRADA) shouldContain MotivosMovimiento.OTRO
        MotivosMovimiento.para(TipoMovimiento.SALIDA) shouldContain "Merma"
        MotivosMovimiento.para(TipoMovimiento.AJUSTE).shouldBeEmpty()
    }
})