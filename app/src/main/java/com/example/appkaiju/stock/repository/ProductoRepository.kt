package com.example.appkaiju.stock.repository

import com.example.appkaiju.stock.data.CategoriaDao
import com.example.appkaiju.stock.data.ProductoDao
import com.example.appkaiju.stock.model.Categoria
import com.example.appkaiju.stock.model.Producto
import com.example.appkaiju.stock.model.TipoProducto
import kotlinx.coroutines.flow.Flow
import com.example.appkaiju.stock.model.ProductoConCategoria

class ProductoRepository(
    private val productoDao: ProductoDao,
    private val categoriaDao: CategoriaDao
) {
    fun buscar(texto: String): Flow<List<Producto>> = productoDao.buscar(texto)

    fun catalogo(texto: String): Flow<List<ProductoConCategoria>> =
        productoDao.observarCatalogoConCategoria(texto)

    fun categorias(): Flow<List<Categoria>> = categoriaDao.observarTodas()


    suspend fun crearDatosIniciales() {
        if (productoDao.contar() > 0) return

        val vestuario = categoriaDao.insertar(Categoria(nombre = "Vestuario", descripcion = "Ropa de colección"))
        val tecnologia = categoriaDao.insertar(Categoria(nombre = "Tecnología", descripcion = "Accesorios electrónicos"))
        val insumos = categoriaDao.insertar(Categoria(nombre = "Insumos", descripcion = "Materiales de uso interno"))
        val accesorios = categoriaDao.insertar(Categoria(nombre = "Accesorios", descripcion = "Productos generales"))

        val productos = listOf(
            Producto(codigo = "VES-001", nombre = "Polera Kaiju Negra", categoriaId = vestuario,
                tipo = TipoProducto.VESTUARIO, precio = 14990, stockActual = 25, stockMinimo = 5,
                talla = "M", color = "Negro"),
            Producto(codigo = "VES-002", nombre = "Polera Kaiju Roja", categoriaId = vestuario,
                tipo = TipoProducto.VESTUARIO, precio = 14990, stockActual = 4, stockMinimo = 5,
                talla = "L", color = "Rojo"),
            Producto(codigo = "VES-003", nombre = "Polerón Kaiju", categoriaId = vestuario,
                tipo = TipoProducto.VESTUARIO, precio = 29990, stockActual = 0, stockMinimo = 3,
                talla = "M", color = "Gris"),
            Producto(codigo = "VES-004", nombre = "Gorro Kaiju", categoriaId = vestuario,
                tipo = TipoProducto.VESTUARIO, precio = 7990, stockActual = 12, stockMinimo = 4,
                talla = "Única", color = "Negro"),
            Producto(codigo = "TEC-001", nombre = "Audífonos Bluetooth", categoriaId = tecnologia,
                tipo = TipoProducto.TECNOLOGIA, precio = 19990, stockActual = 10, stockMinimo = 3,
                modelo = "BT-200"),
            Producto(codigo = "TEC-002", nombre = "Cargador USB-C", categoriaId = tecnologia,
                tipo = TipoProducto.TECNOLOGIA, precio = 9990, stockActual = 3, stockMinimo = 5,
                modelo = "UC-20"),
            Producto(codigo = "TEC-003", nombre = "Power Bank 10000 mAh", categoriaId = tecnologia,
                tipo = TipoProducto.TECNOLOGIA, precio = 15990, stockActual = 0, stockMinimo = 2,
                modelo = "PB-10"),
            Producto(codigo = "INS-001", nombre = "Pegamento Industrial", categoriaId = insumos,
                tipo = TipoProducto.INSUMO_PERECIBLE, precio = 3490, stockActual = 30, stockMinimo = 10,
                lote = "L2026-01", fechaVencimiento = "30-06-2027"),
            Producto(codigo = "INS-002", nombre = "Pintura Acrílica", categoriaId = insumos,
                tipo = TipoProducto.INSUMO_PERECIBLE, precio = 4990, stockActual = 8, stockMinimo = 8,
                lote = "L2026-02", fechaVencimiento = "31-12-2027"),
            Producto(codigo = "GEN-001", nombre = "Llavero Kaiju", categoriaId = accesorios,
                tipo = TipoProducto.GENERAL, precio = 2990, stockActual = 50, stockMinimo = 10),
            Producto(codigo = "GEN-002", nombre = "Póster Kaiju A3", categoriaId = accesorios,
                tipo = TipoProducto.GENERAL, precio = 5990, stockActual = 6, stockMinimo = 6),
            Producto(codigo = "GEN-003", nombre = "Taza Kaiju", categoriaId = accesorios,
                tipo = TipoProducto.GENERAL, precio = 6990, stockActual = 18, stockMinimo = 5)
        )
        productos.forEach { productoDao.insertar(it) }
    }
}