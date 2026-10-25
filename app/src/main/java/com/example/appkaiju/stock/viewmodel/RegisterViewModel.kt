package com.example.appkaiju.stock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appkaiju.stock.model.Usuario
import com.example.appkaiju.stock.repository.ResultadoRegistro
import com.example.appkaiju.stock.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val nombre: String = "",
    val correo: String = "",
    val clave: String = "",
    val confirmacion: String = "",
    val nombreError: String? = null,
    val correoError: String? = null,
    val claveError: String? = null,
    val confirmacionError: String? = null,
    val errorGeneral: String? = null,
    val cargando: Boolean = false,
    val usuarioCreado: Usuario? = null
)

class RegisterViewModel(private val repositorio: UsuarioRepository) : ViewModel() {

    private val _estado = MutableStateFlow(RegisterUiState())
    val estado: StateFlow<RegisterUiState> = _estado.asStateFlow()

    fun onNombreChange(valor: String) {
        _estado.update { it.copy(nombre = valor.take(40), nombreError = null, errorGeneral = null) }
    }

    fun onCorreoChange(valor: String) {
        _estado.update { it.copy(correo = valor, correoError = null, errorGeneral = null) }
    }

    fun onClaveChange(valor: String) {
        _estado.update { it.copy(clave = valor, claveError = null, errorGeneral = null) }
    }

    fun onConfirmacionChange(valor: String) {
        _estado.update { it.copy(confirmacion = valor, confirmacionError = null, errorGeneral = null) }
    }

    private fun validar(): Boolean {
        val s = _estado.value
        val errorNombre = when {
            s.nombre.isBlank() -> "Ingresa tu nombre"
            s.nombre.trim().length < 3 -> "Debe tener al menos 3 caracteres"
            else -> null
        }
        val errorCorreo = when {
            s.correo.isBlank() -> "Ingresa tu correo"
            !REGEX_CORREO.matches(s.correo.trim()) -> "El correo no es válido"
            else -> null
        }
        val errorClave = when {
            s.clave.isBlank() -> "Crea una contraseña"
            s.clave.length < 6 -> "Debe tener al menos 6 caracteres"
            !s.clave.any { it.isLetter() } || !s.clave.any { it.isDigit() } -> "Usa letras y números"
            else -> null
        }
        val errorConfirmacion = when {
            s.confirmacion.isBlank() -> "Repite la contraseña"
            s.confirmacion != s.clave -> "Las contraseñas no coinciden"
            else -> null
        }
        _estado.update {
            it.copy(
                nombreError = errorNombre,
                correoError = errorCorreo,
                claveError = errorClave,
                confirmacionError = errorConfirmacion
            )
        }
        return listOf(errorNombre, errorCorreo, errorClave, errorConfirmacion).all { it == null }
    }

    fun registrar() {
        if (_estado.value.cargando) return
        if (!validar()) return
        val s = _estado.value
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, errorGeneral = null) }
            when (val r = repositorio.registrarCliente(s.nombre, s.correo, s.clave)) {
                is ResultadoRegistro.Ok ->
                    _estado.update { it.copy(cargando = false, usuarioCreado = r.usuario) }
                is ResultadoRegistro.Error ->
                    _estado.update { it.copy(cargando = false, correoError = r.mensaje) }
            }
        }
    }

    companion object {
        private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}