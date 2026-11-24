package com.example.appkaiju.stock.data.model

import com.google.gson.annotations.SerializedName


// Usuario tal como lo devuelve el microservicio (no trae la contraseña)
data class UsuarioRemoto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("apellidos") val apellidos: String? = null,
    @SerializedName("correo") val correo: String? = null,
    @SerializedName("direccion") val direccion: String? = null,
    @SerializedName("rol") val rol: String? = null,
    @SerializedName("activo") val activo: Boolean? = null
)

// Cuerpo de POST /api/v1/auth/login
data class LoginRemotoRequest(
    @SerializedName("correo") val correo: String,
    @SerializedName("password") val password: String
)

// Respuesta del login: el token JWT y los datos del usuario
data class LoginRemotoResponse(
    @SerializedName("accessToken") val accessToken: String? = null,
    @SerializedName("tokenType") val tokenType: String? = null,
    @SerializedName("expiresIn") val expiresIn: Long? = null,
    @SerializedName("usuario") val usuario: UsuarioRemoto? = null
)

// Cuerpo de POST /api/v1/usuarios solo Admin
data class UsuarioCrearRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellidos") val apellidos: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("password") val password: String,
    @SerializedName("direccion") val direccion: String?,
    @SerializedName("rol") val rol: String
)

// Cuerpo de PUT /api/v1/usuarios/{id} .
data class UsuarioActualizarRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellidos") val apellidos: String,
    @SerializedName("direccion") val direccion: String?,
    @SerializedName("rol") val rol: String?,
    @SerializedName("activo") val activo: Boolean?
)

data class ErrorRemoto(
    @SerializedName("mensaje") val mensaje: String? = null,
    @SerializedName("detalles") val detalles: List<String>? = null
)