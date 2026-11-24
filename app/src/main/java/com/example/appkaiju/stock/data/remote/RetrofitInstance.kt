package com.example.appkaiju.stock.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private const val URL_INDICADORES = "https://mindicador.cl/"
    const val URL_USUARIOS = "http://192.168.1.100:8081/"

    private val cliente: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(
                // Muestra en Logcat cada petición y su código de respuesta
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
            )
            .build()
    }

    val indicadoresApi: IndicadoresApi by lazy {
        Retrofit.Builder()
            .baseUrl(URL_INDICADORES)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IndicadoresApi::class.java)
    }

    val usuariosApi: UsuariosApi by lazy {
        Retrofit.Builder()
            .baseUrl(URL_USUARIOS)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsuariosApi::class.java)
    }
}