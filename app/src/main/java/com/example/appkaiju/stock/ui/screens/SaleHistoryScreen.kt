package com.example.appkaiju.stock.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appkaiju.stock.model.EstadoVenta
import com.example.appkaiju.stock.ui.components.comoClp
import com.example.appkaiju.stock.ui.components.comoFechaHora
import com.example.appkaiju.stock.viewmodel.SaleHistoryViewModel

@Composable
fun SaleHistoryScreen(
    viewModel: SaleHistoryViewModel,
    onBack: () -> Unit,
    onNuevaVenta: () -> Unit,
    onVentaClick: (Long) -> Unit
) {
    val ventas by viewModel.ventas.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text(
                "Ventas",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = onNuevaVenta) { Text("Nueva venta") }
        }

        if (ventas.isEmpty()) {
            Text("Aún no hay ventas registradas", modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(ventas, key = { it.venta.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onVentaClick(item.venta.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Venta N° ${item.venta.id}", style = MaterialTheme.typography.titleMedium)
                                Text(item.venta.fechaHora.comoFechaHora(), style = MaterialTheme.typography.bodySmall)
                                Text("Registrada por ${item.usuarioNombre}", style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(item.venta.total.comoClp(), style = MaterialTheme.typography.titleMedium)
                                Text(
                                    if (item.venta.estado == EstadoVenta.ACTIVA) "Activa" else "Anulada",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}