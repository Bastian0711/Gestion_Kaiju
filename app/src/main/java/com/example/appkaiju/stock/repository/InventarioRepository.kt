package com.example.appkaiju.stock.repository

import androidx.room.withTransaction
import com.example.appkaiju.stock.data.AppDatabase
import com.example.appkaiju.stock.model.Categoria
import com.example.appkaiju.stock.model.EstadoStock
import com.example.appkaiju.stock.model.MovimientoInventario
import com.example.appkaiju.stock.model.Producto
import com.example.appkaiju.stock.model.ReglasStock
import com.example.appkaiju.stock.model.Rol
import com.example.appkaiju.stock.model.TipoMovimiento
import com.example.appkaiju.stock.model.Usuario
import kotlinx.coroutines.flow.Flow

sealed class ResultadoMovimiento {
    data class Ok(val stockResultante: Int, val estado: EstadoStock) : ResultadoMovimiento()
    data class Error(val mensaje: String) : ResultadoMovimiento()
}

class InventarioRepository(private val db: AppDatabase) {
    private val productoDao = db.productoDao()
    private val movimientoDao = db.movimientoDao()
    private val categoriaDao = db.categoriaDao()
    private val usuarioDao = db.usuarioDao()

    fun observarCategorias(): Flow<List<Categoria>> = categoriaDao.observarTodas()

    fun observarProducto(id: Long): Flow<Producto?> = productoDao.observarPorId(id)

    fun observarMovimientos(productoId: Long): Flow<List<MovimientoInventario>> =
        movimientoDao.observarPorProducto(productoId)

    suspend fun nombresUsuarios(): Map<Long, String> =
        usuarioDao.listar().associate { it.id to it.nombre }

    suspend fun existeCodigo(codigo: String): Boolean =
        productoDao.buscarPorCodigo(codigo) != null

    suspend fun crearProducto(producto: Producto, usuarioId: Long): Long =
        db.withTransaction {
            val id = productoDao.insertar(producto)
            if (producto.stockActual > 0) {
                movimientoDao.insertar(
                    MovimientoInventario(
                        productoId = id,
                        tipo = TipoMovimiento.ENTRADA,
                        cantidad = producto.stockActual,
                        stockResultante = producto.stockActual,
                        fechaHora = System.currentTimeMillis(),
                        motivo = "Stock inicial",
                        usuarioId = usuarioId
                    )
                )
            }
            id
        }

    suspend fun registrarMovimiento(
        productoId: Long,
        tipo: TipoMovimiento,
        cantidad: Int,
        motivo: String,
        usuario: Usuario
    ): ResultadoMovimiento {
        if (usuario.rol != Rol.ADMIN) {
            return ResultadoMovimiento.Error("No tienes permisos para registrar movimientos")
        }
        if (tipo == TipoMovimiento.VENTA || tipo == TipoMovimiento.ANULACION_VENTA) {
            return ResultadoMovimiento.Error("Este movimiento se genera desde Ventas")
        }

        return db.withTransaction<ResultadoMovimiento> {
            val producto = productoDao.buscarPorId(productoId)
            when {
                producto == null -> ResultadoMovimiento.Error("El producto no existe")
                !producto.activo -> ResultadoMovimiento.Error("El producto está desactivado")
                else -> when (val r = ReglasStock.aplicar(tipo, producto.stockActual, cantidad)) {
                    is ReglasStock.Resultado.Rechazado -> ResultadoMovimiento.Error(r.mensaje)
                    is ReglasStock.Resultado.Ok -> {
                        productoDao.actualizarStock(productoId, r.stockResultante)
                        movimientoDao.insertar(
                            MovimientoInventario(
                                productoId = productoId,
                                tipo = tipo,
                                cantidad = r.cantidadMovimiento,
                                stockResultante = r.stockResultante,
                                fechaHora = System.currentTimeMillis(),
                                motivo = motivo,
                                usuarioId = usuario.id
                            )
                        )
                        ResultadoMovimiento.Ok(
                            r.stockResultante,
                            producto.copy(stockActual = r.stockResultante).estadoStock()
                        )
                    }
                }
            }
        }
    }
}