package com.example.appkaiju.stock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appkaiju.stock.data.model.UsuarioActualizarRequest
import com.example.appkaiju.stock.data.model.UsuarioCrearRequest
import com.example.appkaiju.stock.data.model.UsuarioRemoto
import com.example.appkaiju.stock.repository.ResultadoRemoto
import com.example.appkaiju.stock.repository.UsuariosRemotosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CampoUsuario { NOMBRE, APELLIDOS, CORREO, CLAVE }

data class FormularioUsuario(
    val id: Int? = null,
    val nombre: String = "",
    val apellidos: String = "",
    val correo: String = "",
    val clave: String = "",
    val direccion: String = "",
    val rol: String = "cliente",
    val activo: Boolean = true,
    val errores: Map<CampoUsuario, String> = emptyMap()
) {
    val esNuevo: Boolean get() = id == null
}

data class UsuariosRemotosState(
    val conectado: Boolean = false,
    val correoAdmin: String = "",
    val claveAdmin: String = "",
    val cargando: Boolean = false,
    val usuarios: List<UsuarioRemoto> = emptyList(),
    val formulario: FormularioUsuario? = null,
    val error: String? = null,
    val mensaje: String? = null
)

class UsuariosRemotosViewModel(private val repositorio: UsuariosRemotosRepository) : ViewModel() {

    private val _estado = MutableStateFlow(UsuariosRemotosState(conectado = repositorio.conectado))
    val estado: StateFlow<UsuariosRemotosState> = _estado.asStateFlow()

    init {
        // Si ya había una sesión abierta en el microservicio, se carga la lista
        if (repositorio.conectado) cargar()
    }

    fun onCorreoAdminChange(valor: String) = _estado.update { it.copy(correoAdmin = valor, error = null) }

    fun onClaveAdminChange(valor: String) = _estado.update { it.copy(claveAdmin = valor, error = null) }

    fun conectar() {
        val s = _estado.value
        if (s.cargando) return
        if (s.correoAdmin.isBlank() || s.claveAdmin.isBlank()) {
            _estado.update { it.copy(error = "Ingresa el correo y la contraseña del administrador") }
            return
        }
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            when (val r = repositorio.iniciarSesion(s.correoAdmin, s.claveAdmin)) {
                is ResultadoRemoto.Ok -> {
                    _estado.update { it.copy(conectado = true, claveAdmin = "", cargando = false) }
                    cargar()
                }
                is ResultadoRemoto.Error -> _estado.update { it.copy(cargando = false, error = r.mensaje) }
            }
        }
    }

    fun desconectar() {
        repositorio.cerrarSesion()
        _estado.value = UsuariosRemotosState()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            when (val r = repositorio.listar()) {
                is ResultadoRemoto.Ok -> _estado.update { it.copy(cargando = false, usuarios = r.datos) }
                is ResultadoRemoto.Error -> mostrarError(r.mensaje)
            }
        }
    }

    fun nuevo() = _estado.update { it.copy(formulario = FormularioUsuario(), mensaje = null) }

    fun editar(usuario: UsuarioRemoto) = _estado.update {
        it.copy(
            mensaje = null,
            formulario = FormularioUsuario(
                id = usuario.id,
                nombre = usuario.nombre.orEmpty(),
                apellidos = usuario.apellidos.orEmpty(),
                correo = usuario.correo.orEmpty(),
                direccion = usuario.direccion.orEmpty(),
                rol = usuario.rol ?: "cliente",
                activo = usuario.activo ?: true
            )
        )
    }

    fun onFormularioChange(formulario: FormularioUsuario) = _estado.update {
        it.copy(formulario = formulario.copy(errores = emptyMap()))
    }

    fun cancelarFormulario() = _estado.update { it.copy(formulario = null) }

    fun guardar() {
        val f = _estado.value.formulario ?: return
        if (_estado.value.cargando) return
        val errores = validar(f)
        if (errores.isNotEmpty()) {
            _estado.update { it.copy(formulario = f.copy(errores = errores)) }
            return
        }
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            val direccion = f.direccion.trim().ifBlank { null }
            val r = if (f.id == null) {
                repositorio.crear(
                    UsuarioCrearRequest(
                        nombre = f.nombre.trim(),
                        apellidos = f.apellidos.trim(),
                        correo = f.correo.trim().lowercase(),
                        password = f.clave,
                        direccion = direccion,
                        rol = f.rol
                    )
                )
            } else {
                repositorio.actualizar(
                    f.id,
                    UsuarioActualizarRequest(
                        nombre = f.nombre.trim(),
                        apellidos = f.apellidos.trim(),
                        direccion = direccion,
                        rol = f.rol,
                        activo = f.activo
                    )
                )
            }
            when (r) {
                is ResultadoRemoto.Ok -> {
                    _estado.update {
                        it.copy(
                            formulario = null,
                            mensaje = if (f.esNuevo) "Usuario creado" else "Usuario actualizado"
                        )
                    }
                    cargar()
                }
                is ResultadoRemoto.Error -> mostrarError(r.mensaje)
            }
        }
    }

    fun desactivar(usuario: UsuarioRemoto) {
        val id = usuario.id ?: return
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null, mensaje = null) }
            when (val r = repositorio.desactivar(id)) {
                is ResultadoRemoto.Ok -> {
                    _estado.update { it.copy(mensaje = "Usuario desactivado") }
                    cargar()
                }
                is ResultadoRemoto.Error -> mostrarError(r.mensaje)
            }
        }
    }

    private fun mostrarError(mensaje: String) = _estado.update {
        // Si el token venció, el repositorio cierra la sesión y se vuelve al login
        it.copy(cargando = false, error = mensaje, conectado = repositorio.conectado)
    }

    private fun validar(f: FormularioUsuario): Map<CampoUsuario, String> {
        val e = mutableMapOf<CampoUsuario, String>()
        if (f.nombre.trim().length !in 2..50) e[CampoUsuario.NOMBRE] = "Entre 2 y 50 caracteres"
        if (f.apellidos.trim().length !in 2..100) e[CampoUsuario.APELLIDOS] = "Entre 2 y 100 caracteres"
        if (f.esNuevo) {
            if (!REGEX_CORREO.matches(f.correo.trim())) e[CampoUsuario.CORREO] = "El correo no es válido"
            if (!REGEX_CLAVE.matches(f.clave)) {
                e[CampoUsuario.CLAVE] = "Mínimo 8 caracteres, con mayúscula, minúscula, número y símbolo"
            }
        }
        return e
    }

    companion object {
        val ROLES = listOf("admin", "vendedor", "contratista", "cliente")
        private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        // Misma regla que el microservicio (Validaciones.CLAVE_FUERTE)
        private val REGEX_CLAVE = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$")
    }
}