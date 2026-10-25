package com.example.appkaiju.stock.data

import android.content.Context
import com.example.appkaiju.stock.model.Producto
import com.example.appkaiju.stock.repository.InventarioRepository
import com.example.appkaiju.stock.repository.ProductoRepository
import com.example.appkaiju.stock.repository.UsuarioRepository
import com.example.appkaiju.stock.repository.VentaRepository
import kotlinx.coroutines.flow.MutableSharedFlow

class AppContainer(context: Context) {
    private val db = AppDatabase.getInstance(context)

    val usuarioRepository = UsuarioRepository(db.usuarioDao())
    val productoRepository = ProductoRepository(db.productoDao(), db.categoriaDao())
    val inventarioRepository = InventarioRepository(db)
    val ventaRepository = VentaRepository(db)

    val alertasStock = MutableSharedFlow<Producto>(extraBufferCapacity = 16)
}