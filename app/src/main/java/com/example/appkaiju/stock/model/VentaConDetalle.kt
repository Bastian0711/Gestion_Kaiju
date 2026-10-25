package com.example.appkaiju.stock.model

import androidx.room.Embedded

// Venta con el nombre de quien la registro
data class VentaConUsuario(
    @Embedded val venta: Venta,
    val usuarioNombre: String
)

// Linea de una venta con el nombre del producto
data class DetalleConProducto(
    @Embedded val detalle: DetalleVenta,
    val productoNombre: String
)