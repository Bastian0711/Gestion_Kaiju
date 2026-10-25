package com.example.appkaiju.stock.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appkaiju.stock.model.Rol
import com.example.appkaiju.stock.model.Usuario

@Composable
fun HomeScreen(
    usuario: Usuario?,
    onProductsClick: () -> Unit,
    onVentasClick: () -> Unit,
    onCatalogClick: () -> Unit,
    onCartClick: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Inicio", style = MaterialTheme.typography.headlineMedium)
        if (usuario != null) {
            Text("Hola, ${usuario.nombre}", style = MaterialTheme.typography.titleMedium)
            Text(
                if (usuario.rol == Rol.ADMIN) "Perfil: Administrador" else "Perfil: Cliente",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (usuario?.rol == Rol.ADMIN) {
            Button(onClick = onProductsClick, modifier = Modifier.fillMaxWidth()) { Text("Productos") }
            Button(onClick = onVentasClick, modifier = Modifier.fillMaxWidth()) { Text("Ventas") }
        }
        Button(onClick = onCatalogClick, modifier = Modifier.fillMaxWidth()) { Text("Catálogo") }
        if (usuario?.rol == Rol.CLIENTE) {
            Button(onClick = onCartClick, modifier = Modifier.fillMaxWidth()) { Text("Mi carrito") }
        }

        Spacer(Modifier.weight(1f))
        OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar sesión")
        }
    }
}