package com.example.appkaiju.stock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appkaiju.stock.model.VentaConUsuario
import com.example.appkaiju.stock.repository.VentaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SaleHistoryViewModel(repositorio: VentaRepository) : ViewModel() {
    val ventas: StateFlow<List<VentaConUsuario>> = repositorio.observarVentas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}