package com.example.appkaiju.stock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appkaiju.stock.model.DetalleConProducto
import com.example.appkaiju.stock.model.VentaConUsuario
import com.example.appkaiju.stock.repository.VentaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SaleDetailState(
    val venta: VentaConUsuario? = null,
    val detalles: List<DetalleConProducto> = emptyList(),
    val cargando: Boolean = true
)

class SaleDetailViewModel(
    repositorio: VentaRepository,
    ventaId: Long
) : ViewModel() {

    private val _estado = MutableStateFlow(SaleDetailState())
    val estado: StateFlow<SaleDetailState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            val venta = repositorio.obtenerVenta(ventaId)
            val detalles = repositorio.detalleDeVenta(ventaId)
            _estado.value = SaleDetailState(venta = venta, detalles = detalles, cargando = false)
        }
    }
}