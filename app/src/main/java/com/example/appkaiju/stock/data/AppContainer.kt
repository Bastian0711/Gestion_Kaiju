package com.example.appkaiju.stock.data

import android.content.Context
import com.example.appkaiju.stock.repository.ProductoRepository
import com.example.appkaiju.stock.repository.UsuarioRepository

class AppContainer(context: Context) {
    private val db = AppDatabase.getInstance(context)

    val usuarioRepository = UsuarioRepository(db.usuarioDao())
    val productoRepository = ProductoRepository(db.productoDao(), db.categoriaDao())
}