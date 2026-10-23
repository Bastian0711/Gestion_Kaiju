package com.example.appkaiju.stock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appkaiju.stock.model.MovimientoInventario
import com.example.appkaiju.stock.model.Producto
import com.example.appkaiju.stock.model.TipoMovimiento
import com.example.appkaiju.stock.repository.InventarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class KardexState(
    val producto: Producto? = null,
    val movimientos: List<MovimientoInventario> = emptyList(),
    val filtro: TipoMovimiento? = null,
    val nombresUsuarios: Map<Long, String> = emptyMap()
)

class KardexViewModel(
    private val repositorio: InventarioRepository,
    productoId: Long
) : ViewModel() {

    private val filtro = MutableStateFlow<TipoMovimiento?>(null)
    private val nombres = MutableStateFlow<Map<Long, String>>(emptyMap())

    init {
        viewModelScope.launch { nombres.value = repositorio.nombresUsuarios() }
    }

    val estado: StateFlow<KardexState> = combine(
        repositorio.observarProducto(productoId),
        repositorio.observarMovimientos(productoId),
        filtro,
        nombres
    ) { producto, movimientos, tipo, mapaNombres ->
        KardexState(
            producto = producto,
            movimientos = if (tipo == null) movimientos else movimientos.filter { it.tipo == tipo },
            filtro = tipo,
            nombresUsuarios = mapaNombres
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), KardexState())

    fun onFiltroChange(tipo: TipoMovimiento?) {
        filtro.value = tipo
    }
}