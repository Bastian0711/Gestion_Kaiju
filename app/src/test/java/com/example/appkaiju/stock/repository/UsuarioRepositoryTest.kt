package com.example.appkaiju.stock.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.appkaiju.stock.data.PasswordHasher
import com.example.appkaiju.stock.data.UsuarioDao
import com.example.appkaiju.stock.model.Rol
import com.example.appkaiju.stock.model.Usuario
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest

class UsuarioRepositoryTest : StringSpec({

    val hashAdmin = PasswordHasher.hashear("Admin123")
    val admin = Usuario(
        id = 1, nombre = "Administrador Kaiju", correo = "admin@kaiju.cl",
        passwordHash = hashAdmin, rol = Rol.ADMIN
    )

    "autenticar devuelve el usuario con credenciales correctas (ignora mayúsculas del correo)" {
        val dao = mockk<UsuarioDao>()
        coEvery { dao.buscarPorCorreo("admin@kaiju.cl") } returns admin
        val repo = UsuarioRepository(dao)

        runTest {
            repo.autenticar("ADMIN@kaiju.cl", "Admin123") shouldBe admin
        }
    }

    "autenticar devuelve null si la contraseña es incorrecta" {
        val dao = mockk<UsuarioDao>()
        coEvery { dao.buscarPorCorreo("admin@kaiju.cl") } returns admin
        runTest {
            UsuarioRepository(dao).autenticar("admin@kaiju.cl", "otra123").shouldBeNull()
        }
    }

    "autenticar devuelve null si el correo no existe" {
        val dao = mockk<UsuarioDao>()
        coEvery { dao.buscarPorCorreo(any()) } returns null
        runTest {
            UsuarioRepository(dao).autenticar("nadie@kaiju.cl", "Admin123").shouldBeNull()
        }
    }

    "autenticar devuelve null si el usuario está inactivo" {
        val dao = mockk<UsuarioDao>()
        coEvery { dao.buscarPorCorreo("admin@kaiju.cl") } returns admin.copy(activo = false)
        runTest {
            UsuarioRepository(dao).autenticar("admin@kaiju.cl", "Admin123").shouldBeNull()
        }
    }

    "registrarCliente guarda un cliente con correo normalizado y contraseña en hash (RF-02)" {
        val dao = mockk<UsuarioDao>()
        val guardado = slot<Usuario>()
        coEvery { dao.buscarPorCorreo("nuevo@kaiju.cl") } returns null
        coEvery { dao.insertar(capture(guardado)) } returns 9L

        runTest {
            val r = UsuarioRepository(dao).registrarCliente("  Ana  ", " Nuevo@Kaiju.cl ", "Clave123")
            val ok = r.shouldBeInstanceOf<ResultadoRegistro.Ok>()
            ok.usuario.id shouldBe 9L
            ok.usuario.rol shouldBe Rol.CLIENTE
        }
        guardado.captured.nombre shouldBe "Ana"
        guardado.captured.correo shouldBe "nuevo@kaiju.cl"
        PasswordHasher.verificar("Clave123", guardado.captured.passwordHash) shouldBe true
    }

    "registrarCliente rechaza un correo ya registrado" {
        val dao = mockk<UsuarioDao>()
        coEvery { dao.buscarPorCorreo("admin@kaiju.cl") } returns admin
        runTest {
            UsuarioRepository(dao).registrarCliente("Ana", "admin@kaiju.cl", "Clave123") shouldBe
                    ResultadoRegistro.Error("Ya existe una cuenta con ese correo")
        }
        coVerify(exactly = 0) { dao.insertar(any()) }
    }

    "registrarCliente informa el error si la base rechaza el correo duplicado" {
        val dao = mockk<UsuarioDao>()
        coEvery { dao.buscarPorCorreo(any()) } returns null
        coEvery { dao.insertar(any()) } throws mockk<SQLiteConstraintException>(relaxed = true)
        runTest {
            UsuarioRepository(dao).registrarCliente("Ana", "ana@kaiju.cl", "Clave123") shouldBe
                    ResultadoRegistro.Error("Ya existe una cuenta con ese correo")
        }
    }

    "crearUsuariosIniciales crea un administrador y un cliente si no hay usuarios (RF-25)" {
        val dao = mockk<UsuarioDao>()
        val creados = mutableListOf<Usuario>()
        coEvery { dao.contar() } returns 0
        coEvery { dao.insertar(capture(creados)) } returns 1L

        runTest { UsuarioRepository(dao).crearUsuariosIniciales() }

        creados.map { it.rol } shouldBe listOf(Rol.ADMIN, Rol.CLIENTE)
        creados.map { it.correo } shouldBe listOf("admin@kaiju.cl", "cliente@kaiju.cl")
        PasswordHasher.verificar("Admin123", creados[0].passwordHash) shouldBe true
    }

    "crearUsuariosIniciales no duplica usuarios al reabrir la app" {
        val dao = mockk<UsuarioDao>()
        coEvery { dao.contar() } returns 2
        runTest { UsuarioRepository(dao).crearUsuariosIniciales() }
        coVerify(exactly = 0) { dao.insertar(any()) }
    }
})