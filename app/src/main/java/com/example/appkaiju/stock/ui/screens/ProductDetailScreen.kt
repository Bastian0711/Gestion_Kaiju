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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appkaiju.stock.model.TipoMovimiento
import com.example.appkaiju.stock.ui.components.EtiquetaEstadoStock
import com.example.appkaiju.stock.ui.components.MovimientoItem
import com.example.appkaiju.stock.ui.components.comoClp
import com.example.appkaiju.stock.ui.components.etiqueta
import com.example.appkaiju.stock.viewmodel.ProductDetailViewModel
import com.example.appkaiju.stock.ui.components.ImagenProducto

@Composable
fun ProductDetailScreen(
    viewModel: ProductDetailViewModel,
    esAdmin: Boolean,
    onBack: () -> Unit,
    onRegistrar: (TipoMovimiento) -> Unit,
    onKardex: () -> Unit
) {
    val s by viewModel.estado.collectAsState()
    val producto = s.producto

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
            Text("Detalle de producto", style = MaterialTheme.typography.headlineMedium)
        }

        if (producto == null) {
            Text(
                if (s.cargando) "Cargando..." else "No se encontró el producto",
                modifier = Modifier.padding(16.dp)
            )
        } else {
            ImagenProducto(
                ruta = producto.imagenUri,
                modifier = Modifier.fillMaxWidth().height(180.dp)
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            producto.nombre,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f)
                        )
                        EtiquetaEstadoStock(producto.estadoStock())
                    }
                    Text("Código: ${producto.codigo}", style = MaterialTheme.typography.bodyMedium)
                    if (producto.descripcion.isNotBlank()) {
                        Text(producto.descripcion, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("Tipo: ${producto.tipo.etiqueta()}", style = MaterialTheme.typography.bodyMedium)
                    Text("Precio: ${producto.precio.comoClp()}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Stock actual: ${producto.stockActual} (mínimo ${producto.stockMinimo})",
                        style = MaterialTheme.typography.titleMedium
                    )

                    val atributos = listOfNotNull(
                        producto.talla?.let { "Talla" to it },
                        producto.color?.let { "Color" to it },
                        producto.modelo?.let { "Modelo" to it },
                        producto.lote?.let { "Lote" to it },
                        producto.fechaVencimiento?.let { "Vence" to it }
                    )
                    atributos.forEach { (nombre, valor) ->
                        Text("$nombre: $valor", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (esAdmin) {
                Button(onClick = { onRegistrar(TipoMovimiento.ENTRADA) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Registrar entrada")
                }
                OutlinedButton(onClick = { onRegistrar(TipoMovimiento.SALIDA) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Registrar salida")
                }
                OutlinedButton(onClick = { onRegistrar(TipoMovimiento.AJUSTE) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Ajustar stock por conteo")
                }
            }

            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Últimos movimientos",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (esAdmin) {
                    TextButton(onClick = onKardex) { Text("Ver historial completo") }
                }
            }
            if (s.ultimosMovimientos.isEmpty()) {
                Text("Este producto aún no tiene movimientos")
            } else {
                s.ultimosMovimientos.forEach { MovimientoItem(it) }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}