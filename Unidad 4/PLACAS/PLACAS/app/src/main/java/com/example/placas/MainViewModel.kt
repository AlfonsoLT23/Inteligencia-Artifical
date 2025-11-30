package com.example.placas

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.placas.model.*
import com.example.placas.repository.Repository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class MainViewModel(private val repository: Repository): ViewModel() {

    val usuarios: MutableLiveData<Response<List<UsuarioResponse>>> = MutableLiveData()
    val usuario: MutableLiveData<Response<UsuarioResponse>> = MutableLiveData()

    fun getUsuarios() {
        viewModelScope.launch {
            val response = repository.getUsuarios()
            usuarios.value = response
        }
    }

    fun getUsuario(id: Int) {
        viewModelScope.launch {
            val response = repository.getUsuario(id)
            usuario.value = response
        }
    }

    val vehiculos: MutableLiveData<Response<List<VehiculoResponse>>> = MutableLiveData()
    val vehiculo: MutableLiveData<Response<VehiculoResponse>> = MutableLiveData()

    fun getVehiculos() {
        viewModelScope.launch {
            val response = repository.getVehiculos()
            vehiculos.value = response
        }
    }

    fun getVehiculo(placa: String) {
        viewModelScope.launch {
            val response = repository.getVehiculo(placa)
            vehiculo.value = response
        }
    }

    val operadores: MutableLiveData<Response<List<OperadorResponse>>> = MutableLiveData()
    val operador: MutableLiveData<Response<OperadorResponse>> = MutableLiveData()

    fun getOperadores() {
        viewModelScope.launch {
            val response = repository.getOperadores()
            operadores.value = response
        }
    }

    fun getOperador(id: Int) {
        viewModelScope.launch {
            val response = repository.getOperador(id)
            operador.value = response
        }
    }

    val incidencias: MutableLiveData<Response<List<IncidenciaResponse>>> = MutableLiveData()
    val incidencia: MutableLiveData<Response<IncidenciaResponse>> = MutableLiveData()
    val incidenciaP: MutableLiveData<Response<Any>> = MutableLiveData()

    fun getIncidencias() {
        viewModelScope.launch {
            val response = repository.getIncidencias()
            incidencias.value = response
        }
    }

    fun getIncidencia(id: Int) {
        viewModelScope.launch {
            val response = repository.getIncidencia(id)
            incidencia.value = response
        }
    }

    fun postIncidencia(
        vehiculo: RequestBody,
        operador: RequestBody,
        descripcion_incidencia: RequestBody,
        latitud: RequestBody,
        longitud: RequestBody,
        evidencia_fotografia: MultipartBody.Part
    ) {
        viewModelScope.launch {
            val response = repository.postIncidencia(
                vehiculo,
                operador,
                descripcion_incidencia,
                latitud,
                longitud,
                evidencia_fotografia
            )
            incidenciaP.value = response
        }
    }

}