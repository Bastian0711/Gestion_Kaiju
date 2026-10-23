package com.example.appkaiju.stock.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appkaiju.stock.model.EstadoStock
import com.example.appkaiju.stock.model.Producto
import com.example.appkaiju.stock.ui.components.comoClp
import com.example.appkaiju.stock.viewmodel.ProductListViewModel
import androidx.compose.material3.Button

@Composable
fun ProductListScreen(viewModel: ProductListViewModel, onBack: () -> Unit, onNuevoProducto: () -> Unit)  {
    val productos by viewModel.productos.collectAsState()
    val texto by viewModel.texto.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text(
                "Productos",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = onNuevoProducto) { Text("Nuevo") }
        }

        OutlinedTextField(
            value = texto,
            onValueChange = viewModel::onTextoChange,
            label = { Text("Buscar por nombre o código") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        if (productos.isEmpty()) {
            Text("No se encontraron productos", modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(productos, key = { it.id }) { producto ->
                    ProductoItem(producto)
                }
            }
        }
    }
}

@Composable
private fun ProductoItem(producto: Producto) {
    val estado = producto.estadoStock()
    val colores = MaterialTheme.colorScheme

    val colorEtiqueta by animateColorAsState(
        targetValue = when (estado) {
            EstadoStock.NORMAL -> colores.secondaryContainer
            EstadoStock.BAJO -> colores.tertiaryContainer
            EstadoStock.AGOTADO -> colores.errorContainer
        },
        label = "colorEstadoStock"
    )

    val textoEstado = when (estado) {
        EstadoStock.NORMAL -> "Normal"
        EstadoStock.BAJO -> "Stock bajo"
        EstadoStock.AGOTADO -> "Agotado"
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.nombre, style = MaterialTheme.typography.titleMedium)
                Text("Código: ${producto.codigo}", style = MaterialTheme.typography.bodySmall)
                Text(
                    "${producto.precio.comoClp()} · Stock: ${producto.stockActual} (mín. ${producto.stockMinimo})",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Surface(color = colorEtiqueta, shape = RoundedCornerShape(50)) {
                Text(
                    text = textoEstado,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}