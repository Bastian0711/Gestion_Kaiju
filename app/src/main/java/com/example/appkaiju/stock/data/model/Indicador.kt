package com.example.appkaiju.stock.data.model

import com.google.gson.annotations.SerializedName

data class Indicador(
    @SerializedName("codigo") val codigo: String? = null,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("unidad_medida") val unidadMedida: String? = null,
    @SerializedName("fecha") val fecha: String? = null,
    @SerializedName("valor") val valor: Double? = null
)

/** Respuesta de GET https://mindicador.cl/api (solo los indicadores que usa la app). */
data class IndicadoresResponse(
    @SerializedName("fecha") val fecha: String? = null,
    @SerializedName("dolar") val dolar: Indicador? = null,
    @SerializedName("euro") val euro: Indicador? = null,
    @SerializedName("uf") val uf: Indicador? = null,
    @SerializedName("utm") val utm: Indicador? = null
)