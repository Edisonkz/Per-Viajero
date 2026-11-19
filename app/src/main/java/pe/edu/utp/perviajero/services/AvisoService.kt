package pe.edu.utp.perviajero.services

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.AvisoTuristico

/**
 * Servicio local de Bandeja de Avisos respaldado por assets/services/avisos.json y SharedPreferences.
 * Conserva hasta los últimos 50 avisos con estado leído/no leído.
 */
class AvisoService(
    private val context: Context,
    private val prefs: PreferencesHelper
) {
    private val gson = Gson()

    companion object {
        private const val ASSET_AVISOS = "services/avisos.json"
        private const val KEY_AVISOS_JSON = "service_avisos_historial_v1"
        private const val MAX_AVISOS = 50
    }

    fun listar(): List<AvisoTuristico> {
        val type = object : TypeToken<List<AvisoTuristico>>() {}.type
        val stored = prefs.getRawString(KEY_AVISOS_JSON)
        val lista = if (!stored.isNullOrBlank()) {
            try {
                gson.fromJson<List<AvisoTuristico>>(stored, type) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            val seed = JsonAssetReader.read<List<AvisoTuristico>>(context, ASSET_AVISOS, type) ?: emptyList()
            guardar(seed)
            seed
        }
        return lista.sortedByDescending { it.hora }
    }

    fun obtenerPorId(id: Int): AvisoTuristico? = listar().find { it.id == id }

    fun agregar(aviso: AvisoTuristico) {
        val actuales = listar().filterNot { it.id == aviso.id }
        guardar((listOf(aviso) + actuales).take(MAX_AVISOS))
    }

    fun marcarLeido(id: Int) {
        val actualizados = listar().map { if (it.id == id) it.copy(leido = true) else it }
        guardar(actualizados)
    }

    fun marcarTodosLeidos() {
        val actualizados = listar().map { it.copy(leido = true) }
        guardar(actualizados)
    }

    fun contarNoLeidos(): Int = listar().count { !it.leido }

    fun limpiar() {
        guardar(emptyList())
    }

    private fun guardar(lista: List<AvisoTuristico>) {
        prefs.putRawString(KEY_AVISOS_JSON, gson.toJson(lista.take(MAX_AVISOS)))
    }
}
