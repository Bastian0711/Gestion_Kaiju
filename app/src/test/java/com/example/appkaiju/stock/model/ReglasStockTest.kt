package com.example.appkaiju.stock.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ReglasStockTest : StringSpec({

    "una entrada suma la cantidad al stock" {
        ReglasStock.aplicar(TipoMovimiento.ENTRADA, 10, 5) shouldBe ReglasStock.Resultado.Ok(15, 5)
    }

    "una anulación de venta devuelve el stock" {
        ReglasStock.aplicar(TipoMovimiento.ANULACION_VENTA, 2, 3) shouldBe ReglasStock.Resultado.Ok(5, 3)
    }

    "una entrada con cantidad menor a 1 se rechaza" {
        ReglasStock.aplicar(TipoMovimiento.ENTRADA, 10, 0) shouldBe
                ReglasStock.Resultado.Rechazado("La cantidad debe ser al menos 1")
    }

    "una salida resta la cantidad al stock" {
        ReglasStock.aplicar(TipoMovimiento.SALIDA, 10, 4) shouldBe ReglasStock.Resultado.Ok(6, 4)
    }

    "una venta puede dejar el stock en cero" {
        ReglasStock.aplicar(TipoMovimiento.VENTA, 3, 3) shouldBe ReglasStock.Resultado.Ok(0, 3)
    }

    "una salida con cantidad menor a 1 se rechaza" {
        ReglasStock.aplicar(TipoMovimiento.SALIDA, 10, 0) shouldBe
                ReglasStock.Resultado.Rechazado("La cantidad debe ser al menos 1")
    }

    "una salida mayor al stock se rechaza (RN-04)" {
        ReglasStock.aplicar(TipoMovimiento.VENTA, 2, 3) shouldBe
                ReglasStock.Resultado.Rechazado("Stock insuficiente: disponible 2")
    }

    "un ajuste deja el stock en lo contado y guarda la diferencia" {
        ReglasStock.aplicar(TipoMovimiento.AJUSTE, 10, 7) shouldBe ReglasStock.Resultado.Ok(7, -3)
        ReglasStock.aplicar(TipoMovimiento.AJUSTE, 10, 12) shouldBe ReglasStock.Resultado.Ok(12, 2)
    }

    "un ajuste con conteo negativo se rechaza" {
        ReglasStock.aplicar(TipoMovimiento.AJUSTE, 10, -1) shouldBe
                ReglasStock.Resultado.Rechazado("El stock contado no puede ser negativo")
    }

    "un ajuste igual al stock registrado se rechaza" {
        ReglasStock.aplicar(TipoMovimiento.AJUSTE, 10, 10) shouldBe
                ReglasStock.Resultado.Rechazado("No hay diferencia con el stock registrado")
    }
})