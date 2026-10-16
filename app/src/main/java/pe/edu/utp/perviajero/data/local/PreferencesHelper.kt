package pe.edu.utp.perviajero.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.data.model.EmpresaInfo
import pe.edu.utp.perviajero.data.model.Reserva
import pe.edu.utp.perviajero.data.model.RolUsuario
import pe.edu.utp.perviajero.data.model.Usuario

// REQ-09 / REQ-10: Persistencia local con SharedPreferences (Sesiones, Roles, Favoritos, Reservas y Datos de Agencia)
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

        // Reservas de Agencia
        private const val KEY_RESERVAS = "key_reservas"

        // Datos Fiscales y Corporativos de la Agencia
        private const val KEY_EMPRESA_INFO = "key_empresa_info"

        // Planificador de Itinerario de Viaje
        private const val KEY_ITINERARIO = "key_itinerario"

        // Bienvenida / Onboarding Inicial
        private const val KEY_FIRST_RUN = "key_first_run"

        @Volatile
        private var instance: PreferencesHelper? = null

        fun getInstance(context: Context): PreferencesHelper {
            return instance ?: synchronized(this) {
                instance ?: PreferencesHelper(context).also { instance = it }
            }
        }
    }

    // --- Gestión de Sesión y Autenticación Local ---
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

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUsuarioActual(): Usuario? {
        if (!isLoggedIn()) return null
        val id = prefs.getString(KEY_USER_ID, "") ?: ""
        val nombre = prefs.getString(KEY_USER_NOMBRE, "Viajero") ?: "Viajero"
        val email = prefs.getString(KEY_USER_EMAIL, "turista@peruviajero.pe") ?: "turista@peruviajero.pe"
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

    // --- Gestión de Datos Fiscales de la Empresa (Modo Administrador) ---
    fun getEmpresaInfo(): EmpresaInfo {
        val json = prefs.getString(KEY_EMPRESA_INFO, null)
        return if (json.isNullOrEmpty()) {
            val porDefecto = EmpresaInfo()
            guardarEmpresaInfo(porDefecto)
            porDefecto
        } else {
            try {
                gson.fromJson(json, EmpresaInfo::class.java) ?: EmpresaInfo()
            } catch (_: Exception) {
                EmpresaInfo()
            }
        }
    }

    fun guardarEmpresaInfo(info: EmpresaInfo) {
        val json = gson.toJson(info)
        prefs.edit().putString(KEY_EMPRESA_INFO, json).apply()
    }

    // --- Gestión de Reservas (Módulo Agencia de Turismo) ---
    fun getReservas(): List<Reserva> {
        val json = prefs.getString(KEY_RESERVAS, null)
        if (json.isNullOrEmpty()) {
            val iniciales = obtenerReservasDemoIniciales()
            guardarListaReservas(iniciales)
            return iniciales
        }
        return try {
            val type = object : TypeToken<List<Reserva>>() {}.type
            gson.fromJson(json, type) ?: obtenerReservasDemoIniciales()
        } catch (_: Exception) {
            obtenerReservasDemoIniciales()
        }
    }

    fun guardarReserva(reserva: Reserva) {
        val lista = getReservas().toMutableList()
        lista.add(0, reserva) // Se agrega al inicio para orden cronológico inverso
        guardarListaReservas(lista)
    }

    fun actualizarEstadoReserva(codigo: String, nuevoEstado: String): Boolean {
        val lista = getReservas().toMutableList()
        val index = lista.indexOfFirst { it.codigoReserva == codigo }
        if (index != -1) {
            lista[index].estado = nuevoEstado
            guardarListaReservas(lista)
            return true
        }
        return false
    }

    private fun guardarListaReservas(lista: List<Reserva>) {
        val json = gson.toJson(lista)
        prefs.edit().putString(KEY_RESERVAS, json).apply()
    }

    private fun obtenerReservasDemoIniciales(): List<Reserva> {
        return listOf(
            Reserva(
                codigoReserva = "PV-2026-8412",
                destinoId = "dest_01",
                destinoNombre = "Santuario Histórico de Machu Picchu",
                destinoFoto = "https://images.unsplash.com/photo-1526392060635-9d6019884377?w=800",
                fechaSalida = "15/11/2026",
                terminalEmbarque = "Estación San Pedro (Cusco)",
                horarioSalida = "06:30 AM (Primer Turno)",
                cantidadPersonas = 2,
                listaPasajeros = listOf(
                    pe.edu.utp.perviajero.data.model.Pasajero("Kevin Turista Quispe", "DNI", "74891234", 23, "984112233"),
                    pe.edu.utp.perviajero.data.model.Pasajero("Andrea Morales Soto", "DNI", "76123456", 22, "984445566")
                ),
                tipoMovilidad = "Minibús Panorámico",
                categoriaHotel = "Hotel Turista 3★",
                guiaTuristico = "Guía Oficial en Español",
                costoTotal = 605.0,
                metodoPago = "Tarjeta Débito Visa",
                numeroOperacion = "OP-98421",
                estado = "Confirmada",
                usuarioNombre = "Kevin Turista",
                usuarioEmail = "kevin@peruviajero.pe"
            ),
            Reserva(
                codigoReserva = "PV-2026-3190",
                destinoId = "dest_02",
                destinoNombre = "Líneas de Nazca",
                destinoFoto = "https://images.unsplash.com/photo-1589556264800-08ae9e129a8c?w=800",
                fechaSalida = "20/11/2026",
                terminalEmbarque = "Terminal Javier Prado (La Victoria)",
                horarioSalida = "04:30 AM (Salida Madrugada)",
                cantidadPersonas = 4,
                listaPasajeros = listOf(
                    pe.edu.utp.perviajero.data.model.Pasajero("Gerson Administrador Flores", "DNI", "71239845", 24, "956123456"),
                    pe.edu.utp.perviajero.data.model.Pasajero("Lucía Ramos Valdivia", "DNI", "72456789", 25, "956987654"),
                    pe.edu.utp.perviajero.data.model.Pasajero("Carlos Benítez Prado", "DNI", "45678912", 30, "956112233"),
                    pe.edu.utp.perviajero.data.model.Pasajero("Mariana Torres Vega", "DNI", "73891245", 21, "956778899")
                ),
                tipoMovilidad = "Camioneta 4x4 Privada",
                categoriaHotel = "Hotel Superior 4★",
                guiaTuristico = "Guía Bilingüe Certificado",
                costoTotal = 1840.0,
                metodoPago = "Yape Móvil",
                numeroOperacion = "OP-33129",
                estado = "En Ruta",
                usuarioNombre = "Gerson Turista",
                usuarioEmail = "gerson@peruviajero.pe"
            )
        )
    }

    // --- Gestión de favoritos ---
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

    // --- Gestión de tema visual (Claro/Oscuro) ---
    fun isDarkMode(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    // --- Último término buscado ---
    fun getLastSearch(): String {
        return prefs.getString(KEY_LAST_SEARCH, "") ?: ""
    }

    fun saveLastSearch(query: String) {
        prefs.edit().putString(KEY_LAST_SEARCH, query).apply()
    }

    // --- Bienvenida / Onboarding Inicial ---
    fun isFirstRun(): Boolean {
        return prefs.getBoolean(KEY_FIRST_RUN, true)
    }

    fun setFirstRunCompleted() {
        prefs.edit().putBoolean(KEY_FIRST_RUN, false).apply()
    }

    fun resetFirstRun() {
        prefs.edit().putBoolean(KEY_FIRST_RUN, true).apply()
    }

    // --- Gestión de Planificador de Itinerario (Turista) ---
    fun getItinerario(): List<pe.edu.utp.perviajero.data.model.ActividadItinerario> {
        val json = prefs.getString(KEY_ITINERARIO, null)
        if (json.isNullOrEmpty()) {
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val cal = java.util.Calendar.getInstance()
            val fechaHoy = sdf.format(cal.time)
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
            val fechaManana = sdf.format(cal.time)
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
            val fechaPasado = sdf.format(cal.time)

            val demo = listOf(
                pe.edu.utp.perviajero.data.model.ActividadItinerario(
                    id = "act_01",
                    dia = "Día 1",
                    fecha = fechaHoy,
                    horario = "Mañana (09:00 AM)",
                    lugar = "Plaza de Armas y Centro de Cusco",
                    descripcion = "Llegada, check-in en el hotel y aclimatación con mate de coca.",
                    completada = true
                ),
                pe.edu.utp.perviajero.data.model.ActividadItinerario(
                    id = "act_02",
                    dia = "Día 2",
                    fecha = fechaManana,
                    horario = "Mañana (06:00 AM)",
                    lugar = "Santuario Histórico de Machu Picchu",
                    descripcion = "Tour guiado por la ciudadela inca y sesión de fotos en el mirador.",
                    completada = false
                ),
                pe.edu.utp.perviajero.data.model.ActividadItinerario(
                    id = "act_03",
                    dia = "Día 3",
                    fecha = fechaPasado,
                    horario = "Tarde (01:00 PM)",
                    lugar = "Mercado de San Pedro",
                    descripcion = "Almuerzo gastronómico y compra de artesanías y souvenirs.",
                    completada = false
                )
            )
            guardarListaItinerario(demo)
            return demo
        }
        return try {
            val type = object : TypeToken<List<pe.edu.utp.perviajero.data.model.ActividadItinerario>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun guardarActividad(actividad: pe.edu.utp.perviajero.data.model.ActividadItinerario) {
        val lista = getItinerario().toMutableList()
        lista.add(actividad)
        guardarListaItinerario(lista)
    }

    fun eliminarActividad(id: String): Boolean {
        val lista = getItinerario().toMutableList()
        val eliminada = lista.removeAll { it.id == id }
        if (eliminada) {
            guardarListaItinerario(lista)
        }
        return eliminada
    }

    fun toggleActividadCompletada(id: String): Boolean {
        val lista = getItinerario().toMutableList()
        val item = lista.find { it.id == id }
        return if (item != null) {
            item.completada = !item.completada
            guardarListaItinerario(lista)
            item.completada
        } else {
            false
        }
    }

    private fun guardarListaItinerario(lista: List<pe.edu.utp.perviajero.data.model.ActividadItinerario>) {
        val json = gson.toJson(lista)
        prefs.edit().putString(KEY_ITINERARIO, json).apply()
    }

    // --- Respaldo y Persistencia Local de Destinos de la API (Soporte Offline) ---
    fun guardarDestinosEnCache(lista: List<Destino>) {
        val json = gson.toJson(lista)
        prefs.edit().putString(KEY_CACHED_DESTINOS, json).apply()
    }

    fun obtenerDestinosEnCache(): List<Destino> {
        val json = prefs.getString(KEY_CACHED_DESTINOS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Destino>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

