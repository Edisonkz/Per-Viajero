package pe.edu.utp.perviajero.util

import pe.edu.utp.perviajero.data.model.Reserva
import java.util.Locale

/**
 * Funciones puras de filtrado de reservas para el Panel Administrador (por destino, estado y texto).
 */
object AdminFilterHelper {

    fun filtrarReservas(
        reservas: List<Reserva>,
        destinoFiltro: String = "Todos",
        estadoFiltro: String = "Todas",
        query: String = ""
    ): List<Reserva> {
        val q = query.trim().lowercase(Locale.US)
        return reservas.filter { r ->
            val matchDestino = destinoFiltro.isBlank() ||
                destinoFiltro.equals("Todos", ignoreCase = true) ||
                r.destinoNombre.equals(destinoFiltro, ignoreCase = true)
            val matchEstado = estadoFiltro.isBlank() ||
                estadoFiltro.equals("Todas", ignoreCase = true) ||
                r.estado.equals(estadoFiltro, ignoreCase = true)
            val matchQuery = q.isEmpty() ||
                r.codigoReserva.lowercase(Locale.US).contains(q) ||
                r.usuarioNombre.lowercase(Locale.US).contains(q) ||
                r.usuarioEmail.lowercase(Locale.US).contains(q) ||
                r.destinoNombre.lowercase(Locale.US).contains(q)
            matchDestino && matchEstado && matchQuery
        }
    }
}
