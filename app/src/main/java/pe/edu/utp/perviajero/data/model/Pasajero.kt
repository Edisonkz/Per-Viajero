package pe.edu.utp.perviajero.data.model

import java.io.Serializable

// Modelo para el manifiesto de pasajeros exigido por MTC / SUTRAN / MINCETUR en el Perú
data class Pasajero(
    val nombresApellidos: String,
    val tipoDocumento: String = "DNI", // "DNI", "Pasaporte", "Carnet Extranjería"
    val numeroDocumento: String,
    val edad: Int = 25,
    val telefonoContacto: String = ""
) : Serializable
