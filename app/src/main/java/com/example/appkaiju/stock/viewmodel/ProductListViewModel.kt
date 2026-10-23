package com.example.appkaiju.stock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appkaiju.stock.model.Producto
import com.example.appkaiju.stock.repository.ProductoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class ProductListViewModel(private val repositorio: ProductoRepository) : ViewModel() {

    private val _texto = MutableStateFlow("")
    val texto: StateFlow<String> = _texto.asStateFlow()


    val productos: StateFlow<List<Producto>> = _texto
        .flatMapLatest { repositorio.buscar(it.trim()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onTextoChange(valor: String) {
        _texto.value = valor
    }
}