package com.example.appkaiju.stock.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios", indices = [Index(value = ["correo"], unique = true)])
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val correo: String,
    val passwordHash: String,
    val rol: Rol,
    val activo: Boolean = true
)

@Entity(tableName = "categorias", indices = [Index(value = ["nombre"], unique = true)])
data class Categoria(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val descripcion: String = ""
)

@Entity(
    tableName = "productos",
    foreignKeys = [ForeignKey(
        entity = Categoria::class,
        parentColumns = ["id"],
        childColumns = ["categoriaId"],
        onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index(value = ["codigo"], unique = true), Index("categoriaId")]
)
data class Producto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigo: String,
    val nombre: String,
    val descripcion: String = "",
    val categoriaId: Long,
    val tipo: TipoProducto,
    val precio: Int,
    val stockActual: Int = 0,
    val stockMinimo: Int = 0,
    val publicado: Boolean = true,
    val activo: Boolean = true,
    val talla: String? = null,
    val color: String? = null,
    val modelo: String? = null,
    val lote: String? = null,
    val fechaVencimiento: String? = null,
    val imagenUri: String? = null
) {

    fun estadoStock(): EstadoStock = when {
        stockActual <= 0 -> EstadoStock.AGOTADO
        stockActual <= stockMinimo -> EstadoStock.BAJO
        else -> EstadoStock.NORMAL
    }
}

@Entity(
    tableName = "ventas",
    foreignKeys = [ForeignKey(
        entity = Usuario::class,
        parentColumns = ["id"],
        childColumns = ["usuarioId"]
    )],
    indices = [Index("usuarioId")]
)
data class Venta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fechaHora: Long,
    val total: Int,
    val estado: EstadoVenta = EstadoVenta.ACTIVA,
    val usuarioId: Long
)

@Entity(
    tableName = "detalle_venta",
    foreignKeys = [
        ForeignKey(entity = Venta::class, parentColumns = ["id"], childColumns = ["ventaId"]),
        ForeignKey(entity = Producto::class, parentColumns = ["id"], childColumns = ["productoId"])
    ],
    indices = [Index("ventaId"), Index("productoId")]
)
data class DetalleVenta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ventaId: Long,
    val productoId: Long,
    val cantidad: Int,
    val precioUnitario: Int,
    val subtotal: Int
)

@Entity(
    tableName = "movimientos",
    foreignKeys = [
        ForeignKey(entity = Producto::class, parentColumns = ["id"], childColumns = ["productoId"]),
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["usuarioId"])
    ],
    indices = [Index("productoId"), Index("usuarioId"), Index("ventaId")]
)
data class MovimientoInventario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productoId: Long,
    val tipo: TipoMovimiento,
    val cantidad: Int,
    val stockResultante: Int,
    val fechaHora: Long,
    val motivo: String,
    val usuarioId: Long,
    val ventaId: Long? = null
)