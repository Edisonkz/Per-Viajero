package pe.edu.utp.perviajero.data.repository

import pe.edu.utp.perviajero.data.model.ActividadItinerario
import pe.edu.utp.perviajero.services.SugerenciaViajeDto

/**
 * Contrato de persistencia para el planificador de actividades del viajero y catálogo de sugerencias.
 */
interface ItinerarioRepository {
    fun getItinerario(): List<ActividadItinerario>
    fun guardarActividad(actividad: ActividadItinerario)
    fun eliminarActividad(id: String): Boolean
    fun toggleActividadCompletada(id: String): Boolean
    fun getSugerenciasSemanales(): Map<String, List<SugerenciaViajeDto>>
}
