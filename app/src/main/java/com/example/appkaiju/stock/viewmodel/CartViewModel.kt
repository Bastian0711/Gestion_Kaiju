package com.example.appkaiju.stock.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appkaiju.stock.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ItemCarrito(val producto: Producto, val cantidad: Int) {
    val subtotal: Int get() = producto.precio * cantidad
}

data class CartState(
    val items: List<ItemCarrito> = emptyList(),
    val compraConfirmada: Boolean = false,
    val totalConfirmado: Int = 0
) {
    val total: Int get() = items.sumOf { it.subtotal }
    val cantidadTotal: Int get() = items.sumOf { it.cantidad }
}

class CartViewModel : ViewModel() {

    private val _estado = MutableStateFlow(CartState())
    val estado: StateFlow<CartState> = _estado.asStateFlow()

    fun agregar(producto: Producto) {
        _estado.update { s ->
            val existente = s.items.find { it.producto.id == producto.id }
            when {
                producto.stockActual <= 0 -> s
                existente == null ->
                    s.copy(items = s.items + ItemCarrito(producto, 1), compraConfirmada = false)
                existente.cantidad >= producto.stockActual -> s
                else -> s.copy(
                    items = s.items.map {
                        if (it.producto.id == producto.id) it.copy(cantidad = it.cantidad + 1) else it
                    }
                )
            }
        }
    }

    fun cambiarCantidad(productoId: Long, delta: Int) {
        _estado.update { s ->
            s.copy(
                items = s.items.mapNotNull { item ->
                    if (item.producto.id != productoId) {
                        item
                    } else {
                        val nueva = item.cantidad + delta
                        when {
                            nueva < 1 -> null
                            nueva > item.producto.stockActual -> item
                            else -> item.copy(cantidad = nueva)
                        }
                    }
                }
            )
        }
    }

    fun quitar(productoId: Long) {
        _estado.update { s -> s.copy(items = s.items.filter { it.producto.id != productoId }) }
    }

    fun confirmar() {
        _estado.update { s ->
            if (s.items.isEmpty()) s
            else s.copy(compraConfirmada = true, totalConfirmado = s.total, items = emptyList())
        }
    }

    fun limpiar() {
        _estado.value = CartState()
    }
}