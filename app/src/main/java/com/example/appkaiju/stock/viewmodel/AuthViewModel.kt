package com.example.appkaiju.stock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.appkaiju.stock.model.Usuario
import com.example.appkaiju.stock.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay


data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val correoError: String? = null,
    val claveError: String? = null,
    val errorGeneral: String? = null,
    val cargando: Boolean = false,
    val usuario: Usuario? = null
)

class AuthViewModel(private val repositorio: UsuarioRepository) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    private var intentosFallidos = 0
    private var bloqueadoHasta = 0L

    private var cuentaJob: Job? = null

    init {
        viewModelScope.launch { repositorio.crearUsuariosIniciales() }
    }

    fun onCorreoChange(valor: String) {
        _estado.update { it.copy(correo = valor, correoError = null, errorGeneral = null) }
    }

    fun onClaveChange(valor: String) {
        _estado.update { it.copy(clave = valor, claveError = null, errorGeneral = null) }
    }

    private fun validarCampos(): Boolean {
        val actual = _estado.value
        val errorCorreo = when {
            actual.correo.isBlank() -> "Ingresa tu correo"
            !REGEX_CORREO.matches(actual.correo.trim()) -> "El correo no es válido"
            else -> null
        }
        val errorClave = when {
            actual.clave.isBlank() -> "Ingresa tu contraseña"
            actual.clave.length < 6 -> "Debe tener al menos 6 caracteres"
            else -> null
        }
        _estado.update { it.copy(correoError = errorCorreo, claveError = errorClave) }
        return errorCorreo == null && errorClave == null
    }

    fun iniciarSesion() {

        if (System.currentTimeMillis() < bloqueadoHasta) {
            iniciarCuentaRegresiva()
            return
        }
        if (!validarCampos()) return

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, errorGeneral = null) }
            val actual = _estado.value
            val usuario = repositorio.autenticar(actual.correo.trim(), actual.clave)
            if (usuario != null) {
                intentosFallidos = 0
                _estado.update { it.copy(cargando = false, usuario = usuario) }
            } else {
                intentosFallidos++
                val bloquear = intentosFallidos >= 5
                if (bloquear) {
                    bloqueadoHasta = System.currentTimeMillis() + 60_000
                    intentosFallidos = 0
                }
                _estado.update {
                    it.copy(cargando = false, errorGeneral = "Correo o contraseña incorrectos")
                }
                if (bloquear) iniciarCuentaRegresiva()
            }
        }
    }
    private fun iniciarCuentaRegresiva() {
        cuentaJob?.cancel()
        cuentaJob = viewModelScope.launch {
            while (true) {
                val restante = (bloqueadoHasta - System.currentTimeMillis() + 999) / 1000
                if (restante <= 0) {
                    _estado.update { it.copy(errorGeneral = null) }
                    break
                }
                _estado.update { it.copy(errorGeneral = "Demasiados intentos. Espera $restante s") }
                delay(1000)
            }
        }
    }

    fun cerrarSesion() {
        _estado.value = LoginUiState()
    }

    companion object {
        private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }

    fun establecerSesion(usuario: Usuario) {
        _estado.update { it.copy(usuario = usuario) }
    }
}


class AuthViewModelFactory(private val repositorio: UsuarioRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(repositorio) as T
}

