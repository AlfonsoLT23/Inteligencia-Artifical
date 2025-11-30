package com.example.placas.model

data class VehiculoResponse(
    val placa: String,
    val usuario: Int,
    val marca: String?,
    val modelo: String?,
    val color: String?,
    val anio: Int?,
    val estado: Boolean
)