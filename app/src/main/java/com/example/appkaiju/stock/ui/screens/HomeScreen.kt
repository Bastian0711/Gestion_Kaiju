package com.example.appkaiju.stock.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(onProductsClick: () -> Unit, onCatalogClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Inicio", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onProductsClick, modifier = Modifier.fillMaxWidth()) {
            Text("Productos")
        }
        Button(onClick = onCatalogClick, modifier = Modifier.fillMaxWidth()) {
            Text("Catálogo público")
        }
    }
}
