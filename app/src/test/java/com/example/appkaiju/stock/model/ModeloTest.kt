package com.example.appkaiju.stock.model

import com.example.appkaiju.stock.Datos
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class ModeloTest : StringSpec({

    "un producto con stock 0 está agotado" {
        Datos.producto(stock = 0, minimo = 3).estadoStock() shouldBe EstadoStock.AGOTADO
    }

    "un producto con stock menor o igual al mínimo está bajo" {
        Datos.producto(stock = 3, minimo = 3).estadoStock() shouldBe EstadoStock.BAJO
        Datos.producto(stock = 1, minimo = 3).estadoStock() shouldBe EstadoStock.BAJO
    }

    "un producto con stock sobre el mínimo está normal" {
        Datos.producto(stock = 4, minimo = 3).estadoStock() shouldBe EstadoStock.NORMAL
    }

    "empeora detecta el paso de normal a bajo y de bajo a agotado" {
        val normal = Datos.producto(stock = 10, minimo = 3)
        ReglasAlertaStock.empeora(normal, normal.copy(stockActual = 2)) shouldBe true
        ReglasAlertaStock.empeora(normal.copy(stockActual = 2), normal.copy(stockActual = 0)) shouldBe true
    }

    "empeora no avisa si el estado se mantiene o mejora" {
        val bajo = Datos.producto(stock = 2, minimo = 3)
        ReglasAlertaStock.empeora(bajo, bajo.copy(stockActual = 1)) shouldBe false
        ReglasAlertaStock.empeora(bajo, bajo.copy(stockActual = 20)) shouldBe false
    }

    "productosParaAlertar solo incluye los que empeoraron y tenían estado previo" {
        val a = Datos.producto(id = 1, stock = 2, minimo = 3)   // NORMAL -> BAJO
        val b = Datos.producto(id = 2, stock = 10, minimo = 3)  // NORMAL -> NORMAL
        val c = Datos.producto(id = 3, stock = 0, minimo = 3)   // sin estado previo
        val previo = mapOf(1L to EstadoStock.NORMAL, 2L to EstadoStock.NORMAL)

        ReglasAlertaStock.productosParaAlertar(previo, listOf(a, b, c)) shouldContainExactly listOf(a)
    }

    "convierte pesos a dólares con el valor del dólar" {
        ConversorMoneda.clpADolares(9500, 950.0).shouldNotBeNull() shouldBe (10.0 plusOrMinus 0.001)
    }

    "sin un valor de dólar válido no hay conversión" {
        ConversorMoneda.clpADolares(9500, null).shouldBeNull()
        ConversorMoneda.clpADolares(9500, 0.0).shouldBeNull()
    }
})