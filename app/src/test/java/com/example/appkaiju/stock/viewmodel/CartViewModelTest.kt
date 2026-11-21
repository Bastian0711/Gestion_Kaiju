package com.example.appkaiju.stock.viewmodel

import com.example.appkaiju.stock.Datos
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

class CartViewModelTest : StringSpec({

    val taza = Datos.producto(id = 1, nombre = "Taza", stock = 2, precio = 6990)
    val llavero = Datos.producto(id = 2, nombre = "Llavero", stock = 50, precio = 2990)
    val agotado = Datos.producto(id = 3, stock = 0)

    "agregar suma unidades sin superar el stock (RF-22)" {
        val vm = CartViewModel()
        repeat(3) { vm.agregar(taza) }
        vm.agregar(llavero)
        vm.agregar(agotado)

        vm.estado.value.items.map { it.producto.id to it.cantidad } shouldBe listOf(1L to 2, 2L to 1)
        vm.estado.value.cantidadTotal shouldBe 3
        vm.estado.value.total shouldBe 2 * 6990 + 2990
    }

    "cambiarCantidad respeta el stock y quita el producto al llegar a 0" {
        val vm = CartViewModel()
        vm.agregar(taza)
        vm.agregar(llavero)
        vm.cambiarCantidad(1, +1)
        vm.cambiarCantidad(1, +1)   // superaría el stock de 2
        vm.estado.value.items.first { it.producto.id == 1L }.cantidad shouldBe 2

        vm.cambiarCantidad(2, -1)
        vm.estado.value.items.map { it.producto.id } shouldBe listOf(1L)

        vm.quitar(1)
        vm.estado.value.items.shouldBeEmpty()
    }

    "confirmar guarda el total, vacía el carrito y no descuenta stock (RF-23, RN-10)" {
        val vm = CartViewModel()
        vm.agregar(llavero)
        vm.agregar(llavero)
        vm.confirmar()

        vm.estado.value.compraConfirmada shouldBe true
        vm.estado.value.totalConfirmado shouldBe 5980
        vm.estado.value.items.shouldBeEmpty()
        llavero.stockActual shouldBe 50
    }

    "confirmar un carrito vacío no hace nada y limpiar reinicia el estado" {
        val vm = CartViewModel()
        vm.confirmar()
        vm.estado.value.compraConfirmada shouldBe false

        vm.agregar(taza)
        vm.limpiar()
        vm.estado.value shouldBe CartState()
    }
})