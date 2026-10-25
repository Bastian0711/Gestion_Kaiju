package com.example.appkaiju.stock.repository

import androidx.room.withTransaction
import com.example.appkaiju.stock.data.AppDatabase
import com.example.appkaiju.stock.model.DetalleConProducto
import com.example.appkaiju.stock.model.DetalleVenta
import com.example.appkaiju.stock.model.MovimientoInventario
import com.example.appkaiju.stock.model.Producto
import com.example.appkaiju.stock.model.ReglasStock
import com.example.appkaiju.stock.model.Rol
import com.example.appkaiju.stock.model.TipoMovimiento
import com.example.appkaiju.stock.model.Usuario
import com.example.appkaiju.stock.model.Venta
import com.example.appkaiju.stock.model.VentaConUsuario
import kotlinx.coroutines.flow.Flow

data class LineaVenta(val productoId: Long, val cantidad: Int)

sealed class ResultadoVenta {
    data class Ok(val ventaId: Long, val total: Int) : ResultadoVenta()
    data class Error(val mensaje: String) : ResultadoVenta()
}

class VentaRepository(private val db: AppDatabase) {
    private val ventaDao = db.ventaDao()
    private val productoDao = db.productoDao()
    private val movimientoDao = db.movimientoDao()

    fun observarVentas(): Flow<List<VentaConUsuario>> = ventaDao.observarVentas()

    suspend fun obtenerVenta(id: Long): VentaConUsuario? = ventaDao.ventaConUsuario(id)

    suspend fun detalleDeVenta(id: Long): List<DetalleConProducto> = ventaDao.detalleConProducto(id)

    suspend fun registrarVenta(lineas: List<LineaVenta>, usuario: Usuario): ResultadoVenta {
        if (usuario.rol != Rol.ADMIN) {
            return ResultadoVenta.Error("No tienes permisos para registrar ventas")
        }
        if (lineas.isEmpty()) {
            return ResultadoVenta.Error("Agrega al menos un producto a la venta")
        }

        return db.withTransaction<ResultadoVenta> {

            val validadas = mutableListOf<Triple<Producto, Int, ReglasStock.Resultado.Ok>>()
            for (linea in lineas) {
                val producto = productoDao.buscarPorId(linea.productoId)
                    ?: return@withTransaction ResultadoVenta.Error("Uno de los productos ya no existe")
                if (!producto.activo) {
                    return@withTransaction ResultadoVenta.Error("${producto.nombre} está desactivado")
                }
                when (val r = ReglasStock.aplicar(TipoMovimiento.VENTA, producto.stockActual, linea.cantidad)) {
                    is ReglasStock.Resultado.Rechazado ->
                        return@withTransaction ResultadoVenta.Error("${producto.nombre}: ${r.mensaje}")
                    is ReglasStock.Resultado.Ok ->
                        validadas.add(Triple(producto, linea.cantidad, r))
                }
            }

            val total = validadas.sumOf { (producto, cantidad, _) -> producto.precio * cantidad }
            val ahora = System.currentTimeMillis()
            val ventaId = ventaDao.insertarVenta(
                Venta(fechaHora = ahora, total = total, usuarioId = usuario.id)
            )
            ventaDao.insertarDetalles(
                validadas.map { (producto, cantidad, _) ->
                    DetalleVenta(
                        ventaId = ventaId,
                        productoId = producto.id,
                        cantidad = cantidad,
                        precioUnitario = producto.precio,
                        subtotal = producto.precio * cantidad
                    )
                }
            )
            validadas.forEach { (producto, _, r) ->
                productoDao.actualizarStock(producto.id, r.stockResultante)
                movimientoDao.insertar(
                    MovimientoInventario(
                        productoId = producto.id,
                        tipo = TipoMovimiento.VENTA,
                        cantidad = r.cantidadMovimiento,
                        stockResultante = r.stockResultante,
                        fechaHora = ahora,
                        motivo = "Venta N° $ventaId",
                        usuarioId = usuario.id,
                        ventaId = ventaId
                    )
                )
            }
            ResultadoVenta.Ok(ventaId, total)
        }
    }
}