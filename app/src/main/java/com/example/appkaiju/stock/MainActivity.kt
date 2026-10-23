package com.example.appkaiju.stock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.appkaiju.stock.data.AppContainer
import com.example.appkaiju.stock.navigation.AppNavigation
import com.example.appkaiju.stock.viewmodel.AuthViewModel
import com.example.appkaiju.stock.viewmodel.AuthViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var container: AppContainer

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(container.usuarioRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        container = AppContainer(applicationContext)
        lifecycleScope.launch { container.productoRepository.crearDatosIniciales() }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.safeDrawingPadding()) {
                        AppNavigation(authViewModel, container)
                    }
                }
            }
        }
    }
}