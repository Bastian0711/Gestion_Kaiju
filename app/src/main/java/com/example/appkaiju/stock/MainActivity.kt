package com.example.appkaiju.stock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.appkaiju.stock.data.AppDatabase
import com.example.appkaiju.stock.navigation.AppNavigation
import com.example.appkaiju.stock.repository.UsuarioRepository
import com.example.appkaiju.stock.viewmodel.AuthViewModel
import com.example.appkaiju.stock.viewmodel.AuthViewModelFactory

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            UsuarioRepository(AppDatabase.getInstance(applicationContext).usuarioDao())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface { AppNavigation(authViewModel) }
            }
        }
    }
}