package pe.edu.utp.perviajero.services

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.ActividadItinerario
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ActividadSeedDto(
    val id: String,
    val dia: String,
    val dayOffset: Int = 0,
    val horario: String,
    val lugar: String,
    val descripcion: String,
    val completada: Boolean = false,
    val fotoUri: String = ""
)

data class SugerenciaViajeDto(
    val lugar: String,
    val horario: String,
    val descripcion: String
)

/**
 * Servicio de Itinerario y Sugerencias alimentado desde assets/services/itinerario.json y sugerencias_semanales.json.
 */
class ItinerarioService(
    private val context: Context,
    private val prefs: PreferencesHelper
) {
    private val gson = Gson()

    companion object {
        private const val ASSET_ITINERARIO = "services/itinerario.json"
        private const val ASSET_SUGERENCIAS = "services/sugerencias_semanales.json"
        private const val KEY_ITINERARIO = "key_itinerario"
    }

    fun getActividades(): List<ActividadItinerario> {
        val json = prefs.getRawString(KEY_ITINERARIO)
        if (!json.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<ActividadItinerario>>() {}.type
                val list: List<ActividadItinerario>? = gson.fromJson(json, type)
                if (list != null) return list
            } catch (_: Exception) { }
        }

        val type = object : TypeToken<List<ActividadSeedDto>>() {}.type
        val seedDtos = JsonAssetReader.read<List<ActividadSeedDto>>(context, ASSET_ITINERARIO, type).orEmpty()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val seed = seedDtos.map { dto ->
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dto.dayOffset) }
            ActividadItinerario(
                id = dto.id,
                dia = dto.dia,
                fecha = sdf.format(cal.time),
                horario = dto.horario,
                lugar = dto.lugar,
                descripcion = dto.descripcion,
                completada = dto.completada,
                fotoUri = dto.fotoUri
            )
        }
        guardarLista(seed)
        return seed
    }

    fun guardarActividad(actividad: ActividadItinerario) {
        val lista = getActividades().toMutableList()
        lista.add(actividad)
        guardarLista(lista)
    }

    fun eliminarActividad(id: String): Boolean {
        val lista = getActividades().toMutableList()
        val eliminada = lista.removeAll { it.id == id }
        if (eliminada) guardarLista(lista)
        return eliminada
    }

    fun toggleCompletada(id: String): Boolean {
        val lista = getActividades().toMutableList()
        val item = lista.find { it.id == id } ?: return false
        item.completada = !item.completada
        guardarLista(lista)
        return item.completada
    }

    fun getSugerenciasSemanales(): Map<String, List<SugerenciaViajeDto>> {
        val type = object : TypeToken<Map<String, List<SugerenciaViajeDto>>>() {}.type
        return JsonAssetReader.read<Map<String, List<SugerenciaViajeDto>>>(context, ASSET_SUGERENCIAS, type).orEmpty()
    }

    private fun guardarLista(lista: List<ActividadItinerario>) {
        prefs.putRawString(KEY_ITINERARIO, gson.toJson(lista))
    }
}
