package pe.edu.utp.perviajero.data.model

import java.io.Serializable

// Modelo completo para reservas y comprobantes de tours turísticos en el Perú
data class Reserva(
    val codigoReserva: String,
    val destinoId: String,
    val destinoNombre: String,
    val destinoFoto: String,
    val fechaSalida: String,
    val terminalEmbarque: String = "Terminal Javier Prado (La Victoria)",
    val horarioSalida: String = "06:30 AM (Primer Turno)",
    val cantidadPersonas: Int,
    val listaPasajeros: List<Pasajero> = emptyList(),
    val tipoMovilidad: String,
    val categoriaHotel: String,
    val guiaTuristico: String = "Sin Guía Privado",
    val costoTotal: Double,
    var estado: String = "Confirmada", // "Confirmada", "En Ruta", "Completada"
    val usuarioNombre: String,
    val usuarioEmail: String
) : Serializable
