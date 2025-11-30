package com.example.placas.api

import com.example.placas.model.IncidenciaRequest
import com.example.placas.model.IncidenciaResponse
import com.example.placas.model.UsuarioResponse
import com.example.placas.model.VehiculoResponse
import com.example.placas.model.OperadorResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface SimpleApi {

    @GET("usuarios")
    suspend fun getUsuarios(): Response<List<UsuarioResponse>>

    @GET("usuarios/{id}")
    suspend fun getUsuario(@Path("id") id: Int): Response<UsuarioResponse>

    @GET("vehiculos")
    suspend fun getVehiculos(): Response<List<VehiculoResponse>>

    @GET("vehiculos/{placa}")
    suspend fun getVehiculo(@Path("placa") placa: String): Response<VehiculoResponse>

    @GET("operadores")
    suspend fun getOperadores(): Response<List<OperadorResponse>>

    @GET("operadores/{id}")
    suspend fun getOperador(@Path("id") id: Int): Response<OperadorResponse>

    @GET("incidencias")
    suspend fun getIncidencias(): Response<List<IncidenciaResponse>>

    @GET("incidencias/{id}")
    suspend fun getIncidencia(@Path("id") id: Int): Response<IncidenciaResponse>

    @Multipart
    @POST("incidencias/")
    suspend fun postIncidencia(
        @Part("vehiculo") vehiculo: RequestBody,
        @Part("operador") operador: RequestBody,
        @Part("descripcion_incidencia") descripcion: RequestBody,
        @Part("latitud") latitud: RequestBody,
        @Part("longitud") longitud: RequestBody,
        @Part evidencia_fotografia: MultipartBody.Part
    ): Response<Any>
}