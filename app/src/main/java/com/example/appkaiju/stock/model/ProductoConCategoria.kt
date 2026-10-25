package com.example.appkaiju.stock.model

import androidx.room.Embedded

data class ProductoConCategoria(
    @Embedded val producto: Producto,
    val categoriaNombre: String
)