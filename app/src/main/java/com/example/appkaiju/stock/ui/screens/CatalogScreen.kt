package com.example.appkaiju.stock.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.appkaiju.stock.model.EstadoStock
import com.example.appkaiju.stock.model.Producto
import com.example.appkaiju.stock.model.ProductoConCategoria
import com.example.appkaiju.stock.ui.components.EtiquetaDisponibilidad
import com.example.appkaiju.stock.ui.components.ImagenProducto
import com.example.appkaiju.stock.ui.components.comoClp
import com.example.appkaiju.stock.viewmodel.CatalogViewModel

@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel,
    puedeComprar: Boolean,
    unidadesEnCarrito: Int,
    cantidades: Map<Long, Int>,
    onAgregar: (Producto) -> Unit,
    onCarrito: () -> Unit,
    onBack: () -> Unit
) {
    val s by viewModel.estado.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text(
                "Catálogo",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            if (puedeComprar) {
                OutlinedButton(onClick = onCarrito) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Text("  Carrito ($unidadesEnCarrito)")
                }
            }
        }

        OutlinedTextField(
            value = s.texto,
            onValueChange = viewModel::onTextoChange,
            label = { Text("Buscar producto") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = s.categoriaId == null,
                    onClick = { viewModel.onCategoriaChange(null) },
                    label = { Text("Todas") }
                )
            }
            items(s.categorias, key = { it.id }) { categoria ->
                FilterChip(
                    selected = s.categoriaId == categoria.id,
                    onClick = { viewModel.onCategoriaChange(categoria.id) },
                    label = { Text(categoria.nombre) }
                )
            }
        }

        Spacer(Modifier.padding(top = 8.dp))
        if (s.productos.isEmpty()) {
            Text("No se encontraron productos", modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(s.productos, key = { it.producto.id }) { item ->
                    CatalogoItem(
                        item = item,
                        puedeComprar = puedeComprar,
                        enCarrito = cantidades[item.producto.id] ?: 0,
                        onAgregar = { onAgregar(item.producto) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CatalogoItem(
    item: ProductoConCategoria,
    puedeComprar: Boolean,
    enCarrito: Int,
    onAgregar: () -> Unit
) {
    val p = item.producto
    val estado = p.estadoStock()

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp)) {
            ImagenProducto(ruta = p.imagenUri, modifier = Modifier.size(72.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(p.nombre, style = MaterialTheme.typography.titleMedium)
                Text(item.categoriaNombre, style = MaterialTheme.typography.bodySmall)
                if (p.descripcion.isNotBlank()) {
                    Text(
                        p.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(p.precio.comoClp(), style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EtiquetaDisponibilidad(estado)
                    if (puedeComprar) {
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = onAgregar,
                            enabled = estado != EstadoStock.AGOTADO && enCarrito < p.stockActual
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                            Text(if (enCarrito > 0) " ($enCarrito)" else " Agregar")
                        }
                    }
                }
            }
        }
    }
}