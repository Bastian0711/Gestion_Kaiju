package com.example.appkaiju.stock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appkaiju.stock.model.Categoria
import com.example.appkaiju.stock.model.ProductoConCategoria
import com.example.appkaiju.stock.repository.ProductoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class CatalogState(
    val texto: String = "",
    val categoriaId: Long? = null,
    val categorias: List<Categoria> = emptyList(),
    val productos: List<ProductoConCategoria> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModel(repositorio: ProductoRepository) : ViewModel() {

    private val texto = MutableStateFlow("")
    private val categoriaId = MutableStateFlow<Long?>(null)

    val estado: StateFlow<CatalogState> = combine(
        texto.flatMapLatest { repositorio.catalogo(it.trim()) },
        repositorio.categorias(),
        texto,
        categoriaId
    ) { items, categorias, textoActual, categoriaActual ->
        CatalogState(
            texto = textoActual,
            categoriaId = categoriaActual,
            categorias = categorias,
            productos = if (categoriaActual == null) items
            else items.filter { it.producto.categoriaId == categoriaActual }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CatalogState())

    fun onTextoChange(valor: String) {
        texto.value = valor
    }

    fun onCategoriaChange(id: Long?) {
        categoriaId.value = id
    }
}