@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.appkaiju.stock.viewmodel

import com.example.appkaiju.stock.Datos
import com.example.appkaiju.stock.repository.UsuarioRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class AuthViewModelTest : StringSpec({

    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    fun repositorio(): UsuarioRepository = mockk<UsuarioRepository>().also {
        coEvery { it.crearUsuariosIniciales() } just Runs
    }

    "al crearse carga los usuarios ficticios" {
        val repo = repositorio()
        AuthViewModel(repo)
        coVerify(exactly = 1) { repo.crearUsuariosIniciales() }
    }

    "un inicio de sesión correcto guarda el usuario (RF-01)" {
        val repo = repositorio()
        coEvery { repo.autenticar("admin@kaiju.cl", "Admin123") } returns Datos.admin
        val vm = AuthViewModel(repo)

        vm.onCorreoChange(" admin@kaiju.cl ")
        vm.onClaveChange("Admin123")
        vm.iniciarSesion()

        vm.estado.value.usuario shouldBe Datos.admin
        vm.estado.value.cargando shouldBe false
        vm.estado.value.errorGeneral.shouldBeNull()
    }

    "con campos vacíos muestra un error por campo y no consulta el repositorio" {
        val repo = repositorio()
        val vm = AuthViewModel(repo)

        vm.iniciarSesion()

        vm.estado.value.correoError shouldBe "Ingresa tu correo"
        vm.estado.value.claveError shouldBe "Ingresa tu contraseña"
        coVerify(exactly = 0) { repo.autenticar(any(), any()) }
    }

    "valida el formato del correo y el largo de la contraseña" {
        val vm = AuthViewModel(repositorio())
        vm.onCorreoChange("correo-malo")
        vm.onClaveChange("123")
        vm.iniciarSesion()

        vm.estado.value.correoError shouldBe "El correo no es válido"
        vm.estado.value.claveError shouldBe "Debe tener al menos 6 caracteres"
    }

    "al escribir se limpia el error del campo" {
        val vm = AuthViewModel(repositorio())
        vm.iniciarSesion()
        vm.onCorreoChange("a")
        vm.onClaveChange("b")
        vm.estado.value.correoError.shouldBeNull()
        vm.estado.value.claveError.shouldBeNull()
    }

    "con credenciales incorrectas muestra un mensaje genérico" {
        val repo = repositorio()
        coEvery { repo.autenticar(any(), any()) } returns null
        val vm = AuthViewModel(repo)
        vm.onCorreoChange("admin@kaiju.cl")
        vm.onClaveChange("otraClave")
        vm.iniciarSesion()

        vm.estado.value.errorGeneral shouldBe "Correo o contraseña incorrectos"
        vm.estado.value.usuario.shouldBeNull()
    }

    "tras 5 intentos fallidos bloquea 60 segundos con cuenta regresiva (RN-11)" {
        runTest {
            val repo = repositorio()
            coEvery { repo.autenticar(any(), any()) } returns null
            // El reloj del ViewModel usa el tiempo virtual de la prueba
            val vm = AuthViewModel(repo) { testScheduler.currentTime }
            vm.onCorreoChange("admin@kaiju.cl")
            vm.onClaveChange("otraClave")

            repeat(5) { vm.iniciarSesion() }
            vm.estado.value.errorGeneral shouldBe "Demasiados intentos. Espera 60 s"

            // Mientras está bloqueado no vuelve a consultar
            vm.iniciarSesion()
            coVerify(exactly = 5) { repo.autenticar(any(), any()) }

            advanceTimeBy(30_000)
            runCurrent()
            vm.estado.value.errorGeneral shouldBe "Demasiados intentos. Espera 30 s"

            advanceTimeBy(31_000)
            vm.estado.value.errorGeneral.shouldBeNull()
        }
    }

    "cerrar sesión limpia el estado y establecerSesion guarda el usuario (RF-03)" {
        val vm = AuthViewModel(repositorio())
        vm.establecerSesion(Datos.cliente)
        vm.estado.value.usuario shouldBe Datos.cliente

        vm.cerrarSesion()
        vm.estado.value shouldBe LoginUiState()
    }

    "la fábrica crea el AuthViewModel" {
        AuthViewModelFactory(repositorio()).create(AuthViewModel::class.java)
            .shouldBeInstanceOf<AuthViewModel>()
    }
})