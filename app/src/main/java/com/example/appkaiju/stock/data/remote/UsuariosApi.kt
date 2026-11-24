package com.example.appkaiju.stock.data.remote

import com.example.appkaiju.stock.data.model.LoginRemotoRequest
import com.example.appkaiju.stock.data.model.LoginRemotoResponse
import com.example.appkaiju.stock.data.model.UsuarioActualizarRequest
import com.example.appkaiju.stock.data.model.UsuarioCrearRequest
import com.example.appkaiju.stock.data.model.UsuarioRemoto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UsuariosApi {

    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginRemotoRequest): LoginRemotoResponse

    @GET("api/v1/usuarios")
    suspend fun listar(@Header("Authorization") token: String): List<UsuarioRemoto>

    @POST("api/v1/usuarios")
    suspend fun crear(
        @Header("Authorization") token: String,
        @Body body: UsuarioCrearRequest
    ): UsuarioRemoto

    @PUT("api/v1/usuarios/{id}")
    suspend fun actualizar(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: UsuarioActualizarRequest
    ): UsuarioRemoto

// Desactivar la cuenta (responde 204)
    @DELETE("api/v1/usuarios/{id}")
    suspend fun desactivar(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    )
}