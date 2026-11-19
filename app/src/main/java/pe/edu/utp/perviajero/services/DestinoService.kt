package pe.edu.utp.perviajero.services

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.Destino

/**
 * Servicio local de gestión CRUD de destinos turísticos creados/editados por el Administrador.
 * Se fusiona automáticamente con el catálogo remoto de API REST sin sobrescribir la fuente remota.
 */
class DestinoService(
    private val prefs: PreferencesHelper
) {
    private val gson = Gson()

    companion object {
        private const val KEY_DESTINOS_ADMIN = "service_destinos_admin_v1"
        private const val KEY_DESTINOS_OCULTOS = "service_destinos_ocultos_v1"

        fun combinarCatalogo(
            remotos: List<Destino>,
            personalizados: List<Destino>,
            ocultosIds: Set<String>,
            incluirOcultos: Boolean = false
        ): List<Destino> {
            val customById = personalizados.associateBy { it.id }
            val mergedRemote = remotos.map { customById[it.id] ?: it }
            val remoteIds = remotos.map { it.id }.toSet()
            val nuevos = personalizados.filter { it.id !in remoteIds }
            val combinados = nuevos + mergedRemote
            return if (incluirOcultos) combinados else combinados.filter { it.id !in ocultosIds }
        }
    }

    fun getPersonalizados(): List<Destino> {
        val raw = prefs.getRawString(KEY_DESTINOS_ADMIN) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Destino>>() {}.type
            gson.fromJson(raw, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun guardarDestino(destino: Destino) {
        val actuales = getPersonalizados().toMutableList()
        val idx = actuales.indexOfFirst { it.id == destino.id }
        if (idx >= 0) {
            actuales[idx] = destino
        } else {
            actuales.add(0, destino)
        }
        prefs.putRawString(KEY_DESTINOS_ADMIN, gson.toJson(actuales))
    }

    fun getOcultosIds(): Set<String> {
        val raw = prefs.getRawString(KEY_DESTINOS_OCULTOS) ?: return emptySet()
        return try {
            val type = object : TypeToken<Set<String>>() {}.type
            gson.fromJson(raw, type) ?: emptySet()
        } catch (_: Exception) {
            emptySet()
        }
    }

    fun toggleOculto(destinoId: String): Boolean {
        val set = getOcultosIds().toMutableSet()
        val nowHidden = if (set.contains(destinoId)) {
            set.remove(destinoId)
            false
        } else {
            set.add(destinoId)
            true
        }
        prefs.putRawString(KEY_DESTINOS_OCULTOS, gson.toJson(set))
        return nowHidden
    }

    fun estaOculto(destinoId: String): Boolean = getOcultosIds().contains(destinoId)
}
