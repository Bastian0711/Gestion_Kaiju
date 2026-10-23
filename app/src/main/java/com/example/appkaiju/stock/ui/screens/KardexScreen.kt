package com.example.appkaiju.stock.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilterChip
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
import com.example.appkaiju.stock.model.TipoMovimiento
import com.example.appkaiju.stock.ui.components.MovimientoItem
import com.example.appkaiju.stock.ui.components.etiqueta
import com.example.appkaiju.stock.viewmodel.KardexViewModel

@Composable
fun KardexScreen(viewModel: KardexViewModel, onBack: () -> Unit) {
    val s by viewModel.estado.collectAsState()
    val opciones: List<TipoMovimiento?> = listOf(null) + TipoMovimiento.entries

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Column {
                Text("Historial de movimientos", style = MaterialTheme.typography.headlineSmall)
                s.producto?.let {
                    Text("${it.nombre} · ${it.codigo}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(opciones) { tipo ->
                FilterChip(
                    selected = s.filtro == tipo,
                    onClick = { viewModel.onFiltroChange(tipo) },
                    label = { Text(tipo?.etiqueta() ?: "Todos") }
                )
            }
        }

        if (s.movimientos.isEmpty()) {
            Text("No hay movimientos para este filtro", modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(s.movimientos, key = { it.id }) { mov ->
                    MovimientoItem(mov, nombreUsuario = s.nombresUsuarios[mov.usuarioId])
                }
            }
        }
    }
}