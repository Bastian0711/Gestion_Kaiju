package com.example.appkaiju.stock.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ImagenProducto(ruta: String?, modifier: Modifier = Modifier) {
    val imagen by produceState<ImageBitmap?>(initialValue = null, key1 = ruta) {
        value = if (ruta == null) null else withContext(Dispatchers.IO) {
            try {
                decodificar(ruta)
            } catch (e: Exception) {
                null
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val img = imagen
        if (img != null) {
            Image(
                bitmap = img,
                contentDescription = "Foto del producto",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = "Sin foto",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun decodificar(ruta: String, maxLado: Int = 900): ImageBitmap? {
    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(ruta, limites)
    var muestra = 1
    while (limites.outWidth / muestra > maxLado || limites.outHeight / muestra > maxLado) muestra *= 2

    val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
    val bitmap = BitmapFactory.decodeFile(ruta, opciones) ?: return null

    val grados = when (
        ExifInterface(ruta).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    ) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
    val final = if (grados == 0f) bitmap else Bitmap.createBitmap(
        bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(grados) }, true
    )
    return final.asImageBitmap()
}