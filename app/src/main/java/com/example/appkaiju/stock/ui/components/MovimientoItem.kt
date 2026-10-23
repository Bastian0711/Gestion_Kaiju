package com.example.appkaiju.stock.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appkaiju.stock.model.MovimientoInventario

@Composable
fun MovimientoItem(movimiento: MovimientoInventario, nombreUsuario: String? = null) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${movimiento.tipo.etiqueta()} · ${movimiento.fechaHora.comoFechaHora()}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(movimiento.motivo, style = MaterialTheme.typography.bodyMedium)
                if (nombreUsuario != null) {
                    Text("Registrado por: $nombreUsuario", style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(movimiento.cantidadConSigno(), style = MaterialTheme.typography.titleMedium)
                Text("Stock: ${movimiento.stockResultante}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}