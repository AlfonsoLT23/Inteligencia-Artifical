package com.example.placas.model

data class OperadorResponse(
    val id: Int,
    val nombre_completo: String,
    val telefono: String?,
    val email: String?,
    val estado: Boolean
)