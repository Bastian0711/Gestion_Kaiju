package com.example.appkaiju.stock.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.appkaiju.stock.data.NotificadorStock
import com.example.appkaiju.stock.model.EstadoStock
import com.example.appkaiju.stock.model.Producto
import com.example.appkaiju.stock.model.ReglasAlertaStock
import com.example.appkaiju.stock.repository.ProductoRepository
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun MonitorStockBajo(
    productos: ProductoRepository,
    alertas: SharedFlow<Producto>,
    activo: Boolean
) {
    val context = LocalContext.current
    val notificador = remember { NotificadorStock(context.applicationContext) }
    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    
    LaunchedEffect(Unit) {
        alertas.collect { notificador.avisarProducto(it) }
    }

    LaunchedEffect(activo) {
        if (!activo) return@LaunchedEffect

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificador.puedeNotificar()) {
            pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        var previo: Map<Long, EstadoStock>? = null
        productos.buscar("").collect { lista ->
            val anterior = previo
            if (notificador.puedeNotificar()) {
                if (anterior == null) {
                    val pendientes = lista.count { it.estadoStock() != EstadoStock.NORMAL }
                    if (pendientes > 0) notificador.avisarResumen(pendientes)
                } else {
                    ReglasAlertaStock.productosParaAlertar(anterior, lista)
                        .forEach { notificador.avisarProducto(it) }
                }
            }
            previo = lista.associate { it.id to it.estadoStock() }
        }
    }
}