package com.example.appkaiju.stock.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appkaiju.stock.model.Producto
import com.example.appkaiju.stock.model.Usuario
import com.example.appkaiju.stock.ui.components.comoClp
import com.example.appkaiju.stock.viewmodel.SaleViewModel

private const val MAX_RESULTADOS = 5

@Composable
fun SaleScreen(
    viewModel: SaleViewModel,
    usuario: Usuario,
    onBack: () -> Unit,
    onVentaRegistrada: (Long) -> Unit
) {
    val s by viewModel.estado.collectAsState()

    LaunchedEffect(s.ventaRegistradaId) {
        s.ventaRegistradaId?.let(onVentaRegistrada)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text("Nueva venta", style = MaterialTheme.typography.headlineMedium)
            }
        }
        item {
            OutlinedTextField(
                value = s.texto,
                onValueChange = viewModel::onTextoChange,
                label = { Text("Buscar producto por nombre o código") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item { Text("Productos con stock", style = MaterialTheme.typography.titleSmall) }

        if (s.resultados.isEmpty()) {
            item { Text("No se encontraron productos con stock") }
        }
        items(s.resultados.take(MAX_RESULTADOS), key = { "producto_${it.id}" }) { producto ->
            val enVenta = s.lineas.find { it.producto.id == producto.id }?.cantidad ?: 0
            FilaProductoVenta(
                producto = producto,
                enVenta = enVenta,
                onAgregar = { viewModel.agregar(producto) }
            )
        }
        if (s.resultados.size > MAX_RESULTADOS) {
            item {
                Text(
                    "Mostrando $MAX_RESULTADOS de ${s.resultados.size}: escribe para filtrar",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
        item { Text("Venta actual", style = MaterialTheme.typography.titleMedium) }
        if (s.lineas.isEmpty()) {
            item { Text("Aún no agregas productos") }
        }
        items(s.lineas, key = { "linea_${it.producto.id}" }) { item ->
            FilaCarrito(
                item = item,
                onMas = { viewModel.cambiarCantidad(item.producto.id, 1) },
                onMenos = { viewModel.cambiarCantidad(item.producto.id, -1) },
                onQuitar = { viewModel.quitar(item.producto.id) }
            )
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                Text("Total", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                Text(s.total.comoClp(), style = MaterialTheme.typography.titleLarge)
            }
        }
        item {
            AnimatedVisibility(visible = s.errorGeneral != null) {
                Text(
                    text = s.errorGeneral.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        item {
            Button(
                onClick = { viewModel.confirmar(usuario) },
                enabled = !s.guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (s.guardando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Confirmar venta")
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun FilaProductoVenta(producto: Producto, enVenta: Int, onAgregar: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.nombre, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${producto.codigo} · ${producto.precio.comoClp()} · Stock: ${producto.stockActual}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(onClick = onAgregar, enabled = enVenta < producto.stockActual) {
                Text(if (enVenta > 0) "Agregar ($enVenta)" else "Agregar")
            }
        }
    }
}