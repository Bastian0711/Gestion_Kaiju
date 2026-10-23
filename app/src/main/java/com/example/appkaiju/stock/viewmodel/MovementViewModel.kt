package com.example.appkaiju.stock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appkaiju.stock.model.Producto
import com.example.appkaiju.stock.model.ReglasStock
import com.example.appkaiju.stock.model.TipoMovimiento
import com.example.appkaiju.stock.model.Usuario
import com.example.appkaiju.stock.repository.InventarioRepository
import com.example.appkaiju.stock.repository.ResultadoMovimiento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object CampoMovimiento {
    const val CANTIDAD = "cantidad"
    const val MOTIVO = "motivo"
    const val MOTIVO_TEXTO = "motivoTexto"
}

object MotivosMovimiento {
    const val OTRO = "Otro"
    private val ENTRADA = listOf("Compra", "Devolución", "Reposición", OTRO)
    private val SALIDA = listOf("Consumo", "Merma", "Pérdida", "Uso interno")

    fun para(tipo: TipoMovimiento): List<String> = when (tipo) {
        TipoMovimiento.ENTRADA -> ENTRADA
        TipoMovimiento.SALIDA -> SALIDA
        else -> emptyList()
    }
}

data class MovementState(
    val producto: Producto? = null,
    val tipo: TipoMovimiento = TipoMovimiento.ENTRADA,
    val cantidad: String = "",
    val motivo: String? = null,
    val motivoTexto: String = "",
    val errores: Map<String, String> = emptyMap(),
    val errorGeneral: String? = null,
    val guardando: Boolean = false,
    val guardadoOk: Boolean = false,
    val mensajeExito: String? = null
) {
    val requiereTexto: Boolean
        get() = tipo == TipoMovimiento.AJUSTE || motivo == MotivosMovimiento.OTRO

    val stockResultante: Int?
        get() {
            val p = producto ?: return null
            val n = cantidad.toIntOrNull() ?: return null
            return when (val r = ReglasStock.aplicar(tipo, p.stockActual, n)) {
                is ReglasStock.Resultado.Ok -> r.stockResultante
                is ReglasStock.Resultado.Rechazado -> null
            }
        }

    val diferencia: Int?
        get() {
            if (tipo != TipoMovimiento.AJUSTE) return null
            val p = producto ?: return null
            val n = cantidad.toIntOrNull() ?: return null
            return n - p.stockActual
        }
}

class MovementViewModel(
    private val repositorio: InventarioRepository,
    private val productoId: Long,
    tipo: TipoMovimiento
) : ViewModel() {

    private val _estado = MutableStateFlow(MovementState(tipo = tipo))
    val estado: StateFlow<MovementState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            repositorio.observarProducto(productoId).collect { producto ->
                _estado.update { it.copy(producto = producto) }
            }
        }
    }

    fun onCantidadChange(valor: String) {
        val limpio = valor.filter { it.isDigit() }.take(6)
        _estado.update {
            it.copy(cantidad = limpio, errores = it.errores - CampoMovimiento.CANTIDAD, errorGeneral = null)
        }
    }

    fun onMotivoChange(motivo: String) {
        _estado.update { it.copy(motivo = motivo, errores = it.errores - CampoMovimiento.MOTIVO) }
    }

    fun onMotivoTextoChange(texto: String) {
        _estado.update {
            it.copy(motivoTexto = texto.take(80), errores = it.errores - CampoMovimiento.MOTIVO_TEXTO)
        }
    }

    private fun validar(): Boolean {
        val s = _estado.value
        val producto = s.producto ?: return false
        val e = mutableMapOf<String, String>()
        val n = s.cantidad.toIntOrNull()
        val esAjuste = s.tipo == TipoMovimiento.AJUSTE

        if (n == null || (!esAjuste && n < 1)) {
            e[CampoMovimiento.CANTIDAD] =
                if (esAjuste) "Ingresa el stock contado (puede ser 0)" else "Ingresa una cantidad entera de al menos 1"
        } else {
            val r = ReglasStock.aplicar(s.tipo, producto.stockActual, n)
            if (r is ReglasStock.Resultado.Rechazado) e[CampoMovimiento.CANTIDAD] = r.mensaje
        }
        if (!esAjuste && s.motivo == null) {
            e[CampoMovimiento.MOTIVO] = "Selecciona un motivo"
        }
        if (s.requiereTexto && s.motivoTexto.trim().length < MIN_MOTIVO) {
            e[CampoMovimiento.MOTIVO_TEXTO] = "Escribe al menos $MIN_MOTIVO caracteres"
        }

        _estado.update { it.copy(errores = e) }
        return e.isEmpty()
    }

    fun guardar(usuario: Usuario) {
        if (_estado.value.guardando || _estado.value.guardadoOk) return
        if (!validar()) return

        val s = _estado.value
        val cantidad = s.cantidad.toIntOrNull() ?: return
        val motivoFinal = when {
            s.tipo == TipoMovimiento.AJUSTE -> "Conteo físico: ${s.motivoTexto.trim()}"
            s.motivo == MotivosMovimiento.OTRO -> "Otro: ${s.motivoTexto.trim()}"
            else -> s.motivo.orEmpty()
        }

        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, errorGeneral = null) }
            when (val r = repositorio.registrarMovimiento(productoId, s.tipo, cantidad, motivoFinal, usuario)) {
                is ResultadoMovimiento.Ok -> _estado.update {
                    it.copy(
                        guardando = false,
                        guardadoOk = true,
                        mensajeExito = "Movimiento registrado. Nuevo stock: ${r.stockResultante}"
                    )
                }
                is ResultadoMovimiento.Error -> _estado.update {
                    it.copy(guardando = false, errorGeneral = r.mensaje)
                }
            }
        }
    }

    companion object {
        private const val MIN_MOTIVO = 5
    }
}