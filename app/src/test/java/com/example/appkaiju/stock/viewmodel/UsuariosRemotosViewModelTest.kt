@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.appkaiju.stock.viewmodel

import com.example.appkaiju.stock.data.model.UsuarioActualizarRequest
import com.example.appkaiju.stock.data.model.UsuarioCrearRequest
import com.example.appkaiju.stock.data.model.UsuarioRemoto
import com.example.appkaiju.stock.repository.ResultadoRemoto
import com.example.appkaiju.stock.repository.UsuariosRemotosRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldContainKeys
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

class UsuariosRemotosViewModelTest : StringSpec({

    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    val admin = UsuarioRemoto(id = 1, nombre = "Admin", apellidos = "Maestros", correo = "admin@maestros.cl", rol = "admin", activo = true)
    val vendedor = UsuarioRemoto(id = 2, nombre = "Vale", apellidos = "Soto", correo = "vendedor@maestros.cl", rol = "vendedor", activo = true)

    // repositorio simulado con sesion iniciada y una lista de 2 usuarios
    fun repoConectado(): UsuariosRemotosRepository = mockk<UsuariosRemotosRepository>().also {
        every { it.conectado } returns true
        coEvery { it.listar() } returns ResultadoRemoto.Ok(listOf(admin, vendedor))
    }

    "si ya hay sesión, al abrir la pantalla carga la lista (GET)" {
        val vm = UsuariosRemotosViewModel(repoConectado())
        vm.estado.value.conectado shouldBe true
        vm.estado.value.usuarios shouldBe listOf(admin, vendedor)
        vm.estado.value.cargando shouldBe false
    }

    "conectar con datos vacíos muestra un error sin llamar al servidor" {
        val repo = mockk<UsuariosRemotosRepository>()
        every { repo.conectado } returns false
        val vm = UsuariosRemotosViewModel(repo)

        vm.conectar()

        vm.estado.value.error shouldBe "Ingresa el correo y la contraseña del administrador"
        coVerify(exactly = 0) { repo.iniciarSesion(any(), any()) }
    }

    "conectar inicia sesión y luego carga los usuarios" {
        val repo = mockk<UsuariosRemotosRepository>()
        every { repo.conectado } returns false
        coEvery { repo.iniciarSesion("admin@maestros.cl", "Admin123!") } returns ResultadoRemoto.Ok(admin)
        coEvery { repo.listar() } returns ResultadoRemoto.Ok(listOf(admin))
        val vm = UsuariosRemotosViewModel(repo)

        vm.onCorreoAdminChange("admin@maestros.cl")
        vm.onClaveAdminChange("Admin123!")
        vm.conectar()

        vm.estado.value.conectado shouldBe true
        vm.estado.value.claveAdmin shouldBe ""
        vm.estado.value.usuarios shouldBe listOf(admin)
    }

    "si el login falla se muestra el mensaje del servidor" {
        val repo = mockk<UsuariosRemotosRepository>()
        every { repo.conectado } returns false
        coEvery { repo.iniciarSesion(any(), any()) } returns ResultadoRemoto.Error("Correo o contrasena incorrectos.")
        val vm = UsuariosRemotosViewModel(repo)
        vm.onCorreoAdminChange("admin@maestros.cl")
        vm.onClaveAdminChange("mala")

        vm.conectar()

        vm.estado.value.conectado shouldBe false
        vm.estado.value.error shouldBe "Correo o contrasena incorrectos."
    }

    "el formulario nuevo valida nombre, apellidos, correo y contraseña fuerte" {
        val repo = repoConectado()
        val vm = UsuariosRemotosViewModel(repo)
        vm.nuevo()
        vm.onFormularioChange(FormularioUsuario(nombre = "A", apellidos = "", correo = "malo", clave = "abc"))

        vm.guardar()

        vm.estado.value.formulario.shouldNotBeNull().errores.shouldContainKeys(
            CampoUsuario.NOMBRE, CampoUsuario.APELLIDOS, CampoUsuario.CORREO, CampoUsuario.CLAVE
        )
        coVerify(exactly = 0) { repo.crear(any()) }
    }

    "crear un usuario envía los datos limpios (POST) y recarga la lista" {
        val repo = repoConectado()
        val enviado = slot<UsuarioCrearRequest>()
        coEvery { repo.crear(capture(enviado)) } returns ResultadoRemoto.Ok(vendedor)
        val vm = UsuariosRemotosViewModel(repo)
        vm.nuevo()
        vm.onFormularioChange(
            FormularioUsuario(
                nombre = " Vale ", apellidos = " Soto ", correo = " Vendedor@Maestros.cl ",
                clave = "Clave123!", direccion = "  ", rol = "vendedor"
            )
        )

        vm.guardar()

        enviado.captured shouldBe UsuarioCrearRequest("Vale", "Soto", "vendedor@maestros.cl", "Clave123!", null, "vendedor")
        vm.estado.value.formulario.shouldBeNull()
        vm.estado.value.mensaje shouldBe "Usuario creado"
        coVerify(exactly = 2) { repo.listar() }
    }

    "editar carga los datos del usuario y guardar envía rol y estado (PUT)" {
        val repo = repoConectado()
        val enviado = slot<UsuarioActualizarRequest>()
        coEvery { repo.actualizar(2, capture(enviado)) } returns ResultadoRemoto.Ok(vendedor)
        val vm = UsuariosRemotosViewModel(repo)

        vm.editar(vendedor)
        val f = vm.estado.value.formulario.shouldNotBeNull()
        f.esNuevo shouldBe false
        f.correo shouldBe "vendedor@maestros.cl"
        vm.onFormularioChange(f.copy(rol = "cliente", activo = false))
        vm.guardar()

        enviado.captured shouldBe UsuarioActualizarRequest("Vale", "Soto", null, "cliente", false)
        vm.estado.value.mensaje shouldBe "Usuario actualizado"
    }

    "si el servidor rechaza el guardado se mantiene el formulario con el error" {
        val repo = repoConectado()
        coEvery { repo.crear(any()) } returns ResultadoRemoto.Error("Ya existe una cuenta con ese correo.")
        val vm = UsuariosRemotosViewModel(repo)
        vm.nuevo()
        vm.onFormularioChange(FormularioUsuario(nombre = "Ana", apellidos = "Pérez", correo = "admin@maestros.cl", clave = "Clave123!"))

        vm.guardar()

        vm.estado.value.formulario.shouldNotBeNull()
        vm.estado.value.error shouldBe "Ya existe una cuenta con ese correo."
    }

    "cancelar cierra el formulario" {
        val vm = UsuariosRemotosViewModel(repoConectado())
        vm.nuevo()
        vm.cancelarFormulario()
        vm.estado.value.formulario.shouldBeNull()
    }

    "desactivar un usuario (DELETE) muestra el mensaje y recarga" {
        val repo = repoConectado()
        coEvery { repo.desactivar(2) } returns ResultadoRemoto.Ok(Unit)
        val vm = UsuariosRemotosViewModel(repo)

        vm.desactivar(vendedor)

        vm.estado.value.mensaje shouldBe "Usuario desactivado"
        coVerify(exactly = 2) { repo.listar() }
    }

    "si la sesión vence se vuelve a la pantalla de conexión" {
        val repo = mockk<UsuariosRemotosRepository>()
        every { repo.conectado } returnsMany listOf(true, true, false)
        coEvery { repo.listar() } returns ResultadoRemoto.Error("Debes iniciar sesion o tu sesion expiro.")

        val vm = UsuariosRemotosViewModel(repo)

        vm.estado.value.conectado shouldBe false
        vm.estado.value.error shouldBe "Debes iniciar sesion o tu sesion expiro."
    }

    "desconectar cierra la sesión del microservicio" {
        val repo = repoConectado()
        every { repo.cerrarSesion() } just Runs
        val vm = UsuariosRemotosViewModel(repo)

        vm.desconectar()

        verify { repo.cerrarSesion() }
        vm.estado.value shouldBe UsuariosRemotosState()
    }
})
