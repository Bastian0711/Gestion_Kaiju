package com.example.appkaiju.stock.model

/** Conversion de precios usando el valor del dolar de la API externa. */
object ConversorMoneda {

    /** Devuelve el precio en dolares, o null si no hay un valor de dolar valido. */
    fun clpADolares(precioClp: Int, valorDolar: Double?): Double? =
        if (valorDolar == null || valorDolar <= 0.0) null else precioClp / valorDolar
}