package com.example.appkaiju.stock.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
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
import com.example.appkaiju.stock.ui.components.ImagenProducto
import com.example.appkaiju.stock.ui.components.comoClp
import com.example.appkaiju.stock.viewmodel.CartViewModel
import com.example.appkaiju.stock.viewmodel.ItemCarrito

@Composable
fun CartScreen(
    viewModel: CartViewModel,
    onBack: () -> Unit,
    onIrAlCatalogo: () -> Unit
) {
    val s by viewModel.estado.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text("Mi carrito", style = MaterialTheme.typography.headlineMedium)
        }

        AnimatedVisibility(
            visible = s.compraConfirmada,
            enter = fadeIn() + expandVertically()
        ) {
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "  ¡Pedido registrado!",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text("Total: ${s.totalConfirmado.comoClp()}", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Esta app no procesa pagos. Acércate al local de Kaiju para pagar y retirar " +
                                "tu compra (compra presencial). Los productos no quedan reservados: " +
                                "la disponibilidad puede cambiar hasta que pagues en el local.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = {
                            viewModel.limpiar()
                            onBack()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Entendido") }
                }
            }
        }

        if (!s.compraConfirmada) {
            if (s.items.isEmpty()) {
                Text("Tu carrito está vacío", modifier = Modifier.padding(vertical = 16.dp))
                Button(onClick = onIrAlCatalogo) { Text("Ir al catálogo") }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(s.items, key = { it.producto.id }) { item ->
                        FilaCarrito(
                            item = item,
                            onMas = { viewModel.cambiarCantidad(item.producto.id, 1) },
                            onMenos = { viewModel.cambiarCantidad(item.producto.id, -1) },
                            onQuitar = { viewModel.quitar(item.producto.id) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Total", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    Text(s.total.comoClp(), style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = viewModel::confirmar, modifier = Modifier.fillMaxWidth()) {
                    Text("Confirmar compra")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun FilaCarrito(
    item: ItemCarrito,
    onMas: () -> Unit,
    onMenos: () -> Unit,
    onQuitar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            ImagenProducto(ruta = item.producto.imagenUri, modifier = Modifier.size(56.dp))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.producto.nombre, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${item.producto.precio.comoClp()} c/u · Subtotal ${item.subtotal.comoClp()}",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onMenos) { Text("−", style = MaterialTheme.typography.titleLarge) }
                    Text("${item.cantidad}", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onMas) { Icon(Icons.Default.Add, contentDescription = "Sumar uno") }
                }
            }
            IconButton(onClick = onQuitar) {
                Icon(Icons.Default.Delete, contentDescription = "Quitar del carrito")
            }
        }
    }
}