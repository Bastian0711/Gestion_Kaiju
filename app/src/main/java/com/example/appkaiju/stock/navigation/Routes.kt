package com.example.appkaiju.stock.navigation

import com.example.appkaiju.stock.model.TipoMovimiento

/** Rutas de la app. Se centralizan acá para no repetir strings en las pantallas. */
object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val PRODUCTS = "products"
    const val CATALOG = "catalog"

    const val PRODUCT_FORM = "product_form"

    const val ARG_PRODUCTO_ID = "productoId"
    const val ARG_TIPO = "tipo"

    const val PRODUCT_DETAIL = "product_detail/{$ARG_PRODUCTO_ID}"
    const val MOVEMENT = "movement/{$ARG_PRODUCTO_ID}/{$ARG_TIPO}"
    const val KARDEX = "kardex/{$ARG_PRODUCTO_ID}"

    fun productDetail(productoId: Long) = "product_detail/$productoId"
    fun movement(productoId: Long, tipo: TipoMovimiento) = "movement/$productoId/${tipo.name}"
    fun kardex(productoId: Long) = "kardex/$productoId"
}