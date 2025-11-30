package com.example.placas.model

data class IncidenciaRequest(
    val vehiculo: String,
    val operador: Int,
    val descripcion_incidencia: String,
    val evidencia_fotografia: String,
    val latitud: Double?,
    val longitud: Double?
)
