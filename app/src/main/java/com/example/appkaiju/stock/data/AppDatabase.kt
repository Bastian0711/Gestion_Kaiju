package com.example.appkaiju.stock.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.appkaiju.stock.model.Categoria
import com.example.appkaiju.stock.model.DetalleVenta
import com.example.appkaiju.stock.model.MovimientoInventario
import com.example.appkaiju.stock.model.Producto
import com.example.appkaiju.stock.model.Usuario
import com.example.appkaiju.stock.model.Venta

@Database(
    entities = [
        Usuario::class,
        Categoria::class,
        Producto::class,
        MovimientoInventario::class,
        Venta::class,
        DetalleVenta::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun productoDao(): ProductoDao
    abstract fun movimientoDao(): MovimientoDao
    abstract fun ventaDao(): VentaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null


        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kaiju_stock.db"
                ).build().also { INSTANCE = it }
            }
    }
}