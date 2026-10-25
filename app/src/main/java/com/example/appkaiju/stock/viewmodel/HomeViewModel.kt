package com.example.appkaiju.stock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appkaiju.stock.model.EstadoStock
import com.example.appkaiju.stock.model.EstadoVenta
import com.example.appkaiju.stock.model.Producto
import com.example.appkaiju.stock.repository.ProductoRepository
import com.example.appkaiju.stock.repository.VentaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class ResumenState(
    val productosActivos: Int = 0,
    val stockBajo: Int = 0,
    val agotados: Int = 0,
    val ventasHoy: Int = 0,
    val montoHoy: Int = 0,
    val reposicion: List<Producto> = emptyList()
)

class HomeViewModel(
    productos: ProductoRepository,
    ventas: VentaRepository
) : ViewModel() {

    val resumen: StateFlow<ResumenState> = combine(
        productos.buscar(""),
        ventas.observarVentas()
    ) { lista, listaVentas ->
        val inicioDia = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val ventasDeHoy = listaVentas.filter {
            it.venta.estado == EstadoVenta.ACTIVA && it.venta.fechaHora >= inicioDia
        }
        val agotados = lista.count { it.estadoStock() == EstadoStock.AGOTADO }
        val bajos = lista.count { it.estadoStock() == EstadoStock.BAJO }

        ResumenState(
            productosActivos = lista.size,
            stockBajo = bajos,
            agotados = agotados,
            ventasHoy = ventasDeHoy.size,
            montoHoy = ventasDeHoy.sumOf { it.venta.total },
            reposicion = lista
                .filter { it.estadoStock() != EstadoStock.NORMAL }
                .sortedWith(compareBy({ it.stockActual }, { it.nombre }))
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ResumenState())
}