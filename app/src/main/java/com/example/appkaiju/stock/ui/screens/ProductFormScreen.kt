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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.appkaiju.stock.model.TipoProducto
import com.example.appkaiju.stock.viewmodel.CampoProducto
import com.example.appkaiju.stock.viewmodel.ProductFormViewModel
import com.example.appkaiju.stock.ui.components.SeccionFoto

@Composable
fun ProductFormScreen(viewModel: ProductFormViewModel, usuarioId: Long, onBack: () -> Unit) {
    val s by viewModel.estado.collectAsState()

    LaunchedEffect(s.guardadoOk) {
        if (s.guardadoOk) onBack()
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
            Text("Nuevo producto", style = MaterialTheme.typography.headlineMedium)
        }

        Campo(s.codigo, "Código (ej: VES-010)", s.errores[CampoProducto.CODIGO], Icons.Default.QrCode) {
            viewModel.onCampoChange(CampoProducto.CODIGO, it)
        }
        Campo(s.nombre, "Nombre", s.errores[CampoProducto.NOMBRE], Icons.Default.Edit) {
            viewModel.onCampoChange(CampoProducto.NOMBRE, it)
        }
        Campo(s.descripcion, "Descripción (opcional)", null, Icons.Default.Info) {
            viewModel.onCampoChange(CampoProducto.DESCRIPCION, it)
        }

        Text("Categoría", style = MaterialTheme.typography.titleSmall)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(s.categorias, key = { it.id }) { categoria ->
                FilterChip(
                    selected = s.categoriaId == categoria.id,
                    onClick = { viewModel.onCategoriaChange(categoria.id) },
                    label = { Text(categoria.nombre) }
                )
            }
        }
        s.errores[CampoProducto.CATEGORIA]?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(12.dp))
        Text("Tipo de producto", style = MaterialTheme.typography.titleSmall)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(TipoProducto.entries.toList()) { tipo ->
                FilterChip(
                    selected = s.tipo == tipo,
                    onClick = { viewModel.onTipoChange(tipo) },
                    label = { Text(etiquetaTipo(tipo)) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        AnimatedVisibility(visible = s.tipo == TipoProducto.VESTUARIO) {
            Column {
                Campo(s.talla, "Talla (opcional)", null, Icons.Default.Edit) {
                    viewModel.onCampoChange(CampoProducto.TALLA, it)
                }
                Campo(s.color, "Color (opcional)", null, Icons.Default.Edit) {
                    viewModel.onCampoChange(CampoProducto.COLOR, it)
                }
            }
        }
        AnimatedVisibility(visible = s.tipo == TipoProducto.TECNOLOGIA) {
            Campo(s.modelo, "Modelo (opcional)", null, Icons.Default.Edit) {
                viewModel.onCampoChange(CampoProducto.MODELO, it)
            }
        }
        AnimatedVisibility(visible = s.tipo == TipoProducto.INSUMO_PERECIBLE) {
            Column {
                Campo(s.lote, "Lote", s.errores[CampoProducto.LOTE], Icons.Default.Edit) {
                    viewModel.onCampoChange(CampoProducto.LOTE, it)
                }
                Campo(
                    s.fechaVencimiento, "Vencimiento (dd-MM-aaaa)",
                    s.errores[CampoProducto.FECHA_VENC], Icons.Default.DateRange
                ) { viewModel.onCampoChange(CampoProducto.FECHA_VENC, it) }
            }
        }

        Campo(s.precio, "Precio (CLP)", s.errores[CampoProducto.PRECIO], Icons.Default.AttachMoney, KeyboardType.Number) {
            viewModel.onCampoChange(CampoProducto.PRECIO, it)
        }
        Campo(s.stockInicial, "Stock inicial", s.errores[CampoProducto.STOCK_INICIAL], Icons.Default.Inventory2, KeyboardType.Number) {
            viewModel.onCampoChange(CampoProducto.STOCK_INICIAL, it)
        }
        Campo(s.stockMinimo, "Stock mínimo", s.errores[CampoProducto.STOCK_MINIMO], Icons.Default.Warning, KeyboardType.Number) {
            viewModel.onCampoChange(CampoProducto.STOCK_MINIMO, it)
        }

        SeccionFoto(ruta = s.imagenPath, onFotoChange = viewModel::onFotoChange)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { viewModel.guardar(usuarioId) },
            enabled = !s.guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (s.guardando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Guardar producto")
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Campo(
    valor: String,
    etiqueta: String,
    error: String?,
    icono: ImageVector,
    teclado: KeyboardType = KeyboardType.Text,
    onCambio: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        isError = error != null,
        supportingText = { error?.let { Text(it) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
    )
}

private fun etiquetaTipo(tipo: TipoProducto): String = when (tipo) {
    TipoProducto.VESTUARIO -> "Vestuario"
    TipoProducto.TECNOLOGIA -> "Tecnología"
    TipoProducto.INSUMO_PERECIBLE -> "Insumo perecible"
    TipoProducto.GENERAL -> "General"
}