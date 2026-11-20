package com.example.appkaiju.stock.data

import android.content.Context
import com.example.appkaiju.stock.data.remote.RetrofitInstance
import com.example.appkaiju.stock.model.Producto
import com.example.appkaiju.stock.repository.IndicadoresRepository
import com.example.appkaiju.stock.repository.InventarioRepository
import com.example.appkaiju.stock.repository.ProductoRepository
import com.example.appkaiju.stock.repository.UsuarioRepository
import com.example.appkaiju.stock.repository.VentaRepository
import kotlinx.coroutines.flow.MutableSharedFlow

class AppContainer(context: Context) {
    private val db = AppDatabase.getInstance(context)

    private val transacciones = RoomEjecutorTransaccion(db)
    val usuarioRepository = UsuarioRepository(db.usuarioDao())
    val productoRepository = ProductoRepository(db.productoDao(), db.categoriaDao())
    val inventarioRepository = InventarioRepository(
        productoDao = db.productoDao(),
        movimientoDao = db.movimientoDao(),
        categoriaDao = db.categoriaDao(),
        usuarioDao = db.usuarioDao(),
        transacciones = transacciones
    )
    val ventaRepository = VentaRepository(
        ventaDao = db.ventaDao(),
        productoDao = db.productoDao(),
        movimientoDao = db.movimientoDao(),
        transacciones = transacciones
    )
    // API externa (Retrofit)
    val indicadoresRepository = IndicadoresRepository(RetrofitInstance.indicadoresApi)
    val alertasStock = MutableSharedFlow<Producto>(extraBufferCapacity = 16)
}