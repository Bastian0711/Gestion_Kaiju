package com.example.appkaiju.stock.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.appkaiju.stock.viewmodel.SaleDetailViewModel

@Composable
fun SaleDetailScreen(viewModel: SaleDetailViewModel, onBack: () -> Unit) {
    val s by viewModel.estado.collectAsState()
    val venta = s.venta

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text("Detalle de venta", style = MaterialTheme.typography.headlineMedium)
        }

        if (venta == null) {
            Text(if (s.cargando) "Cargando..." else "No se encontró la venta")
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Venta N° ${venta.venta.id}", style = MaterialTheme.typography.titleLarge)
                    Text("Fecha: ${venta.venta.fechaHora.comoFechaHora()}")
                    Text("Registrada por: ${venta.usuarioNombre}")
                    Text("Estado: ${if (venta.venta.estado == EstadoVenta.ACTIVA) "Activa" else "Anulada"}")
                }
            }

            Text("Productos", style = MaterialTheme.typography.titleMedium)
            s.detalles.forEach { d ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(d.productoNombre, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${d.detalle.cantidad} × ${d.detalle.precioUnitario.comoClp()}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(d.detalle.subtotal.comoClp(), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                Text(venta.venta.total.comoClp(), style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}