package pe.edu.utp.perviajero.data.model

import java.io.Serializable

// Modelo para reservas de tours realizadas por clientes
data class Reserva(
    val codigoReserva: String,
    val destinoId: String,
    val destinoNombre: String,
    val destinoFoto: String,
    val fechaSalida: String,
    val cantidadPersonas: Int,
    val tipoMovilidad: String,
    val categoriaHotel: String,
    val costoTotal: Double,
    var estado: String = "Confirmada", // "Confirmada", "En Ruta", "Finalizada"
    val usuarioNombre: String,
    val usuarioEmail: String
) : Serializable

