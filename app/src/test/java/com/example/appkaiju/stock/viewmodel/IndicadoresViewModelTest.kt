@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.appkaiju.stock.viewmodel

import com.example.appkaiju.stock.data.model.Indicador
import com.example.appkaiju.stock.repository.IndicadoresRepository
import com.example.appkaiju.stock.repository.ResultadoIndicadores
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

class IndicadoresViewModelTest : StringSpec({

    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    val dolar = Indicador(codigo = "dolar", nombre = "Dólar observado", valor = 950.5)
    val uf = Indicador(codigo = "uf", nombre = "Unidad de fomento (UF)", valor = 39800.0)

    "al crearse carga los indicadores de la API externa" {
        val repo = mockk<IndicadoresRepository>()
        coEvery { repo.obtenerIndicadores() } returns ResultadoIndicadores.Ok(listOf(dolar, uf))

        val vm = IndicadoresViewModel(repo)

        vm.estado.value.indicadores shouldBe listOf(dolar, uf)
        vm.estado.value.valorDolar shouldBe 950.5
        vm.estado.value.cargando shouldBe false
        vm.estado.value.error.shouldBeNull()
    }

    "si la API falla muestra el error sin indicadores" {
        val repo = mockk<IndicadoresRepository>()
        coEvery { repo.obtenerIndicadores() } returns ResultadoIndicadores.Error("Sin conexión")

        val vm = IndicadoresViewModel(repo)

        vm.estado.value.error shouldBe "Sin conexión"
        vm.estado.value.indicadores.shouldBeEmpty()
        vm.estado.value.valorDolar.shouldBeNull()
    }

    "reintentar después de un error carga los datos" {
        val repo = mockk<IndicadoresRepository>()
        coEvery { repo.obtenerIndicadores() } returnsMany listOf(
            ResultadoIndicadores.Error("Sin conexión"),
            ResultadoIndicadores.Ok(listOf(dolar))
        )
        val vm = IndicadoresViewModel(repo)

        vm.cargar()

        vm.estado.value.error.shouldBeNull()
        vm.estado.value.indicadores shouldBe listOf(dolar)
        coVerify(exactly = 2) { repo.obtenerIndicadores() }
    }

    "un error al actualizar conserva los últimos valores obtenidos" {
        val repo = mockk<IndicadoresRepository>()
        coEvery { repo.obtenerIndicadores() } returnsMany listOf(
            ResultadoIndicadores.Ok(listOf(dolar, uf)),
            ResultadoIndicadores.Error("Sin conexión")
        )
        val vm = IndicadoresViewModel(repo)

        vm.cargar()

        vm.estado.value.indicadores shouldBe listOf(dolar, uf)
        vm.estado.value.error shouldBe "Sin conexión"
    }
})