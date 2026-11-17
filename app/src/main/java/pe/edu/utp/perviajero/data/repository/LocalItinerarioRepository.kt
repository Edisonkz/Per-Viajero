package pe.edu.utp.perviajero.data.repository

import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.ActividadItinerario

class LocalItinerarioRepository(
    private val prefs: PreferencesHelper
) : ItinerarioRepository {
    override fun getItinerario(): List<ActividadItinerario> = prefs.getItinerario()
    override fun guardarActividad(actividad: ActividadItinerario) = prefs.guardarActividad(actividad)
    override fun eliminarActividad(id: String): Boolean = prefs.eliminarActividad(id)
    override fun toggleActividadCompletada(id: String): Boolean = prefs.toggleActividadCompletada(id)
}
