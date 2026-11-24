package com.example.appkaiju.stock.ui.components

import com.example.appkaiju.stock.model.MovimientoInventario
import com.example.appkaiju.stock.model.TipoMovimiento
import com.example.appkaiju.stock.model.TipoProducto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Int.comoClp(): String =
    "\$" + String.format(Locale.forLanguageTag("es-CL"), "%,d", this)

private val FORMATO_FECHA_HORA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

fun Long.comoFechaHora(): String =
    Instant.ofEpochMilli(this).atZone(ZoneId.of("America/Santiago")).format(FORMATO_FECHA_HORA)

fun TipoMovimiento.etiqueta(): String = when (this) {
    TipoMovimiento.ENTRADA -> "Entrada"
    TipoMovimiento.SALIDA -> "Salida"
    TipoMovimiento.AJUSTE -> "Ajuste"
    TipoMovimiento.VENTA -> "Venta"
    TipoMovimiento.ANULACION_VENTA -> "Anulación de venta"
}

fun TipoProducto.etiqueta(): String = when (this) {
    TipoProducto.VESTUARIO -> "Vestuario"
    TipoProducto.TECNOLOGIA -> "Tecnología"
    TipoProducto.INSUMO_PERECIBLE -> "Insumo perecible"
    TipoProducto.GENERAL -> "General"
}

fun MovimientoInventario.cantidadConSigno(): String = when (tipo) {
    TipoMovimiento.ENTRADA, TipoMovimiento.ANULACION_VENTA -> "+$cantidad"
    TipoMovimiento.SALIDA, TipoMovimiento.VENTA -> "-$cantidad"
    TipoMovimiento.AJUSTE -> if (cantidad > 0) "+$cantidad" else "$cantidad"
}

// Formatos para la API externa de indicadores (mindicador.cl)

fun Double.comoPesosConDecimales(): String =
    "\$" + String.format(Locale.forLanguageTag("es-CL"), "%,.2f", this)

fun Double.comoUsd(): String =
    "US\$ " + String.format(Locale.forLanguageTag("es-CL"), "%,.2f", this)

fun String.comoFechaCorta(): String {
    val partes = take(10).split("-")
    return if (partes.size == 3) "${partes[2]}-${partes[1]}-${partes[0]}" else this
}

fun etiquetaIndicador(codigo: String?, nombre: String?): String = when (codigo) {
    "dolar" -> "Dólar observado"
    "euro" -> "Euro"
    "uf" -> "UF"
    "utm" -> "UTM"
    else -> nombre ?: codigo.orEmpty()
}