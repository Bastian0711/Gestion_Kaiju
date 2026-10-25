package com.example.appkaiju.stock.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appkaiju.stock.model.EstadoStock

@Composable
fun EtiquetaDisponibilidad(estado: EstadoStock) {
    val colores = MaterialTheme.colorScheme
    val color by animateColorAsState(
        targetValue = when (estado) {
            EstadoStock.NORMAL -> colores.secondaryContainer
            EstadoStock.BAJO -> colores.tertiaryContainer
            EstadoStock.AGOTADO -> colores.errorContainer
        },
        label = "colorDisponibilidad"
    )
    val texto = when (estado) {
        EstadoStock.NORMAL -> "Disponible"
        EstadoStock.BAJO -> "Pocas unidades"
        EstadoStock.AGOTADO -> "Agotado"
    }
    Surface(color = color, shape = RoundedCornerShape(50)) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}