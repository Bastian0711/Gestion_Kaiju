package com.example.appkaiju.stock.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.appkaiju.stock.data.model.UsuarioRemoto
import com.example.appkaiju.stock.data.remote.RetrofitInstance
import com.example.appkaiju.stock.ui.components.CampoFormulario
import com.example.appkaiju.stock.viewmodel.CampoUsuario
import com.example.appkaiju.stock.viewmodel.FormularioUsuario
import com.example.appkaiju.stock.viewmodel.UsuariosRemotosViewModel

//CRUD de usuarios del microservicio propio (ms-usuarios). Solo para el administrador
@Composable
fun UsuariosRemotosScreen(viewModel: UsuariosRemotosViewModel, onBack: () -> Unit) {
    val estado by viewModel.estado.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text("Usuarios (microservicio)", style = MaterialTheme.typography.headlineSmall)
        }
        Text(
            "Servidor: ${RetrofitInstance.URL_USUARIOS}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (!estado.conectado) {
            // 1) Primero el administrador inicia sesion en el microservicio
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Inicia sesión como administrador del microservicio", style = MaterialTheme.typography.titleMedium)
                    CampoFormulario(
                        valor = estado.correoAdmin,
                        etiqueta = "Correo",
                        error = null,
                        icono = Icons.Default.Email,
                        teclado = KeyboardType.Email,
                        onCambio = viewModel::onCorreoAdminChange
                    )
                    OutlinedTextField(
                        value = estado.claveAdmin,
                        onValueChange = viewModel::onClaveAdminChange,
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    estado.error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(
                        onClick = viewModel::conectar,
                        enabled = !estado.cargando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (estado.cargando) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Conectar")
                        }
                    }
                }
            }
        } else {
            // 2) Con sesion iniciada: listar, crear, editar y desactivar
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = viewModel::nuevo) { Text("Nuevo") }
                OutlinedButton(onClick = viewModel::cargar) { Text("Actualizar") }
                TextButton(onClick = viewModel::desconectar) { Text("Desconectar") }
            }
            if (estado.cargando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
            }
            estado.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 4.dp))
            }
            estado.mensaje?.let {
                Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 4.dp))
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(estado.usuarios, key = { it.id ?: it.correo.hashCode() }) { usuario ->
                    TarjetaUsuario(
                        usuario = usuario,
                        onEditar = { viewModel.editar(usuario) },
                        onDesactivar = { viewModel.desactivar(usuario) }
                    )
                }
            }
        }
    }

    estado.formulario?.let { formulario ->
        DialogoUsuario(
            formulario = formulario,
            error = estado.error,
            guardando = estado.cargando,
            onCambio = viewModel::onFormularioChange,
            onGuardar = viewModel::guardar,
            onCancelar = viewModel::cancelarFormulario
        )
    }
}

@Composable
private fun TarjetaUsuario(usuario: UsuarioRemoto, onEditar: () -> Unit, onDesactivar: () -> Unit) {
    val activo = usuario.activo ?: true
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("${usuario.nombre.orEmpty()} ${usuario.apellidos.orEmpty()}", style = MaterialTheme.typography.titleMedium)
            Text(usuario.correo.orEmpty(), style = MaterialTheme.typography.bodySmall)
            Text(
                "Rol: ${usuario.rol.orEmpty()} · ${if (activo) "Activo" else "Inactivo"}",
                style = MaterialTheme.typography.labelMedium,
                color = if (activo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
            )
            Row {
                TextButton(onClick = onEditar) { Text("Editar") }
                if (activo) {
                    TextButton(onClick = onDesactivar) { Text("Desactivar") }
                }
            }
        }
    }
}

@Composable
private fun DialogoUsuario(
    formulario: FormularioUsuario,
    error: String?,
    guardando: Boolean,
    onCambio: (FormularioUsuario) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(if (formulario.esNuevo) "Nuevo usuario" else "Editar usuario") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                CampoFormulario(
                    valor = formulario.nombre,
                    etiqueta = "Nombre",
                    error = formulario.errores[CampoUsuario.NOMBRE],
                    icono = Icons.Default.Person,
                    onCambio = { onCambio(formulario.copy(nombre = it)) }
                )
                CampoFormulario(
                    valor = formulario.apellidos,
                    etiqueta = "Apellidos",
                    error = formulario.errores[CampoUsuario.APELLIDOS],
                    icono = Icons.Default.Person,
                    onCambio = { onCambio(formulario.copy(apellidos = it)) }
                )
                if (formulario.esNuevo) {
                    CampoFormulario(
                        valor = formulario.correo,
                        etiqueta = "Correo",
                        error = formulario.errores[CampoUsuario.CORREO],
                        icono = Icons.Default.Email,
                        teclado = KeyboardType.Email,
                        onCambio = { onCambio(formulario.copy(correo = it)) }
                    )
                    OutlinedTextField(
                        value = formulario.clave,
                        onValueChange = { onCambio(formulario.copy(clave = it)) },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        isError = formulario.errores[CampoUsuario.CLAVE] != null,
                        supportingText = { formulario.errores[CampoUsuario.CLAVE]?.let { Text(it) } },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text("Correo: ${formulario.correo}", style = MaterialTheme.typography.bodyMedium)
                }
                CampoFormulario(
                    valor = formulario.direccion,
                    etiqueta = "Dirección (opcional)",
                    error = null,
                    icono = Icons.Default.Home,
                    onCambio = { onCambio(formulario.copy(direccion = it)) }
                )
                Text("Rol", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 6.dp))
                UsuariosRemotosViewModel.ROLES.chunked(2).forEach { fila ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        fila.forEach { rol ->
                            FilterChip(
                                selected = formulario.rol == rol,
                                onClick = { onCambio(formulario.copy(rol = rol)) },
                                label = { Text(rol) }
                            )
                        }
                    }
                }
                if (!formulario.esNuevo) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Cuenta activa", modifier = Modifier.weight(1f))
                        Switch(
                            checked = formulario.activo,
                            onCheckedChange = { onCambio(formulario.copy(activo = it)) }
                        )
                    }
                }
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onGuardar, enabled = !guardando) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}
