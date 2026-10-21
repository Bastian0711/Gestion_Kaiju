package com.example.appkaiju.stock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.appkaiju.stock.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Más adelante se reemplaza por el tema propio de Kaiju (colores/tipografía)
            MaterialTheme {
                Surface { AppNavigation() }
            }
        }
    }
}
