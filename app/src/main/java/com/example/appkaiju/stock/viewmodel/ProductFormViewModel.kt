package com.example.appkaiju.stock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appkaiju.stock.model.Categoria
import com.example.appkaiju.stock.model.Producto
import com.example.appkaiju.stock.model.TipoProducto
import com.example.appkaiju.stock.repository.InventarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

object CampoProducto {
    const val CODIGO = "codigo"
    const val NOMBRE = "nombre"
    const val DESCRIPCION = "descripcion"
    const val CATEGORIA = "categoria"
    const val PRECIO = "precio"
    const val STOCK_INICIAL = "stockInicial"
    const val STOCK_MINIMO = "stockMinimo"
    const val TALLA = "talla"
    const val COLOR = "color"
    const val MODELO = "modelo"
    const val LOTE = "lote"
    const val FECHA_VENC = "fechaVencimiento"
}

data class ProductFormState(
    val codigo: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: String = "",
    val stockInicial: String = "0",
    val stockMinimo: String = "0",
    val categoriaId: Long? = null,
    val tipo: TipoProducto = TipoProducto.GENERAL,
    val talla: String = "",
    val color: String = "",
    val modelo: String = "",
    val lote: String = "",
    val fechaVencimiento: String = "",
    val categorias: List<Categoria> = emptyList(),
    val errores: Map<String, String> = emptyMap(),
    val guardando: Boolean = false,
    val guardadoOk: Boolean = false,
    val imagenPath: String? = null
)

class ProductFormViewModel(private val repositorio: InventarioRepository) : ViewModel() {

    private val _estado = MutableStateFlow(ProductFormState())
    val estado: StateFlow<ProductFormState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            repositorio.observarCategorias().collect { categorias ->
                _estado.update { it.copy(categorias = categorias) }
            }
        }
    }

    fun onCampoChange(campo: String, valor: String) {
        val limpio = when (campo) {
            CampoProducto.PRECIO, CampoProducto.STOCK_INICIAL, CampoProducto.STOCK_MINIMO ->
                valor.filter { it.isDigit() }.take(9)
            CampoProducto.CODIGO -> valor.take(20)
            else -> valor
        }
        _estado.update { s ->
            val nuevo = when (campo) {
                CampoProducto.CODIGO -> s.copy(codigo = limpio)
                CampoProducto.NOMBRE -> s.copy(nombre = limpio)
                CampoProducto.DESCRIPCION -> s.copy(descripcion = limpio)
                CampoProducto.PRECIO -> s.copy(precio = limpio)
                CampoProducto.STOCK_INICIAL -> s.copy(stockInicial = limpio)
                CampoProducto.STOCK_MINIMO -> s.copy(stockMinimo = limpio)
                CampoProducto.TALLA -> s.copy(talla = limpio)
                CampoProducto.COLOR -> s.copy(color = limpio)
                CampoProducto.MODELO -> s.copy(modelo = limpio)
                CampoProducto.LOTE -> s.copy(lote = limpio)
                CampoProducto.FECHA_VENC -> s.copy(fechaVencimiento = limpio)
                else -> s
            }
            nuevo.copy(errores = nuevo.errores - campo)
        }
    }

    fun onCategoriaChange(id: Long) {
        _estado.update { it.copy(categoriaId = id, errores = it.errores - CampoProducto.CATEGORIA) }
    }

    fun onTipoChange(tipo: TipoProducto) {
        _estado.update { it.copy(tipo = tipo) }
    }

    fun onFotoChange(ruta: String?) {
        _estado.update { it.copy(imagenPath = ruta) }
    }

    private fun validar(): Boolean {
        val s = _estado.value
        val e = mutableMapOf<String, String>()

        if (!REGEX_CODIGO.matches(s.codigo.trim().uppercase())) {
            e[CampoProducto.CODIGO] = "3 a 20 caracteres: letras, números o guion"
        }
        if (s.nombre.isBlank()) {
            e[CampoProducto.NOMBRE] = "Ingresa el nombre del producto"
        } else if (s.nombre.trim().length < 3) {
            e[CampoProducto.NOMBRE] = "Debe tener al menos 3 caracteres"
        }
        if (s.categoriaId == null) {
            e[CampoProducto.CATEGORIA] = "Selecciona una categoría"
        }
        val precio = s.precio.toIntOrNull()
        if (precio == null || precio < 1) {
            e[CampoProducto.PRECIO] = "El precio debe ser al menos \$1"
        }
        if (s.stockInicial.toIntOrNull() == null) {
            e[CampoProducto.STOCK_INICIAL] = "Ingresa el stock inicial (puede ser 0)"
        }
        if (s.stockMinimo.toIntOrNull() == null) {
            e[CampoProducto.STOCK_MINIMO] = "Ingresa el stock mínimo (puede ser 0)"
        }
        if (s.tipo == TipoProducto.INSUMO_PERECIBLE) {
            if (s.lote.isBlank()) e[CampoProducto.LOTE] = "Ingresa el lote"
            if (!fechaValida(s.fechaVencimiento.trim())) {
                e[CampoProducto.FECHA_VENC] = "Usa el formato dd-MM-aaaa (ej: 31-12-2027)"
            }
        }

        _estado.update { it.copy(errores = e) }
        return e.isEmpty()
    }

    private fun fechaValida(texto: String): Boolean =
        try {
            LocalDate.parse(texto, FORMATO_FECHA)
            true
        } catch (ex: DateTimeParseException) {
            false
        }

    fun guardar(usuarioId: Long) {
        if (!validar()) return
        viewModelScope.launch {
            val s = _estado.value
            _estado.update { it.copy(guardando = true) }
            val codigo = s.codigo.trim().uppercase()

            if (repositorio.existeCodigo(codigo)) {
                _estado.update {
                    it.copy(
                        guardando = false,
                        errores = it.errores + (CampoProducto.CODIGO to "Ya existe un producto con ese código")
                    )
                }
                return@launch
            }

            val producto = Producto(
                codigo = codigo,
                nombre = s.nombre.trim(),
                descripcion = s.descripcion.trim(),
                categoriaId = s.categoriaId ?: return@launch,
                tipo = s.tipo,
                precio = s.precio.toInt(),
                stockActual = s.stockInicial.toInt(),
                stockMinimo = s.stockMinimo.toInt(),
                talla = if (s.tipo == TipoProducto.VESTUARIO) s.talla.trim().ifBlank { null } else null,
                color = if (s.tipo == TipoProducto.VESTUARIO) s.color.trim().ifBlank { null } else null,
                modelo = if (s.tipo == TipoProducto.TECNOLOGIA) s.modelo.trim().ifBlank { null } else null,
                lote = if (s.tipo == TipoProducto.INSUMO_PERECIBLE) s.lote.trim() else null,
                fechaVencimiento = if (s.tipo == TipoProducto.INSUMO_PERECIBLE) s.fechaVencimiento.trim() else null,
                imagenUri = s.imagenPath
            )
            repositorio.crearProducto(producto, usuarioId)
            _estado.update { it.copy(guardando = false, guardadoOk = true) }
        }
    }

    companion object {
        private val REGEX_CODIGO = Regex("^[A-Z0-9-]{3,20}$")
        private val FORMATO_FECHA: DateTimeFormatter =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT)
    }
}