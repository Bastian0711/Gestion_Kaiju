package com.example.appkaiju.stock.repository

import com.example.appkaiju.stock.data.model.LoginRemotoResponse
import com.example.appkaiju.stock.data.model.UsuarioActualizarRequest
import com.example.appkaiju.stock.data.model.UsuarioCrearRequest
import com.example.appkaiju.stock.data.model.UsuarioRemoto
import com.example.appkaiju.stock.data.remote.UsuariosApi
import com.google.gson.JsonSyntaxException
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class UsuariosRemotosRepositoryTest : StringSpec({

    val admin = UsuarioRemoto(id = 1, nombre = "Admin", apellidos = "Maestros", correo = "admin@maestros.cl", rol = "admin", activo = true)
    val cliente = UsuarioRemoto(id = 4, nombre = "Ana", apellidos = "Pérez", correo = "cliente@maestros.cl", rol = "cliente", activo = true)

    // Simula una respuesta de error del microservicio con su cuerpo JSON
    fun errorHttp(codigo: Int, json: String) = HttpException(
        Response.error<Any>(codigo, json.toResponseBody("application/json".toMediaType()))
    )

    // Repositorio con un administrador que ya inicio sesion
    suspend fun conectado(api: UsuariosApi): UsuariosRemotosRepository {
        coEvery { api.login(any()) } returns LoginRemotoResponse(accessToken = "abc", usuario = admin)
        return UsuariosRemotosRepository(api).also { it.iniciarSesion("admin@maestros.cl", "Admin123!") }
    }

    "iniciar sesión como admin guarda el token y lo envía como Bearer" {
        val api = mockk<UsuariosApi>()
        coEvery { api.listar("Bearer abc") } returns listOf(admin, cliente)

        runTest {
            val repo = conectado(api)
            repo.conectado shouldBe true
            repo.listar() shouldBe ResultadoRemoto.Ok(listOf(admin, cliente))
        }
        coVerify { api.login(match { it.correo == "admin@maestros.cl" && it.password == "Admin123!" }) }
    }

    "el correo se envía sin espacios y en minúsculas" {
        val api = mockk<UsuariosApi>()
        coEvery { api.login(any()) } returns LoginRemotoResponse(accessToken = "abc", usuario = admin)
        runTest { UsuariosRemotosRepository(api).iniciarSesion("  ADMIN@Maestros.cl ", "x") }
        coVerify { api.login(match { it.correo == "admin@maestros.cl" }) }
    }

    "un usuario que no es admin no puede gestionar usuarios" {
        val api = mockk<UsuariosApi>()
        coEvery { api.login(any()) } returns LoginRemotoResponse(accessToken = "abc", usuario = cliente)
        val repo = UsuariosRemotosRepository(api)
        runTest {
            repo.iniciarSesion("cliente@maestros.cl", "Cliente123!") shouldBe
                    ResultadoRemoto.Error(UsuariosRemotosRepository.MENSAJE_SOLO_ADMIN)
        }
        repo.conectado shouldBe false
    }

    "una respuesta de login sin token es inválida" {
        val api = mockk<UsuariosApi>()
        coEvery { api.login(any()) } returns LoginRemotoResponse(accessToken = null, usuario = admin)
        runTest {
            UsuariosRemotosRepository(api).iniciarSesion("a@b.cl", "x") shouldBe
                    ResultadoRemoto.Error(UsuariosRemotosRepository.MENSAJE_RESPUESTA_INVALIDA)
        }
    }

    "credenciales incorrectas muestran el mensaje del servidor (401)" {
        val api = mockk<UsuariosApi>()
        coEvery { api.login(any()) } throws errorHttp(401, """{"mensaje":"Correo o contrasena incorrectos.","detalles":[]}""")
        runTest {
            UsuariosRemotosRepository(api).iniciarSesion("admin@maestros.cl", "mala") shouldBe
                    ResultadoRemoto.Error("Correo o contrasena incorrectos.")
        }
    }

    "sin conexión con el servidor se informa un mensaje" {
        val api = mockk<UsuariosApi>()
        coEvery { api.login(any()) } throws IOException("timeout")
        runTest {
            UsuariosRemotosRepository(api).iniciarSesion("admin@maestros.cl", "x") shouldBe
                    ResultadoRemoto.Error(UsuariosRemotosRepository.MENSAJE_SIN_CONEXION)
        }
    }

    "una respuesta con JSON inesperado es un error" {
        val api = mockk<UsuariosApi>()
        coEvery { api.login(any()) } throws JsonSyntaxException("formato")
        runTest {
            UsuariosRemotosRepository(api).iniciarSesion("admin@maestros.cl", "x") shouldBe
                    ResultadoRemoto.Error(UsuariosRemotosRepository.MENSAJE_RESPUESTA_INVALIDA)
        }
    }

    "sin iniciar sesión no se llama al servidor" {
        val api = mockk<UsuariosApi>()
        runTest {
            UsuariosRemotosRepository(api).listar() shouldBe
                    ResultadoRemoto.Error(UsuariosRemotosRepository.MENSAJE_SIN_SESION)
        }
        coVerify(exactly = 0) { api.listar(any()) }
    }

    "crear, actualizar y desactivar envían el token y los datos" {
        val api = mockk<UsuariosApi>()
        val nuevo = UsuarioCrearRequest("Ana", "Pérez", "ana@maestros.cl", "Clave123!", null, "vendedor")
        val cambios = UsuarioActualizarRequest("Ana", "Pérez", null, "cliente", false)
        coEvery { api.crear("Bearer abc", nuevo) } returns cliente
        coEvery { api.actualizar("Bearer abc", 4, cambios) } returns cliente.copy(activo = false)
        coEvery { api.desactivar("Bearer abc", 4) } just Runs

        runTest {
            val repo = conectado(api)
            repo.crear(nuevo) shouldBe ResultadoRemoto.Ok(cliente)
            repo.actualizar(4, cambios) shouldBe ResultadoRemoto.Ok(cliente.copy(activo = false))
            repo.desactivar(4) shouldBe ResultadoRemoto.Ok(Unit)
        }
    }

    "un error de validación incluye los detalles del servidor (400)" {
        val api = mockk<UsuariosApi>()
        coEvery { api.crear(any(), any()) } throws errorHttp(
            400, """{"mensaje":"Datos invalidos.","detalles":["correo: formato invalido","nombre: obligatorio"]}"""
        )
        runTest {
            val repo = conectado(api)
            repo.crear(UsuarioCrearRequest("", "", "x", "y", null, "cliente")) shouldBe
                    ResultadoRemoto.Error("Datos invalidos.\ncorreo: formato invalido\nnombre: obligatorio")
        }
    }

    "si el error no trae JSON se informa el código HTTP" {
        val api = mockk<UsuariosApi>()
        coEvery { api.desactivar(any(), any()) } throws errorHttp(502, "<html>Bad Gateway</html>")
        runTest {
            conectado(api).desactivar(4) shouldBe ResultadoRemoto.Error("El servidor respondió con error 502")
        }
    }

    "si el token vence (401) se cierra la sesión" {
        val api = mockk<UsuariosApi>()
        coEvery { api.listar(any()) } throws errorHttp(401, """{"mensaje":"Debes iniciar sesion o tu sesion expiro."}""")
        runTest {
            val repo = conectado(api)
            repo.listar() shouldBe ResultadoRemoto.Error("Debes iniciar sesion o tu sesion expiro.")
            repo.conectado shouldBe false
        }
    }

    "cerrar sesión borra el token" {
        val api = mockk<UsuariosApi>()
        runTest {
            val repo = conectado(api)
            repo.cerrarSesion()
            repo.conectado shouldBe false
        }
    }
})
