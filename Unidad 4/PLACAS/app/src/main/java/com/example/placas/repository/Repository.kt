package com.example.placas.repository

import com.example.placas.api.RetrofitInstance
import com.example.placas.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class Repository {

    suspend fun getUsuarios(): Response<List<UsuarioResponse>> {
        return RetrofitInstance.api.getUsuarios()
    }

    suspend fun getUsuario(id: Int): Response<UsuarioResponse> {
        return RetrofitInstance.api.getUsuario(id)
    }


    suspend fun getVehiculos(): Response<List<VehiculoResponse>> {
        return RetrofitInstance.api.getVehiculos()
    }

    suspend fun getVehiculo(placa: String): Response<VehiculoResponse> {
        return RetrofitInstance.api.getVehiculo(placa)
    }


    suspend fun getOperadores(): Response<List<OperadorResponse>> {
        return RetrofitInstance.api.getOperadores()
    }

    suspend fun getOperador(id: Int): Response<OperadorResponse> {
        return RetrofitInstance.api.getOperador(id)
    }


    suspend fun getIncidencias(): Response<List<IncidenciaResponse>> {
        return RetrofitInstance.api.getIncidencias()
    }

    suspend fun getIncidencia(id: Int): Response<IncidenciaResponse> {
        return RetrofitInstance.api.getIncidencia(id)
    }

    suspend fun postIncidencia(
        vehiculo: RequestBody,
        operador: RequestBody,
        descripcion_incidencia: RequestBody,
        latitud: RequestBody,
        longitud: RequestBody,
        evidencia_fotografia: MultipartBody.Part
    ): Response<Any> {
        return RetrofitInstance.api.postIncidencia(
            vehiculo,
            operador,
            descripcion_incidencia,
            latitud,
            longitud,
            evidencia_fotografia
        )
    }

}