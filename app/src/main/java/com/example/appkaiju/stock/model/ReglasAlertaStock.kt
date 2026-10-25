package com.example.appkaiju.stock.model

object ReglasAlertaStock {
    private fun gravedad(estado: EstadoStock): Int = when (estado) {
        EstadoStock.NORMAL -> 0
        EstadoStock.BAJO -> 1
        EstadoStock.AGOTADO -> 2
    }

    fun empeora(antes: Producto, despues: Producto): Boolean =
        gravedad(despues.estadoStock()) > gravedad(antes.estadoStock())

    fun productosParaAlertar(
        estadoPrevio: Map<Long, EstadoStock>,
        actuales: List<Producto>
    ): List<Producto> = actuales.filter { producto ->
        val antes = estadoPrevio[producto.id]
        val ahora = producto.estadoStock()
        antes != null && gravedad(ahora) > gravedad(antes)
    }
}