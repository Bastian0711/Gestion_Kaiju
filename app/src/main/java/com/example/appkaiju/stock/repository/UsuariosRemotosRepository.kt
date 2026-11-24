package com.example.appkaiju.stock.repository

import com.example.appkaiju.stock.data.model.ErrorRemoto
import com.example.appkaiju.stock.data.model.LoginRemotoRequest
import com.example.appkaiju.stock.data.model.UsuarioActualizarRequest
import com.example.appkaiju.stock.data.model.UsuarioCrearRequest
import com.example.appkaiju.stock.data.model.UsuarioRemoto
import com.example.appkaiju.stock.data.remote.UsuariosApi
import com.google.gson.Gson
import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException

sealed class ResultadoRemoto<out T> {
    data class Ok<T>(val datos: T) : ResultadoRemoto<T>()
    data class Error(val mensaje: String) : ResultadoRemoto<Nothing>()
}


class UsuariosRemotosRepository(private val api: UsuariosApi) {

    private var token: String? = null

// True si hay un administrador con sesion iniciada en el microservicio
    val conectado: Boolean get() = token != null

    suspend fun iniciarSesion(correo: String, clave: String): ResultadoRemoto<UsuarioRemoto> {
        val r = llamar { api.login(LoginRemotoRequest(correo.trim().lowercase(), clave)) }
        if (r is ResultadoRemoto.Error) return r
        val respuesta = (r as ResultadoRemoto.Ok).datos
        val usuario = respuesta.usuario
        val accessToken = respuesta.accessToken
        if (usuario == null || accessToken.isNullOrBlank()) {
            return ResultadoRemoto.Error(MENSAJE_RESPUESTA_INVALIDA)
        }
        if (usuario.rol != ROL_ADMIN) {
            return ResultadoRemoto.Error(MENSAJE_SOLO_ADMIN)
        }
        token = "Bearer $accessToken"
        return ResultadoRemoto.Ok(usuario)
    }

    fun cerrarSesion() {
        token = null
    }

    suspend fun listar(): ResultadoRemoto<List<UsuarioRemoto>> = conToken { api.listar(it) }

    suspend fun crear(datos: UsuarioCrearRequest): ResultadoRemoto<UsuarioRemoto> =
        conToken { api.crear(it, datos) }

    suspend fun actualizar(id: Int, datos: UsuarioActualizarRequest): ResultadoRemoto<UsuarioRemoto> =
        conToken { api.actualizar(it, id, datos) }

    suspend fun desactivar(id: Int): ResultadoRemoto<Unit> = conToken { api.desactivar(it, id) }

    private suspend fun <T> conToken(bloque: suspend (String) -> T): ResultadoRemoto<T> {
        val actual = token ?: return ResultadoRemoto.Error(MENSAJE_SIN_SESION)
        return llamar { bloque(actual) }
    }

    private suspend fun <T> llamar(bloque: suspend () -> T): ResultadoRemoto<T> =
        try {
            ResultadoRemoto.Ok(bloque())
        } catch (e: HttpException) {
            // 401: el token vencio o es invalido,  hay que volver a iniciar sesion
            if (e.code() == 401) token = null
            ResultadoRemoto.Error(mensajeDe(e))
        } catch (e: IOException) {
            ResultadoRemoto.Error(MENSAJE_SIN_CONEXION)
        } catch (e: JsonParseException) {
            ResultadoRemoto.Error(MENSAJE_RESPUESTA_INVALIDA)
        }

    private fun mensajeDe(e: HttpException): String {
        val cuerpo = try {
            e.response()?.errorBody()?.string()
        } catch (io: IOException) {
            null
        }
        val error = cuerpo?.let {
            try {
                Gson().fromJson(it, ErrorRemoto::class.java)
            } catch (ex: RuntimeException) {
                null
            }
        }
        val mensaje = error?.mensaje ?: "El servidor respondió con error ${e.code()}"
        val detalles = error?.detalles.orEmpty()
        return if (detalles.isEmpty()) mensaje else mensaje + "\n" + detalles.joinToString("\n")
    }

    companion object {
        const val ROL_ADMIN = "admin"
        const val MENSAJE_SIN_CONEXION = "No se pudo conectar con el microservicio de usuarios"
        const val MENSAJE_RESPUESTA_INVALIDA = "El microservicio respondió con datos inválidos"
        const val MENSAJE_SOLO_ADMIN = "Solo un administrador puede gestionar usuarios"
        const val MENSAJE_SIN_SESION = "Primero inicia sesión en el microservicio"
    }
}