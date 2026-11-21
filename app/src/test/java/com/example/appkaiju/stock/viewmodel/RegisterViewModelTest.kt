@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.appkaiju.stock.viewmodel

import com.example.appkaiju.stock.model.Rol
import com.example.appkaiju.stock.model.Usuario
import com.example.appkaiju.stock.repository.ResultadoRegistro
import com.example.appkaiju.stock.repository.UsuarioRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

class RegisterViewModelTest : StringSpec({

    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    fun RegisterViewModel.llenar(nombre: String, correo: String, clave: String, confirmacion: String) {
        onNombreChange(nombre)
        onCorreoChange(correo)
        onClaveChange(clave)
        onConfirmacionChange(confirmacion)
    }

    "con el formulario vacío muestra un error en cada campo" {
        val repo = mockk<UsuarioRepository>()
        val vm = RegisterViewModel(repo)
        vm.registrar()

        val s = vm.estado.value
        s.nombreError shouldBe "Ingresa tu nombre"
        s.correoError shouldBe "Ingresa tu correo"
        s.claveError shouldBe "Crea una contraseña"
        s.confirmacionError shouldBe "Repite la contraseña"
        coVerify(exactly = 0) { repo.registrarCliente(any(), any(), any()) }
    }

    "valida largo del nombre, formato del correo y coincidencia de contraseñas" {
        val vm = RegisterViewModel(mockk())
        vm.llenar("Al", "ana@", "Clave123", "Clave999")
        vm.registrar()

        val s = vm.estado.value
        s.nombreError shouldBe "Debe tener al menos 3 caracteres"
        s.correoError shouldBe "El correo no es válido"
        s.claveError.shouldBeNull()
        s.confirmacionError shouldBe "Las contraseñas no coinciden"
    }

    "la contraseña debe tener 6 caracteres y combinar letras y números" {
        val vm = RegisterViewModel(mockk())
        vm.llenar("Ana Pérez", "ana@kaiju.cl", "abc1", "abc1")
        vm.registrar()
        vm.estado.value.claveError shouldBe "Debe tener al menos 6 caracteres"

        vm.llenar("Ana Pérez", "ana@kaiju.cl", "abcdefg", "abcdefg")
        vm.registrar()
        vm.estado.value.claveError shouldBe "Usa letras y números"
    }

    "el nombre se limita a 40 caracteres y al escribir se limpian los errores" {
        val vm = RegisterViewModel(mockk())
        vm.registrar()
        vm.llenar("A".repeat(60), "x", "y", "z")

        val s = vm.estado.value
        s.nombre.length shouldBe 40
        s.nombreError.shouldBeNull()
        s.correoError.shouldBeNull()
        s.claveError.shouldBeNull()
        s.confirmacionError.shouldBeNull()
    }

    "un registro válido crea el cliente (RF-02)" {
        val repo = mockk<UsuarioRepository>()
        val nuevo = Usuario(id = 3, nombre = "Ana Pérez", correo = "ana@kaiju.cl", passwordHash = "h", rol = Rol.CLIENTE)
        coEvery { repo.registrarCliente("Ana Pérez", "ana@kaiju.cl", "Clave123") } returns ResultadoRegistro.Ok(nuevo)
        val vm = RegisterViewModel(repo)

        vm.llenar("Ana Pérez", "ana@kaiju.cl", "Clave123", "Clave123")
        vm.registrar()

        vm.estado.value.usuarioCreado shouldBe nuevo
        vm.estado.value.cargando shouldBe false
    }

    "si el correo ya existe muestra el error en el campo correo" {
        val repo = mockk<UsuarioRepository>()
        coEvery { repo.registrarCliente(any(), any(), any()) } returns
                ResultadoRegistro.Error("Ya existe una cuenta con ese correo")
        val vm = RegisterViewModel(repo)

        vm.llenar("Ana Pérez", "admin@kaiju.cl", "Clave123", "Clave123")
        vm.registrar()

        vm.estado.value.correoError shouldBe "Ya existe una cuenta con ese correo"
        vm.estado.value.usuarioCreado.shouldBeNull()
    }
})