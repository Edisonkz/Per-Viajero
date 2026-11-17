package pe.edu.utp.perviajero.data.repository

import pe.edu.utp.perviajero.data.model.Reserva
import pe.edu.utp.perviajero.services.ReservaService

class LocalReservaRepository(
    private val reservaService: ReservaService
) : ReservaRepository {
    override fun getReservas(): List<Reserva> = reservaService.getReservas()
    override fun guardarReserva(reserva: Reserva) = reservaService.guardarReserva(reserva)
    override fun actualizarEstadoReserva(codigoReserva: String, nuevoEstado: String): Boolean =
        reservaService.actualizarEstadoReserva(codigoReserva, nuevoEstado)
}
