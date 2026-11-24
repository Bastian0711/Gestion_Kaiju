package com.example.appkaiju.stock.repository

import com.example.appkaiju.stock.data.model.Indicador
import com.example.appkaiju.stock.data.remote.IndicadoresApi
import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException

sealed class ResultadoIndicadores {
    data class Ok(val indicadores: List<Indicador>) : ResultadoIndicadores()
    data class Error(val mensaje: String) : ResultadoIndicadores()
}

/**
 * Obtiene los indicadores económicos desde la API externa.
 * Los errores de red o de formato se transforman en un mensaje para la interfaz,
 * así la falta de internet nunca detiene el resto de la aplicación.
 */
class IndicadoresRepository(private val api: IndicadoresApi) {

    suspend fun obtenerIndicadores(): ResultadoIndicadores =
        try {
            val respuesta = api.obtenerIndicadores()
            val lista = listOfNotNull(respuesta.dolar, respuesta.euro, respuesta.uf, respuesta.utm)
                .filter { (it.valor ?: 0.0) > 0.0 }
            if (lista.isEmpty()) {
                ResultadoIndicadores.Error(MENSAJE_SIN_DATOS)
            } else {
                ResultadoIndicadores.Ok(lista)
            }
        } catch (e: IOException) {
            ResultadoIndicadores.Error(MENSAJE_SIN_CONEXION)
        } catch (e: HttpException) {
            ResultadoIndicadores.Error("El servicio de indicadores respondió con error ${e.code()}")
        } catch (e: JsonParseException) {
            ResultadoIndicadores.Error(MENSAJE_SIN_DATOS)
        }

    companion object {
        const val MENSAJE_SIN_CONEXION = "Sin conexión: no se pudieron cargar los indicadores"
        const val MENSAJE_SIN_DATOS = "La API no devolvió indicadores válidos"
    }
}