@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.appkaiju.stock.viewmodel

import com.example.appkaiju.stock.model.Categoria
import com.example.appkaiju.stock.model.Producto
import com.example.appkaiju.stock.model.TipoProducto
import com.example.appkaiju.stock.repository.InventarioRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainKeys
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

class ProductFormViewModelTest : StringSpec({

    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }

    val categorias = listOf(Categoria(id = 1, nombre = "Vestuario"), Categoria(id = 2, nombre = "Insumos"))

    fun repositorio(): InventarioRepository = mockk<InventarioRepository>().also {
        every { it.observarCategorias() } returns flowOf(categorias)
        coEvery { it.existeCodigo(any()) } returns false
        coEvery { it.crearProducto(any(), any()) } returns 10L
    }

    fun ProductFormViewModel.llenarValido() {
        onCampoChange(CampoProducto.CODIGO, "ves-010")
        onCampoChange(CampoProducto.NOMBRE, "  Polera Azul ")
        onCampoChange(CampoProducto.DESCRIPCION, " Algodón ")
        onCategoriaChange(1)
        onCampoChange(CampoProducto.PRECIO, "14990")
        onCampoChange(CampoProducto.STOCK_INICIAL, "5")
        onCampoChange(CampoProducto.STOCK_MINIMO, "2")
    }

    "al crearse carga las categorías" {
        ProductFormViewModel(repositorio()).estado.value.categorias shouldBe categorias
    }

    "los campos numéricos solo aceptan dígitos y el código se limita a 20 caracteres" {
        val vm = ProductFormViewModel(repositorio())
        vm.onCampoChange(CampoProducto.PRECIO, "12a3.5")
        vm.onCampoChange(CampoProducto.CODIGO, "X".repeat(30))
        vm.estado.value.precio shouldBe "1235"
        vm.estado.value.codigo.length shouldBe 20
    }

    "con el formulario vacío marca los campos obligatorios y no guarda (RF-05)" {
        val repo = repositorio()
        val vm = ProductFormViewModel(repo)
        vm.guardar(usuarioId = 1)

        vm.estado.value.errores.shouldContainKeys(
            CampoProducto.CODIGO, CampoProducto.NOMBRE, CampoProducto.CATEGORIA, CampoProducto.PRECIO
        )
        vm.estado.value.errores[CampoProducto.PRECIO] shouldBe "El precio debe ser al menos \$1"
        coVerify(exactly = 0) { repo.crearProducto(any(), any()) }
    }

    "valida nombre corto, precio 0 y stocks vacíos" {
        val vm = ProductFormViewModel(repositorio())
        vm.llenarValido()
        vm.onCampoChange(CampoProducto.NOMBRE, "ab")
        vm.onCampoChange(CampoProducto.PRECIO, "0")
        vm.onCampoChange(CampoProducto.STOCK_INICIAL, "")
        vm.onCampoChange(CampoProducto.STOCK_MINIMO, "")
        vm.guardar(usuarioId = 1)

        val e = vm.estado.value.errores
        e[CampoProducto.NOMBRE] shouldBe "Debe tener al menos 3 caracteres"
        e[CampoProducto.PRECIO] shouldBe "El precio debe ser al menos \$1"
        e[CampoProducto.STOCK_INICIAL] shouldBe "Ingresa el stock inicial (puede ser 0)"
        e[CampoProducto.STOCK_MINIMO] shouldBe "Ingresa el stock mínimo (puede ser 0)"
    }

    "al corregir un campo se borra su error" {
        val vm = ProductFormViewModel(repositorio())
        vm.guardar(usuarioId = 1)
        vm.onCampoChange(CampoProducto.NOMBRE, "Polera")
        vm.onCategoriaChange(2)
        vm.estado.value.errores.shouldNotContainKey(CampoProducto.NOMBRE)
        vm.estado.value.errores.shouldNotContainKey(CampoProducto.CATEGORIA)
    }

    "un insumo perecible exige lote y una fecha de vencimiento real" {
        val vm = ProductFormViewModel(repositorio())
        vm.llenarValido()
        vm.onTipoChange(TipoProducto.INSUMO_PERECIBLE)
        vm.onCampoChange(CampoProducto.FECHA_VENC, "31-02-2027")
        vm.guardar(usuarioId = 1)

        vm.estado.value.errores[CampoProducto.LOTE] shouldBe "Ingresa el lote"
        vm.estado.value.errores[CampoProducto.FECHA_VENC] shouldBe "Usa el formato dd-MM-aaaa (ej: 31-12-2027)"
    }

    "no permite un código repetido (RN-05)" {
        val repo = repositorio()
        coEvery { repo.existeCodigo("VES-010") } returns true
        val vm = ProductFormViewModel(repo)
        vm.llenarValido()
        vm.guardar(usuarioId = 1)

        vm.estado.value.errores[CampoProducto.CODIGO] shouldBe "Ya existe un producto con ese código"
        vm.estado.value.guardando shouldBe false
        coVerify(exactly = 0) { repo.crearProducto(any(), any()) }
    }

    "guarda un producto de vestuario con código en mayúsculas, talla, color y foto" {
        val repo = repositorio()
        val producto = slot<Producto>()
        coEvery { repo.crearProducto(capture(producto), 1L) } returns 10L
        val vm = ProductFormViewModel(repo)
        vm.llenarValido()
        vm.onTipoChange(TipoProducto.VESTUARIO)
        vm.onCampoChange(CampoProducto.TALLA, "M")
        vm.onCampoChange(CampoProducto.COLOR, "Azul")
        vm.onCampoChange(CampoProducto.MODELO, "No aplica")
        vm.onFotoChange("/fotos/polera.jpg")
        vm.guardar(usuarioId = 1)

        vm.estado.value.errores.shouldBeEmpty()
        vm.estado.value.guardadoOk shouldBe true
        with(producto.captured) {
            codigo shouldBe "VES-010"
            nombre shouldBe "Polera Azul"
            descripcion shouldBe "Algodón"
            precio shouldBe 14990
            stockActual shouldBe 5
            stockMinimo shouldBe 2
            talla shouldBe "M"
            color shouldBe "Azul"
            modelo.shouldBeNull()
            imagenUri shouldBe "/fotos/polera.jpg"
        }
    }

    "guarda un insumo perecible con lote y vencimiento" {
        val repo = repositorio()
        val producto = slot<Producto>()
        coEvery { repo.crearProducto(capture(producto), any()) } returns 10L
        val vm = ProductFormViewModel(repo)
        vm.llenarValido()
        vm.onTipoChange(TipoProducto.INSUMO_PERECIBLE)
        vm.onCampoChange(CampoProducto.LOTE, " L-01 ")
        vm.onCampoChange(CampoProducto.FECHA_VENC, "31-12-2027")
        vm.guardar(usuarioId = 1)

        producto.captured.lote shouldBe "L-01"
        producto.captured.fechaVencimiento shouldBe "31-12-2027"
        producto.captured.talla.shouldBeNull()
    }

    "guarda un producto de tecnología con modelo" {
        val repo = repositorio()
        val producto = slot<Producto>()
        coEvery { repo.crearProducto(capture(producto), any()) } returns 10L
        val vm = ProductFormViewModel(repo)
        vm.llenarValido()
        vm.onTipoChange(TipoProducto.TECNOLOGIA)
        vm.onCampoChange(CampoProducto.MODELO, "BT-300")
        vm.guardar(usuarioId = 1)

        producto.captured.modelo shouldBe "BT-300"
        producto.captured.lote.shouldBeNull()
    }
})