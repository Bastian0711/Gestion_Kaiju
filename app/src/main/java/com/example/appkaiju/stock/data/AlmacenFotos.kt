package com.example.appkaiju.stock.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object AlmacenFotos {
    private fun carpeta(context: Context): File =
        File(context.filesDir, "fotos").also { it.mkdirs() }

    fun crearArchivoNuevo(context: Context): File =
        File(carpeta(context), "prod_${System.currentTimeMillis()}.jpg")

    fun uriParaCamara(context: Context, archivo: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

    fun copiarDeGaleria(context: Context, origen: Uri): String? {
        return try {
            val entrada = context.contentResolver.openInputStream(origen) ?: return null
            val destino = crearArchivoNuevo(context)
            entrada.use { input -> destino.outputStream().use { output -> input.copyTo(output) } }
            destino.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}