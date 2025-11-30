package com.example.placas.model

data class IncidenciaResponse(
    val vehiculo: String,
    val operador: Int,
    val descripcion_incidencia: String,
    val fecha_hora: String,
    val evidencia_fotografia: String,
    val latitud: Double?,
    val longitud: Double?
)
