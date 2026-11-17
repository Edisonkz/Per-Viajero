package pe.edu.utp.perviajero.services

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.PublicacionViajero

/**
 * Servicio de Comunidad Social alimentado inicialmente desde assets/services/comunidad.json.
 */
class ComunidadService(
    private val context: Context,
    private val prefs: PreferencesHelper
) {
    private val gson = Gson()

    companion object {
        private const val ASSET_COMUNIDAD = "services/comunidad.json"
        private const val KEY_PUBLICACIONES = "key_publicaciones"
    }

    fun getPublicaciones(): List<PublicacionViajero> {
        val json = prefs.getRawString(KEY_PUBLICACIONES)
        if (!json.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<PublicacionViajero>>() {}.type
                val list: List<PublicacionViajero>? = gson.fromJson(json, type)
                if (list != null) return list
            } catch (_: Exception) { }
        }
        val type = object : TypeToken<List<PublicacionViajero>>() {}.type
        val seed = JsonAssetReader.read<List<PublicacionViajero>>(context, ASSET_COMUNIDAD, type).orEmpty()
        guardarLista(seed)
        return seed
    }

    fun guardarLista(lista: List<PublicacionViajero>) {
        prefs.putRawString(KEY_PUBLICACIONES, gson.toJson(lista))
    }
}
