package com.example.appkaiju.stock.repository

import com.example.appkaiju.stock.data.model.Indicador
import com.example.appkaiju.stock.data.model.IndicadoresResponse
import com.example.appkaiju.stock.data.remote.IndicadoresApi
import com.google.gson.JsonSyntaxException
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class IndicadoresRepositoryTest : StringSpec({

    val dolar = Indicador("dolar", "Dólar observado", "Pesos", "2026-11-18T03:00:00.000Z", 950.5)
    val uf = Indicador("uf", "Unidad de fomento (UF)", "Pesos", "2026-11-18T03:00:00.000Z", 39800.0)

    "getIndicadores entrega los indicadores simulados de la API" {
        // 1. Simulamos la API con MockK
        val api = mockk<IndicadoresApi>()
        coEvery { api.obtenerIndicadores() } returns IndicadoresResponse(dolar = dolar, uf = uf)
        // 2. Creamos el repositorio con la API simulada
        val repo = IndicadoresRepository(api)
        // 3. Ejecutamos y comprobamos
        runTest {
            repo.obtenerIndicadores() shouldBe ResultadoIndicadores.Ok(listOf(dolar, uf))
        }
    }

    "se descartan indicadores sin valor" {
        val api = mockk<IndicadoresApi>()
        coEvery { api.obtenerIndicadores() } returns IndicadoresResponse(
            dolar = dolar, euro = Indicador(codigo = "euro", valor = null), utm = Indicador(codigo = "utm", valor = 0.0)
        )
        runTest {
            IndicadoresRepository(api).obtenerIndicadores() shouldBe ResultadoIndicadores.Ok(listOf(dolar))
        }
    }

    "una respuesta sin indicadores válidos es un error" {
        val api = mockk<IndicadoresApi>()
        coEvery { api.obtenerIndicadores() } returns IndicadoresResponse()
        runTest {
            IndicadoresRepository(api).obtenerIndicadores() shouldBe
                    ResultadoIndicadores.Error(IndicadoresRepository.MENSAJE_SIN_DATOS)
        }
    }

    "sin internet se informa un mensaje en vez de fallar" {
        val api = mockk<IndicadoresApi>()
        coEvery { api.obtenerIndicadores() } throws IOException("sin red")
        runTest {
            IndicadoresRepository(api).obtenerIndicadores() shouldBe
                    ResultadoIndicadores.Error(IndicadoresRepository.MENSAJE_SIN_CONEXION)
        }
    }

    "un error HTTP informa el código de respuesta" {
        val api = mockk<IndicadoresApi>()
        coEvery { api.obtenerIndicadores() } throws
                HttpException(Response.error<IndicadoresResponse>(503, "".toResponseBody(null)))
        runTest {
            val error = IndicadoresRepository(api).obtenerIndicadores()
                .shouldBeInstanceOf<ResultadoIndicadores.Error>()
            error.mensaje shouldBe "El servicio de indicadores respondió con error 503"
        }
    }

    "un JSON con formato inesperado se informa como error" {
        val api = mockk<IndicadoresApi>()
        coEvery { api.obtenerIndicadores() } throws JsonSyntaxException("formato")
        runTest {
            IndicadoresRepository(api).obtenerIndicadores() shouldBe
                    ResultadoIndicadores.Error(IndicadoresRepository.MENSAJE_SIN_DATOS)
        }
    }
})