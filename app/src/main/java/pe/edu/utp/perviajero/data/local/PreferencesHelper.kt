package pe.edu.utp.perviajero.data.local

import android.content.Context
import android.content.SharedPreferences

// REQ-09 / REQ-10: Persistencia local con SharedPreferences (Favoritos y Tema)
class PreferencesHelper private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "peruviajero_prefs"
        private const val KEY_FAVORITOS = "key_favoritos"
        private const val KEY_DARK_MODE = "key_dark_mode"
        private const val KEY_LAST_SEARCH = "key_last_search"

        @Volatile
        private var instance: PreferencesHelper? = null

        fun getInstance(context: Context): PreferencesHelper {
            return instance ?: synchronized(this) {
                instance ?: PreferencesHelper(context).also { instance = it }
            }
        }
    }

    // Gestión de favoritos
    fun isFavorito(id: String): Boolean {
        val favoritos = prefs.getStringSet(KEY_FAVORITOS, emptySet()) ?: emptySet()
        return favoritos.contains(id)
    }

    fun toggleFavorito(id: String): Boolean {
        val current = prefs.getStringSet(KEY_FAVORITOS, emptySet())?.toMutableSet() ?: mutableSetOf()
        val newState = if (current.contains(id)) {
            current.remove(id)
            false
        } else {
            current.add(id)
            true
        }
        prefs.edit().putStringSet(KEY_FAVORITOS, current).apply()
        return newState
    }

    fun getFavoritosIds(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITOS, emptySet()) ?: emptySet()
    }

    // Gestión de tema visual (Claro/Oscuro)
    fun isDarkMode(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    // Último término buscado
    fun getLastSearch(): String {
        return prefs.getString(KEY_LAST_SEARCH, "") ?: ""
    }

    fun saveLastSearch(query: String) {
        prefs.edit().putString(KEY_LAST_SEARCH, query).apply()
    }
}

