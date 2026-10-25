package com.example.appkaiju.stock.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.appkaiju.stock.model.EstadoStock
import com.example.appkaiju.stock.model.Rol
import com.example.appkaiju.stock.model.Usuario
import com.example.appkaiju.stock.ui.components.comoClp
import com.example.appkaiju.stock.viewmodel.ResumenState

@Composable
fun HomeScreen(
    usuario: Usuario?,
    resumen: ResumenState,
    onProductsClick: () -> Unit,
    onVentasClick: () -> Unit,
    onCatalogClick: () -> Unit,
    onCartClick: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val esAdmin = usuario?.rol == Rol.ADMIN

    // Las tarjetas aparecen con una animación al entrar a Inicio
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Inicio", style = MaterialTheme.typography.headlineMedium)
            if (usuario != null) {
                Text("Hola, ${usuario.nombre}", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (esAdmin) "Perfil: Administrador" else "Perfil: Cliente",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (esAdmin) {
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TarjetaIndicador(
                                titulo = "Productos activos",
                                valor = resumen.productosActivos,
                                icono = Icons.Default.Inventory2,
                                modifier = Modifier.weight(1f)
                            )
                            TarjetaIndicador(
                                titulo = "Ventas hoy",
                                valor = resumen.ventasHoy,
                                icono = Icons.Default.PointOfSale,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TarjetaIndicador(
                                titulo = "Stock bajo",
                                valor = resumen.stockBajo,
                                icono = Icons.Default.Warning,
                                alerta = resumen.stockBajo > 0,
                                modifier = Modifier.weight(1f)
                            )
                            TarjetaIndicador(
                                titulo = "Agotados",
                                valor = resumen.agotados,
                                icono = Icons.Default.Warning,
                                alerta = resumen.agotados > 0,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Text(
                            "Vendido hoy: ${resumen.montoHoy.comoClp()}",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }

            item {
                Button(onClick = onProductsClick, modifier = Modifier.fillMaxWidth()) { Text("Productos") }
            }
            item {
                Button(onClick = onVentasClick, modifier = Modifier.fillMaxWidth()) { Text("Ventas") }
            }
            item {
                Button(onClick = onCatalogClick, modifier = Modifier.fillMaxWidth()) { Text("Catálogo") }
            }

            item {
                Spacer(Modifier.height(6.dp))
                Text("Lista de reposición", style = MaterialTheme.typography.titleMedium)
            }
            if (resumen.reposicion.isEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Text(
                            "  Todo el stock está sobre el mínimo",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            items(resumen.reposicion, key = { "repo_${it.id}" }) { producto ->
                val agotado = producto.estadoStock() == EstadoStock.AGOTADO
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (agotado) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(producto.nombre, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${producto.codigo} · Mínimo: ${producto.stockMinimo}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            if (agotado) "Agotado" else "Quedan ${producto.stockActual}",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
        } else {
            item {
                Button(onClick = onCatalogClick, modifier = Modifier.fillMaxWidth()) { Text("Catálogo") }
            }
            if (usuario?.rol == Rol.CLIENTE) {
                item {
                    Button(onClick = onCartClick, modifier = Modifier.fillMaxWidth()) { Text("Mi carrito") }
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar sesión")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TarjetaIndicador(
    titulo: String,
    valor: Int,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    alerta: Boolean = false
) {
    val valorAnimado by animateIntAsState(
        targetValue = valor,
        animationSpec = tween(800),
        label = "indicador"
    )
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (alerta) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(icono, contentDescription = null, tint = Color.Unspecified.takeIf { false } ?: MaterialTheme.colorScheme.primary)
            Text(valorAnimado.toString(), style = MaterialTheme.typography.headlineMedium)
            Text(titulo, style = MaterialTheme.typography.bodySmall)
        }
    }
}