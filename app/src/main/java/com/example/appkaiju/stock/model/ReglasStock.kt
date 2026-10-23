package com.example.appkaiju.stock.model


object ReglasStock {

    sealed class Resultado {

        data class Ok(val stockResultante: Int, val cantidadMovimiento: Int) : Resultado()
        data class Rechazado(val mensaje: String) : Resultado()
    }


    fun aplicar(tipo: TipoMovimiento, stockActual: Int, cantidad: Int): Resultado = when (tipo) {
        TipoMovimiento.ENTRADA, TipoMovimiento.ANULACION_VENTA ->
            if (cantidad < 1) {
                Resultado.Rechazado("La cantidad debe ser al menos 1")
            } else {
                Resultado.Ok(stockActual + cantidad, cantidad)
            }

        TipoMovimiento.SALIDA, TipoMovimiento.VENTA ->
            when {
                cantidad < 1 -> Resultado.Rechazado("La cantidad debe ser al menos 1")
                cantidad > stockActual -> Resultado.Rechazado("Stock insuficiente: disponible $stockActual")
                else -> Resultado.Ok(stockActual - cantidad, cantidad)
            }

        TipoMovimiento.AJUSTE ->
            when {
                cantidad < 0 -> Resultado.Rechazado("El stock contado no puede ser negativo")
                cantidad == stockActual -> Resultado.Rechazado("No hay diferencia con el stock registrado")
                else -> Resultado.Ok(cantidad, cantidad - stockActual)
            }
    }
}