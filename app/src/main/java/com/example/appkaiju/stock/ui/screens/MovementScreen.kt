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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.appkaiju.stock.model.TipoMovimiento
import com.example.appkaiju.stock.model.Usuario
import com.example.appkaiju.stock.ui.components.CampoFormulario
import com.example.appkaiju.stock.ui.components.EtiquetaEstadoStock
import com.example.appkaiju.stock.viewmodel.CampoMovimiento
import com.example.appkaiju.stock.viewmodel.MotivosMovimiento
import com.example.appkaiju.stock.viewmodel.MovementViewModel
import kotlinx.coroutines.delay

@Composable
fun MovementScreen(viewModel: MovementViewModel, usuario: Usuario, onBack: () -> Unit) {
    val s by viewModel.estado.collectAsState()
    val esAjuste = s.tipo == TipoMovimiento.AJUSTE

    LaunchedEffect(s.guardadoOk) {
        if (s.guardadoOk) {
            delay(1200)
            onBack()
        }
    }

    val titulo = when (s.tipo) {
        TipoMovimiento.ENTRADA -> "Registrar entrada"
        TipoMovimiento.SALIDA -> "Registrar salida"
        TipoMovimiento.AJUSTE -> "Ajustar stock"
        else -> "Movimiento"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text(titulo, style = MaterialTheme.typography.headlineMedium)
        }

        s.producto?.let { p ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(p.nombre, style = MaterialTheme.typography.titleMedium)
                    Text("Código: ${p.codigo}", style = MaterialTheme.typography.bodySmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Stock actual: ${p.stockActual}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        EtiquetaEstadoStock(p.estadoStock())
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        CampoFormulario(
            valor = s.cantidad,
            etiqueta = if (esAjuste) "Stock contado" else "Cantidad",
            error = s.errores[CampoMovimiento.CANTIDAD],
            icono = Icons.Default.Inventory2,
            teclado = KeyboardType.Number
        ) { viewModel.onCantidadChange(it) }

        if (!esAjuste) {
            Spacer(Modifier.height(8.dp))
            Text("Motivo", style = MaterialTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(MotivosMovimiento.para(s.tipo)) { motivo ->
                    FilterChip(
                        selected = s.motivo == motivo,
                        onClick = { viewModel.onMotivoChange(motivo) },
                        label = { Text(motivo) }
                    )
                }
            }
            s.errores[CampoMovimiento.MOTIVO]?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
        }

        AnimatedVisibility(visible = s.requiereTexto) {
            CampoFormulario(
                valor = s.motivoTexto,
                etiqueta = if (esAjuste) "Motivo del ajuste (mín. 5 caracteres)" else "Detalle del motivo (mín. 5 caracteres)",
                error = s.errores[CampoMovimiento.MOTIVO_TEXTO],
                icono = Icons.Default.Edit
            ) { viewModel.onMotivoTextoChange(it) }
        }

        val actual = s.producto?.stockActual
        val diferencia = s.diferencia
        val resultante = s.stockResultante
        if (actual != null && diferencia != null) {
            val signo = if (diferencia > 0) "+$diferencia" else "$diferencia"
            Text(
                "Registrado: $actual · Contado: ${s.cantidad} · Diferencia: $signo",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else if (actual != null && resultante != null) {
            Text(
                "Stock actual: $actual → Stock resultante: $resultante",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { viewModel.guardar(usuario) },
            enabled = !s.guardando && !s.guardadoOk && s.producto != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (s.guardando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Confirmar")
            }
        }

        s.errorGeneral?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        AnimatedVisibility(visible = s.mensajeExito != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    s.mensajeExito.orEmpty(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}