package com.example.appkaiju.stock.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.appkaiju.stock.data.AlmacenFotos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun SeccionFoto(ruta: String?, onFotoChange: (String?) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var archivoPendiente by remember { mutableStateOf<File?>(null) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val archivo = archivoPendiente
        if (exito && archivo != null) {
            onFotoChange(archivo.absolutePath)
            mensaje = null
        } else {
            archivo?.delete()
        }
        archivoPendiente = null
    }

    fun abrirCamara() {
        val archivo = AlmacenFotos.crearArchivoNuevo(context)
        archivoPendiente = archivo
        camara.launch(AlmacenFotos.uriParaCamara(context, archivo))
    }

    val permiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) {
            abrirCamara()
        } else {
            mensaje = "Sin permiso de cámara. Puedes elegir una foto de la galería."
        }
    }

    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = withContext(Dispatchers.IO) { AlmacenFotos.copiarDeGaleria(context, uri) }
                if (path != null) {
                    onFotoChange(path)
                    mensaje = null
                } else {
                    mensaje = "No se pudo cargar la imagen"
                }
            }
        }
    }

    fun tomarFoto() {
        val concedido = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        if (concedido) abrirCamara() else permiso.launch(Manifest.permission.CAMERA)
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text("Foto del producto (opcional)", style = MaterialTheme.typography.titleSmall)
        ImagenProducto(ruta = ruta, modifier = Modifier.fillMaxWidth().height(160.dp).padding(vertical = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { tomarFoto() }) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Text("  Cámara")
            }
            OutlinedButton(onClick = { galeria.launch("image/*") }) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Text("  Galería")
            }
            if (ruta != null) {
                TextButton(onClick = { onFotoChange(null) }) { Text("Quitar") }
            }
        }
        Text(
            "La cámara se usa solo para fotografiar productos. No fotografíes personas.",
            style = MaterialTheme.typography.bodySmall
        )
        AnimatedVisibility(visible = mensaje != null) {
            Text(
                text = mensaje.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}