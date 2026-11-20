package com.example.appkaiju.stock.data

import androidx.room.withTransaction

interface EjecutorTransaccion {
    suspend fun <R> enTransaccion(bloque: suspend () -> R): R
}

class RoomEjecutorTransaccion(private val db: AppDatabase) : EjecutorTransaccion {
    override suspend fun <R> enTransaccion(bloque: suspend () -> R): R =
        db.withTransaction { bloque() }
}