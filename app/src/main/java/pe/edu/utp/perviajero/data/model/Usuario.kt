package pe.edu.utp.perviajero.data.model

import java.io.Serializable

enum class RolUsuario {
    CLIENTE,
    ADMIN
}

data class Usuario(
    val id: String,
    val nombre: String,
    val email: String,
    val rol: RolUsuario
) : Serializable

