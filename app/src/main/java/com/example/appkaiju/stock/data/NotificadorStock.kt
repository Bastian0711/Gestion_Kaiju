package com.example.appkaiju.stock.data

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.appkaiju.stock.model.EstadoStock
import com.example.appkaiju.stock.model.Producto

class NotificadorStock(private val context: Context) {

    companion object {
        private const val CANAL_ID = "stock_alertas_v2"
        private const val ID_RESUMEN = 100000
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID,
                "Alertas de stock",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Avisos de productos con stock bajo o agotado" }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }
    }

    fun puedeNotificar(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

    fun avisarProducto(producto: Producto) {
        val titulo = if (producto.estadoStock() == EstadoStock.AGOTADO) {
            "Producto agotado"
        } else {
            "Stock bajo"
        }
        val texto = if (producto.estadoStock() == EstadoStock.AGOTADO) {
            "${producto.nombre} se agotó. Hay que reponer."
        } else {
            "${producto.nombre}: quedan ${producto.stockActual} (mínimo ${producto.stockMinimo})."
        }
        mostrar(producto.id.toInt(), titulo, texto)
    }

    fun avisarResumen(cantidad: Int) {
        mostrar(
            ID_RESUMEN,
            "Reposición pendiente",
            "$cantidad producto(s) con stock bajo o agotado."
        )
    }

    @SuppressLint("MissingPermission")
    private fun mostrar(id: Int, titulo: String, texto: String) {
        if (!puedeNotificar()) return

        val intentApp = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendiente = PendingIntent.getActivity(
            context, 0, intentApp, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setContentIntent(pendiente)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(id, notificacion)
    }
}