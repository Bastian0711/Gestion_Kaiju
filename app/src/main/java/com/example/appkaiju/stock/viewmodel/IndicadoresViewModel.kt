package com.example.appkaiju.stock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appkaiju.stock.data.model.Indicador
import com.example.appkaiju.stock.repository.IndicadoresRepository
import com.example.appkaiju.stock.repository.ResultadoIndicadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IndicadoresUiState(
    val cargando: Boolean = false,
    val indicadores: List<Indicador> = emptyList(),
    val error: String? = null
) {
    /** Valor del dolar observado en pesos, usado para mostrar precios de referencia. */
    val valorDolar: Double?
        get() = indicadores.firstOrNull { it.codigo == CODIGO_DOLAR }?.valor

    companion object {
        const val CODIGO_DOLAR = "dolar"
    }
}

class IndicadoresViewModel(private val repositorio: IndicadoresRepository) : ViewModel() {

    private val _estado = MutableStateFlow(IndicadoresUiState())
    val estado: StateFlow<IndicadoresUiState> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        if (_estado.value.cargando) return
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            when (val r = repositorio.obtenerIndicadores()) {
                is ResultadoIndicadores.Ok -> _estado.update {
                    it.copy(cargando = false, indicadores = r.indicadores, error = null)
                }
                // Si falla, se conservan los ultimos valores obtenidos
                is ResultadoIndicadores.Error -> _estado.update {
                    it.copy(cargando = false, error = r.mensaje)
                }
            }
        }
    }
}