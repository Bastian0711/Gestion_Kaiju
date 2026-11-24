package com.example.appkaiju.stock.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.appkaiju.stock.viewmodel.IndicadoresUiState

object TagsIndicadores {
    const val TARJETA = "tarjeta_indicadores"
    const val CARGANDO = "indicadores_cargando"
    const val ERROR = "indicadores_error"
    const val REINTENTAR = "indicadores_reintentar"
}

// Indicadores economicos de la API mindicador.cl no modifica nada
@Composable
fun TarjetaIndicadores(
    estado: IndicadoresUiState,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth().testTag(TagsIndicadores.TARJETA)) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Indicadores económicos", style = MaterialTheme.typography.titleMedium)
            }

            when {
                estado.cargando && estado.indicadores.isEmpty() -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag(TagsIndicadores.CARGANDO)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text("Cargando indicadores...", style = MaterialTheme.typography.bodyMedium)
                }

                estado.indicadores.isEmpty() && estado.error != null -> Text(
                    estado.error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag(TagsIndicadores.ERROR)
                )

                else -> estado.indicadores.forEach { indicador ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            etiquetaIndicador(indicador.codigo, indicador.nombre),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            (indicador.valor ?: 0.0).comoPesosConDecimales(),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }

            val fecha = estado.indicadores.firstOrNull()?.fecha
            if (fecha != null) {
                Text(
                    "Fuente: mindicador.cl · ${fecha.comoFechaCorta()}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (estado.error != null && !estado.cargando) {
                TextButton(
                    onClick = onReintentar,
                    modifier = Modifier.testTag(TagsIndicadores.REINTENTAR)
                ) { Text("Reintentar") }
            }
        }
    }
}