package pe.edu.utp.perviajero.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.data.model.EmpresaInfo
import pe.edu.utp.perviajero.data.model.RolUsuario
import pe.edu.utp.perviajero.data.model.Usuario

/**
 * Capa pura de almacenamiento clave-valor en SharedPreferences.
 * No contiene datos mock hardcodeados; los datos iniciales provienen de assets/services/.
 */
class PreferencesHelper private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "peruviajero_prefs"
        private const val KEY_FAVORITOS = "key_favoritos"
        private const val KEY_DARK_MODE = "key_dark_mode"
        private const val KEY_LAST_SEARCH = "key_last_search"
        private const val KEY_CACHED_DESTINOS = "key_cached_destinos"

        // Sesión y Rol
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_USER_NOMBRE = "key_user_nombre"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_USER_ROL = "key_user_rol"
        private const val KEY_USER_FOTO_URI = "key_user_foto_uri"

        // Datos Fiscales de Agencia
        private const val KEY_EMPRESA_INFO = "key_empresa_info"

        // Ubicación GPS cacheada y Push FCM
        private const val KEY_LAST_LAT = "key_last_lat"
        private const val KEY_LAST_LNG = "key_last_lng"
        private const val KEY_FCM_TOKEN = "key_fcm_token"

        // Onboarding (v2 garantiza mostrar la nueva Bienvenida Épica al iniciar por primera vez)
        private const val KEY_FIRST_RUN = "key_first_run_v2"

        @Volatile
        private var instance: PreferencesHelper? = null

        fun getInstance(context: Context): PreferencesHelper {
            return instance ?: synchronized(this) {
                instance ?: PreferencesHelper(context).also { instance = it }
            }
        }
    }

    fun getRawString(key: String): String? = prefs.getString(key, null)

    fun putRawString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    // --- Sesión y Rol ---
    fun login(usuario: Usuario) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_ID, usuario.id)
            .putString(KEY_USER_NOMBRE, usuario.nombre)
            .putString(KEY_USER_EMAIL, usuario.email)
            .putString(KEY_USER_ROL, usuario.rol.name)
            .apply()
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NOMBRE)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_ROL)
            .apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUsuarioActual(): Usuario? {
        if (!isLoggedIn()) return null
        val id = prefs.getString(KEY_USER_ID, "") ?: ""
        val nombre = prefs.getString(KEY_USER_NOMBRE, "") ?: ""
        val email = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        val rolStr = prefs.getString(KEY_USER_ROL, RolUsuario.CLIENTE.name) ?: RolUsuario.CLIENTE.name
        val rol = try {
            RolUsuario.valueOf(rolStr)
        } catch (_: Exception) {
            RolUsuario.CLIENTE
        }
        return Usuario(id, nombre, email, rol)
    }

    fun getRolActual(): RolUsuario {
        val rolStr = prefs.getString(KEY_USER_ROL, RolUsuario.CLIENTE.name) ?: RolUsuario.CLIENTE.name
        return try {
            RolUsuario.valueOf(rolStr)
        } catch (_: Exception) {
            RolUsuario.CLIENTE
        }
    }

    fun cambiarRol(nuevoRol: RolUsuario) {
        prefs.edit().putString(KEY_USER_ROL, nuevoRol.name).apply()
    }

    fun getUserFotoUri(): String = prefs.getString(KEY_USER_FOTO_URI, "") ?: ""

    fun setUserFotoUri(uri: String) {
        prefs.edit().putString(KEY_USER_FOTO_URI, uri).apply()
    }

    // --- Empresa ---
    fun getEmpresaInfoOrNull(): EmpresaInfo? {
        val json = prefs.getString(KEY_EMPRESA_INFO, null) ?: return null
        return try {
            gson.fromJson(json, EmpresaInfo::class.java)
        } catch (_: Exception) {
            null
        }
    }

    fun guardarEmpresaInfo(info: EmpresaInfo) {
        prefs.edit().putString(KEY_EMPRESA_INFO, gson.toJson(info)).apply()
    }

    // --- Favoritos ---
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

    fun getFavoritosIds(): Set<String> = prefs.getStringSet(KEY_FAVORITOS, emptySet()) ?: emptySet()

    // --- Tema Visual ---
    fun isDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, false)

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    // --- Búsqueda ---
    fun getLastSearch(): String = prefs.getString(KEY_LAST_SEARCH, "") ?: ""

    fun saveLastSearch(query: String) {
        prefs.edit().putString(KEY_LAST_SEARCH, query).apply()
    }

    // --- Onboarding ---
    fun isFirstRun(): Boolean = prefs.getBoolean(KEY_FIRST_RUN, true)

    fun setFirstRunCompleted() {
        prefs.edit().putBoolean(KEY_FIRST_RUN, false).apply()
    }

    fun resetFirstRun() {
        prefs.edit().putBoolean(KEY_FIRST_RUN, true).apply()
    }

    // --- GPS & FCM ---
    fun saveLastLocation(lat: Double, lng: Double) {
        prefs.edit()
            .putString(KEY_LAST_LAT, lat.toString())
            .putString(KEY_LAST_LNG, lng.toString())
            .apply()
    }

    fun getLastLocation(): Pair<Double, Double>? {
        val lat = prefs.getString(KEY_LAST_LAT, null)?.toDoubleOrNull() ?: return null
        val lng = prefs.getString(KEY_LAST_LNG, null)?.toDoubleOrNull() ?: return null
        return lat to lng
    }

    fun saveFcmToken(token: String) {
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    fun getFcmToken(): String = prefs.getString(KEY_FCM_TOKEN, "") ?: ""

    // --- Caché de Destinos ---
    fun guardarDestinosEnCache(lista: List<Destino>) {
        prefs.edit().putString(KEY_CACHED_DESTINOS, gson.toJson(lista)).apply()
    }

    fun obtenerDestinosEnCache(): List<Destino> {
        val json = prefs.getString(KEY_CACHED_DESTINOS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Destino>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
