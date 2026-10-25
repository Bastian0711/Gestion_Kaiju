package com.example.appkaiju.stock.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.appkaiju.stock.model.Categoria
import com.example.appkaiju.stock.model.DetalleVenta
import com.example.appkaiju.stock.model.EstadoVenta
import com.example.appkaiju.stock.model.MovimientoInventario
import com.example.appkaiju.stock.model.Producto
import com.example.appkaiju.stock.model.Usuario
import com.example.appkaiju.stock.model.Venta
import kotlinx.coroutines.flow.Flow
import com.example.appkaiju.stock.model.ProductoConCategoria

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int

    @Query("SELECT * FROM usuarios")
    suspend fun listar(): List<Usuario>
}

@Dao
interface CategoriaDao {
    @Insert
    suspend fun insertar(categoria: Categoria): Long

    @Update
    suspend fun actualizar(categoria: Categoria)

    @Query("DELETE FROM categorias WHERE id = :id")
    suspend fun eliminar(id: Long)

    @Query("SELECT * FROM categorias ORDER BY nombre")
    fun observarTodas(): Flow<List<Categoria>>

    @Query("SELECT COUNT(*) FROM productos WHERE categoriaId = :categoriaId")
    suspend fun contarProductos(categoriaId: Long): Int
}

@Dao
interface ProductoDao {
    @Insert
    suspend fun insertar(producto: Producto): Long

    @Update
    suspend fun actualizar(producto: Producto)

    @Query("SELECT * FROM productos WHERE id = :id")
    suspend fun buscarPorId(id: Long): Producto?

    @Query("SELECT * FROM productos WHERE id = :id")
    fun observarPorId(id: Long): Flow<Producto?>

    @Query("SELECT * FROM productos WHERE codigo = :codigo LIMIT 1")
    suspend fun buscarPorCodigo(codigo: String): Producto?


    @Query(
        "SELECT * FROM productos WHERE activo = 1 AND " +
                "(nombre LIKE '%' || :texto || '%' OR codigo LIKE '%' || :texto || '%') " +
                "ORDER BY nombre"
    )
    fun buscar(texto: String): Flow<List<Producto>>


    @Query(
        "SELECT * FROM productos WHERE activo = 1 AND publicado = 1 AND " +
                "nombre LIKE '%' || :texto || '%' ORDER BY nombre"
    )
    fun observarCatalogo(texto: String): Flow<List<Producto>>

    @Query(
        "SELECT p.*, c.nombre AS categoriaNombre FROM productos p " +
                "INNER JOIN categorias c ON c.id = p.categoriaId " +
                "WHERE p.activo = 1 AND p.publicado = 1 AND p.nombre LIKE '%' || :texto || '%' " +
                "ORDER BY p.nombre"
    )
    fun observarCatalogoConCategoria(texto: String): Flow<List<ProductoConCategoria>>
    @Query(
        "SELECT * FROM productos WHERE activo = 1 AND stockActual <= stockMinimo " +
                "ORDER BY stockActual ASC, nombre ASC"
    )
    fun observarReposicion(): Flow<List<Producto>>


    @Query("UPDATE productos SET stockActual = :nuevoStock WHERE id = :id")
    suspend fun actualizarStock(id: Long, nuevoStock: Int)

    @Query("SELECT COUNT(*) FROM productos")
    suspend fun contar(): Int
}

@Dao
interface MovimientoDao {
    @Insert
    suspend fun insertar(movimiento: MovimientoInventario): Long


    @Query("SELECT * FROM movimientos WHERE productoId = :productoId ORDER BY fechaHora DESC")
    fun observarPorProducto(productoId: Long): Flow<List<MovimientoInventario>>

    @Query("SELECT * FROM movimientos ORDER BY fechaHora DESC")
    fun observarTodos(): Flow<List<MovimientoInventario>>
}

@Dao
interface VentaDao {
    @Insert
    suspend fun insertarVenta(venta: Venta): Long

    @Insert
    suspend fun insertarDetalles(detalles: List<DetalleVenta>)

    @Query("SELECT * FROM ventas ORDER BY fechaHora DESC")
    fun observarVentas(): Flow<List<Venta>>

    @Query("SELECT * FROM detalle_venta WHERE ventaId = :ventaId")
    suspend fun detalleDeVenta(ventaId: Long): List<DetalleVenta>

    @Query("UPDATE ventas SET estado = :estado WHERE id = :id")
    suspend fun cambiarEstado(id: Long, estado: EstadoVenta)
}