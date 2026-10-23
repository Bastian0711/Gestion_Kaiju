package com.example.appkaiju.stock.repository

import androidx.room.withTransaction
import com.example.appkaiju.stock.data.AppDatabase
import com.example.appkaiju.stock.model.Categoria
import com.example.appkaiju.stock.model.MovimientoInventario
import com.example.appkaiju.stock.model.Producto
import com.example.appkaiju.stock.model.TipoMovimiento
import kotlinx.coroutines.flow.Flow


class InventarioRepository(private val db: AppDatabase) {
    private val productoDao = db.productoDao()
    private val movimientoDao = db.movimientoDao()
    private val categoriaDao = db.categoriaDao()

    fun observarCategorias(): Flow<List<Categoria>> = categoriaDao.observarTodas()

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
}