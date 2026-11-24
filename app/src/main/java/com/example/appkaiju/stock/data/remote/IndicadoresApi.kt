package com.example.appkaiju.stock.data.remote

import com.example.appkaiju.stock.data.model.IndicadoresResponse
import retrofit2.http.GET

/** Endpoints de la API externa mindicador.cl (indicadores económicos de Chile). */
interface IndicadoresApi {

    /** GET https://mindicador.cl/api : valores del día de dólar, euro, UF, UTM, etc. */
    @GET("api")
    suspend fun obtenerIndicadores(): IndicadoresResponse
}