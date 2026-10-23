package com.example.appkaiju.stock.repository

import com.example.appkaiju.stock.data.PasswordHasher
import com.example.appkaiju.stock.data.UsuarioDao
import com.example.appkaiju.stock.model.Rol
import com.example.appkaiju.stock.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsuarioRepository(private val dao: UsuarioDao) {


    suspend fun autenticar(correo: String, clave: String): Usuario? {
        val usuario = dao.buscarPorCorreo(correo.lowercase()) ?: return null
        if (!usuario.activo) return null
        val valida = withContext(Dispatchers.Default) {
            PasswordHasher.verificar(clave, usuario.passwordHash)
        }
        return if (valida) usuario else null
    }


    suspend fun crearUsuariosIniciales() {
        if (dao.contar() > 0) return
        val hashAdmin = withContext(Dispatchers.Default) { PasswordHasher.hashear("Admin123") }
        val hashCliente = withContext(Dispatchers.Default) { PasswordHasher.hashear("Cliente123") }
        dao.insertar(
            Usuario(
                nombre = "Administrador Kaiju",
                correo = "admin@kaiju.cl",
                passwordHash = hashAdmin,
                rol = Rol.ADMIN
            )
        )
        dao.insertar(
            Usuario(
                nombre = "Cliente de Prueba",
                correo = "cliente@kaiju.cl",
                passwordHash = hashCliente,
                rol = Rol.CLIENTE
            )
        )
    }
}

