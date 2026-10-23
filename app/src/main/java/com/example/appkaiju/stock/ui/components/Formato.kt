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