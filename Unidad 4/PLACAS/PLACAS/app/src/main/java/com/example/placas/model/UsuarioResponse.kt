package com.example.placas.model

data class UsuarioResponse(
    val id: Int,
    val nombre_completo: String,
    val tipo_usuario: String,
    val telefono: String?,
    val email: String?,
    val fecha_registro: String,
    val discapacitado: Boolean,
    val estado: Boolean
)