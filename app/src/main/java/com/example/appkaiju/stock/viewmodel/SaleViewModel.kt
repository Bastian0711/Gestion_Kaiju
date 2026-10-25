package com.example.appkaiju.stock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appkaiju.stock.model.Producto
import com.example.appkaiju.stock.model.ReglasAlertaStock
import com.example.appkaiju.stock.model.Usuario
import com.example.appkaiju.stock.repository.LineaVenta
import com.example.appkaiju.stock.repository.ProductoRepository
import com.example.appkaiju.stock.repository.ResultadoVenta
import com.example.appkaiju.stock.repository.VentaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SaleState(
    val texto: String = "",
    val resultados: List<Producto> = emptyList(),
    val lineas: List<ItemCarrito> = emptyList(),
    val errorGeneral: String? = null,
    val guardando: Boolean = false,
    val ventaRegistradaId: Long? = null
) {
    val total: Int get() = lineas.sumOf { it.subtotal }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SaleViewModel(
    productos: ProductoRepository,
    private val ventas: VentaRepository,
    private val alertas: MutableSharedFlow<Producto>
) : ViewModel() {

    private val _estado = MutableStateFlow(SaleState())
    val estado: StateFlow<SaleState> = _estado.asStateFlow()

    private val textoBusqueda = MutableStateFlow("")

    init {
        viewModelScope.launch {
            textoBusqueda.flatMapLatest { productos.buscar(it.trim()) }.collect { lista ->
                _estado.update { it.copy(resultados = lista.filter { p -> p.stockActual > 0 }) }
            }
        }
    }

    fun onTextoChange(valor: String) {
        textoBusqueda.value = valor
        _estado.update { it.copy(texto = valor) }
    }

    fun agregar(producto: Producto) {
        _estado.update { s ->
            val existente = s.lineas.find { it.producto.id == producto.id }
            val nuevas = when {
                producto.stockActual <= 0 -> s.lineas
                existente == null -> s.lineas + ItemCarrito(producto, 1)
                existente.cantidad >= producto.stockActual -> s.lineas
                else -> s.lineas.map {
                    if (it.producto.id == producto.id) it.copy(cantidad = it.cantidad + 1) else it
                }
            }
            s.copy(lineas = nuevas, errorGeneral = null)
        }
    }

    fun cambiarCantidad(productoId: Long, delta: Int) {
        _estado.update { s ->
            s.copy(
                errorGeneral = null,
                lineas = s.lineas.mapNotNull { item ->
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
        _estado.update { s -> s.copy(lineas = s.lineas.filter { it.producto.id != productoId }) }
    }

    fun confirmar(usuario: Usuario) {
        val s = _estado.value
        if (s.guardando) return
        if (s.lineas.isEmpty()) {
            _estado.update { it.copy(errorGeneral = "Agrega al menos un producto a la venta") }
            return
        }
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, errorGeneral = null) }
            val lineas = s.lineas.map { LineaVenta(it.producto.id, it.cantidad) }
            when (val r = ventas.registrarVenta(lineas, usuario)) {
                is ResultadoVenta.Ok -> {
                    s.lineas.forEach { linea ->
                        val despues = linea.producto.copy(
                            stockActual = linea.producto.stockActual - linea.cantidad
                        )
                        if (ReglasAlertaStock.empeora(linea.producto, despues)) {
                            alertas.tryEmit(despues)
                        }
                    }
                    _estado.update {
                        it.copy(guardando = false, lineas = emptyList(), ventaRegistradaId = r.ventaId)
                    }
                }
                is ResultadoVenta.Error -> _estado.update {
                    it.copy(guardando = false, errorGeneral = r.mensaje)
                }
            }
        }
    }
}