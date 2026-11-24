package com.example.appkaiju.stock.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.appkaiju.stock.data.model.Indicador
import com.example.appkaiju.stock.ui.components.TagsIndicadores
import com.example.appkaiju.stock.ui.components.TarjetaIndicadores
import com.example.appkaiju.stock.viewmodel.IndicadoresUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Pruebas de la API externa
@RunWith(AndroidJUnit4::class)
class TarjetaIndicadoresTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun mientrasCarga_muestraElIndicadorDeCarga() {
        composeRule.setContent {
            TarjetaIndicadores(estado = IndicadoresUiState(cargando = true), onReintentar = {})
        }
        composeRule.onNodeWithTag(TagsIndicadores.CARGANDO).assertIsDisplayed()
        composeRule.onNodeWithText("Cargando indicadores...").assertIsDisplayed()
    }

    @Test
    fun conDatos_muestraLosIndicadoresYLaFuente() {
        val estado = IndicadoresUiState(
            indicadores = listOf(
                Indicador(codigo = "dolar", nombre = "Dólar observado", fecha = "2026-11-18T03:00:00.000Z", valor = 950.5),
                Indicador(codigo = "uf", nombre = "Unidad de fomento (UF)", fecha = "2026-11-18T03:00:00.000Z", valor = 39800.0)
            )
        )
        composeRule.setContent { TarjetaIndicadores(estado = estado, onReintentar = {}) }

        composeRule.onNodeWithText("Dólar observado").assertIsDisplayed()
        composeRule.onNodeWithText("UF").assertIsDisplayed()
        composeRule.onNodeWithText("Fuente: mindicador.cl · 18-11-2026").assertIsDisplayed()
    }

    @Test
    fun conError_muestraElMensajeYReintentarLlamaAlCallback() {
        var reintentos = 0
        composeRule.setContent {
            TarjetaIndicadores(
                estado = IndicadoresUiState(error = "Sin conexión: no se pudieron cargar los indicadores"),
                onReintentar = { reintentos++ }
            )
        }

        composeRule.onNodeWithTag(TagsIndicadores.ERROR).assertIsDisplayed()
        composeRule.onNodeWithTag(TagsIndicadores.REINTENTAR).performClick()
        assertEquals(1, reintentos)
    }
}