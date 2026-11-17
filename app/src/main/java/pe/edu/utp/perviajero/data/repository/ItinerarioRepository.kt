package pe.edu.utp.perviajero.data.repository

import pe.edu.utp.perviajero.data.model.ActividadItinerario

/**
 * Contrato de persistencia para el planificador de actividades del viajero.
 */
interface ItinerarioRepository {
    fun getItinerario(): List<ActividadItinerario>
    fun guardarActividad(actividad: ActividadItinerario)
    fun eliminarActividad(id: String): Boolean
    fun toggleActividadCompletada(id: String): Boolean
}
