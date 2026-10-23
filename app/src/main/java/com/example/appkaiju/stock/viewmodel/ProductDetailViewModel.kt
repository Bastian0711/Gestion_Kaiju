package com.example.appkaiju.stock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appkaiju.stock.model.MovimientoInventario
import com.example.appkaiju.stock.model.Producto
import com.example.appkaiju.stock.repository.InventarioRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ProductDetailState(
    val producto: Producto? = null,
    val ultimosMovimientos: List<MovimientoInventario> = emptyList(),
    val cargando: Boolean = true
)

class ProductDetailViewModel(
    repositorio: InventarioRepository,
    productoId: Long
) : ViewModel() {

    val estado: StateFlow<ProductDetailState> = combine(
        repositorio.observarProducto(productoId),
        repositorio.observarMovimientos(productoId)
    ) { producto, movimientos ->
        ProductDetailState(
            producto = producto,
            ultimosMovimientos = movimientos.take(5),
            cargando = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductDetailState())
}