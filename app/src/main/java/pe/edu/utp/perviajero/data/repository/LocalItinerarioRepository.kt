package pe.edu.utp.perviajero.data.repository

import pe.edu.utp.perviajero.data.model.ActividadItinerario
import pe.edu.utp.perviajero.services.ItinerarioService
import pe.edu.utp.perviajero.services.SugerenciaViajeDto

class LocalItinerarioRepository(
    private val itinerarioService: ItinerarioService
) : ItinerarioRepository {
    override fun getItinerario(): List<ActividadItinerario> = itinerarioService.getActividades()
    override fun guardarActividad(actividad: ActividadItinerario) = itinerarioService.guardarActividad(actividad)
    override fun eliminarActividad(id: String): Boolean = itinerarioService.eliminarActividad(id)
    override fun toggleActividadCompletada(id: String): Boolean = itinerarioService.toggleCompletada(id)
    override fun getSugerenciasSemanales(): Map<String, List<SugerenciaViajeDto>> =
        itinerarioService.getSugerenciasSemanales()
}
