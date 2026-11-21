package com.example.appkaiju.stock

import com.example.appkaiju.stock.data.EjecutorTransaccion
import com.example.appkaiju.stock.model.Producto
import com.example.appkaiju.stock.model.Rol
import com.example.appkaiju.stock.model.TipoProducto
import com.example.appkaiju.stock.model.Usuario

/** Ejecuta el bloque directamente: reemplaza la transacción de Room en las pruebas. */
object SinTransaccion : EjecutorTransaccion {
    override suspend fun <R> enTransaccion(bloque: suspend () -> R): R = bloque()
}

/** Datos ficticios reutilizados por las pruebas. */
object Datos {
    val admin = Usuario(
        id = 1, nombre = "Administrador Kaiju", correo = "admin@kaiju.cl",
        passwordHash = "hash", rol = Rol.ADMIN
    )
    val cliente = Usuario(
        id = 2, nombre = "Cliente de Prueba", correo = "cliente@kaiju.cl",
        passwordHash = "hash", rol = Rol.CLIENTE
    )

    fun producto(
        id: Long = 1,
        nombre: String = "Polera Kaiju",
        stock: Int = 10,
        minimo: Int = 3,
        precio: Int = 1000,
        activo: Boolean = true,
        categoriaId: Long = 1,
        tipo: TipoProducto = TipoProducto.GENERAL
    ) = Producto(
        id = id,
        codigo = "COD-$id",
        nombre = nombre,
        categoriaId = categoriaId,
        tipo = tipo,
        precio = precio,
        stockActual = stock,
        stockMinimo = minimo,
        activo = activo
    )
}