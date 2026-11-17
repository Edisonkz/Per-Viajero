package pe.edu.utp.perviajero.data.repository

import pe.edu.utp.perviajero.data.model.Reserva

/**
 * Contrato de persistencia para reservas de tours emitidas por la agencia.
 */
interface ReservaRepository {
    fun getReservas(): List<Reserva>
    fun guardarReserva(reserva: Reserva)
    fun actualizarEstadoReserva(codigoReserva: String, nuevoEstado: String): Boolean
}
