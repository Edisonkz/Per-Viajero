package pe.edu.utp.perviajero.data.repository

import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.Reserva

class LocalReservaRepository(
    private val prefs: PreferencesHelper
) : ReservaRepository {
    override fun getReservas(): List<Reserva> = prefs.getReservas()
    override fun guardarReserva(reserva: Reserva) = prefs.guardarReserva(reserva)
    override fun actualizarEstadoReserva(codigoReserva: String, nuevoEstado: String): Boolean =
        prefs.actualizarEstadoReserva(codigoReserva, nuevoEstado)
}
