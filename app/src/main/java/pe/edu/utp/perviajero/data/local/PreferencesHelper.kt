package pe.edu.utp.perviajero.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pe.edu.utp.perviajero.data.model.ActividadItinerario
import pe.edu.utp.perviajero.data.model.ComentarioPublicacion
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.data.model.EmpresaInfo
import pe.edu.utp.perviajero.data.model.Pasajero
import pe.edu.utp.perviajero.data.model.PublicacionViajero
import pe.edu.utp.perviajero.data.model.Reserva
import pe.edu.utp.perviajero.data.model.RolUsuario
import pe.edu.utp.perviajero.data.model.Usuario
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Persistencia local con SharedPreferences (implementación base reemplazable vía interfaces Repository)
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

        // Reservas, Agencia, Itinerario y Comunidad Social
        private const val KEY_RESERVAS = "key_reservas"
        private const val KEY_EMPRESA_INFO = "key_empresa_info"
        private const val KEY_ITINERARIO = "key_itinerario"
        private const val KEY_PUBLICACIONES = "key_publicaciones"

        // Ubicación GPS cacheada y Push FCM
        private const val KEY_LAST_LAT = "key_last_lat"
        private const val KEY_LAST_LNG = "key_last_lng"
        private const val KEY_FCM_TOKEN = "key_fcm_token"

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

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

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

    fun getUserFotoUri(): String = prefs.getString(KEY_USER_FOTO_URI, "") ?: ""

    fun setUserFotoUri(uri: String) {
        prefs.edit().putString(KEY_USER_FOTO_URI, uri).apply()
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
        prefs.edit().putString(KEY_EMPRESA_INFO, gson.toJson(info)).apply()
    }

    // --- Gestión de Reservas ---
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
        lista.add(0, reserva)
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
        prefs.edit().putString(KEY_RESERVAS, gson.toJson(lista)).apply()
    }

    private fun obtenerReservasDemoIniciales(): List<Reserva> {
        return listOf(
            Reserva(
                codigoReserva = "PV-2026-8412",
                destinoId = "1",
                destinoNombre = "Santuario Histórico de Machu Picchu",
                destinoFoto = "https://images.unsplash.com/photo-1526392060635-9d6019884377?w=800",
                fechaSalida = "15/11/2026",
                terminalEmbarque = "Estación San Pedro (Cusco)",
                horarioSalida = "06:30 AM (Primer Turno)",
                cantidadPersonas = 2,
                listaPasajeros = listOf(
                    Pasajero("Kevin Turista Quispe", "DNI", "74891234", 23, "984112233"),
                    Pasajero("Andrea Morales Soto", "DNI", "76123456", 22, "984445566")
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
                destinoId = "6",
                destinoNombre = "Líneas y Geoglifos de Nazca",
                destinoFoto = "https://images.unsplash.com/photo-1544986581-efac024faf62?w=800",
                fechaSalida = "20/11/2026",
                terminalEmbarque = "Terminal Javier Prado (La Victoria)",
                horarioSalida = "04:30 AM (Salida Madrugada)",
                cantidadPersonas = 4,
                listaPasajeros = listOf(
                    Pasajero("Gerson Administrador Flores", "DNI", "71239845", 24, "956123456"),
                    Pasajero("Lucía Ramos Valdivia", "DNI", "72456789", 25, "956987654"),
                    Pasajero("Carlos Benítez Prado", "DNI", "45678912", 30, "956112233"),
                    Pasajero("Mariana Torres Vega", "DNI", "73891245", 21, "956778899")
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

    // --- Gestión de Comunidad Social de Viajeros ---
    fun getPublicaciones(): List<PublicacionViajero> {
        val json = prefs.getString(KEY_PUBLICACIONES, null)
        if (json.isNullOrEmpty()) {
            val iniciales = obtenerPublicacionesDemoIniciales()
            guardarListaPublicaciones(iniciales)
            return iniciales
        }
        return try {
            val type = object : TypeToken<List<PublicacionViajero>>() {}.type
            gson.fromJson(json, type) ?: obtenerPublicacionesDemoIniciales()
        } catch (_: Exception) {
            obtenerPublicacionesDemoIniciales()
        }
    }

    fun guardarListaPublicaciones(lista: List<PublicacionViajero>) {
        prefs.edit().putString(KEY_PUBLICACIONES, gson.toJson(lista)).apply()
    }

    private fun obtenerPublicacionesDemoIniciales(): List<PublicacionViajero> {
        return listOf(
            PublicacionViajero(
                id = "pub_01",
                autorNombre = "Camila Mendoza",
                autorEmail = "camila.m@viajero.pe",
                region = "Cusco",
                destinoId = "1",
                destinoNombre = "Santuario Histórico de Machu Picchu",
                fotoUri = "https://images.unsplash.com/photo-1526392060635-9d6019884377?w=800",
                calificacion = 5.0f,
                comentario = "Subir en el primer bus de las 5:30 a. m. vale totalmente la pena para ver despejar la neblina sobre el Huayna Picchu. Lleven poncho de lluvia y agua.",
                fechaPublicacion = "12/11/2026",
                likes = 24,
                dioLike = false,
                comentarios = listOf(
                    ComentarioPublicacion("c1", "Diego Paredes", "¡Gran consejo! ¿Con cuánta anticipación compraste la entrada?", "13/11/2026")
                )
            ),
            PublicacionViajero(
                id = "pub_02",
                autorNombre = "Mateo Quispe",
                autorEmail = "mateo.q@viajero.pe",
                region = "Arequipa",
                destinoId = "2",
                destinoNombre = "Cañón del Colca",
                fotoUri = "https://images.unsplash.com/photo-1589308078059-be1415eab4c3?w=800",
                calificacion = 4.8f,
                comentario = "En la Cruz del Cóndor vimos 7 cóndores planeando a las 7:40 a. m. Recomendado dormir en Chivay y probar las aguas termales La Calera.",
                fechaPublicacion = "10/11/2026",
                likes = 18,
                dioLike = false
            ),
            PublicacionViajero(
                id = "pub_03",
                autorNombre = "Valeria Rojas",
                autorEmail = "valeria.r@viajero.pe",
                region = "Ica",
                destinoId = "3",
                destinoNombre = "Oasis de la Huacachina",
                fotoUri = "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?w=800",
                calificacion = 4.7f,
                comentario = "El tour de tubulares a las 4:30 p. m. te deja justo arriba de la duna grande para el atardecer. Lleven lentes cerrados por la arena.",
                fechaPublicacion = "08/11/2026",
                likes = 15,
                dioLike = false
            ),
            PublicacionViajero(
                id = "pub_04",
                autorNombre = "Andrés Castillo",
                autorEmail = "andres.c@viajero.pe",
                region = "Áncash",
                destinoId = "8",
                destinoNombre = "Laguna 69 y Parque Nac. Huascarán",
                fotoUri = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
                calificacion = 5.0f,
                comentario = "Caminata exigente sobre los 4,600 m s. n. m., pero el azul turquesa de la laguna frente al Chacraraju es inolvidable. Aclimatarse mínimo 2 días en Huaraz.",
                fechaPublicacion = "05/11/2026",
                likes = 31,
                dioLike = false
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

    fun getFavoritosIds(): Set<String> = prefs.getStringSet(KEY_FAVORITOS, emptySet()) ?: emptySet()

    // --- Gestión de tema visual (Claro/Oscuro) ---
    fun isDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, false)

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    // --- Último término buscado ---
    fun getLastSearch(): String = prefs.getString(KEY_LAST_SEARCH, "") ?: ""

    fun saveLastSearch(query: String) {
        prefs.edit().putString(KEY_LAST_SEARCH, query).apply()
    }

    // --- Bienvenida / Onboarding Inicial ---
    fun isFirstRun(): Boolean = prefs.getBoolean(KEY_FIRST_RUN, true)

    fun setFirstRunCompleted() {
        prefs.edit().putBoolean(KEY_FIRST_RUN, false).apply()
    }

    fun resetFirstRun() {
        prefs.edit().putBoolean(KEY_FIRST_RUN, true).apply()
    }

    // --- Última ubicación GPS conocida (para brújula offline) ---
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

    // --- Token FCM ---
    fun saveFcmToken(token: String) {
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    fun getFcmToken(): String = prefs.getString(KEY_FCM_TOKEN, "") ?: ""

    // --- Gestión de Planificador de Itinerario (Turista) ---
    fun getItinerario(): List<ActividadItinerario> {
        val json = prefs.getString(KEY_ITINERARIO, null)
        if (json.isNullOrEmpty()) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val cal = Calendar.getInstance()
            val fechaHoy = sdf.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val fechaManana = sdf.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val fechaPasado = sdf.format(cal.time)

            val demo = listOf(
                ActividadItinerario(
                    id = "act_01",
                    dia = "Día 1",
                    fecha = fechaHoy,
                    horario = "Mañana (09:00 AM)",
                    lugar = "Plaza de Armas y Centro de Cusco",
                    descripcion = "Llegada, check-in en el hotel y aclimatación con mate de coca.",
                    completada = true
                ),
                ActividadItinerario(
                    id = "act_02",
                    dia = "Día 2",
                    fecha = fechaManana,
                    horario = "Mañana (06:00 AM)",
                    lugar = "Santuario Histórico de Machu Picchu",
                    descripcion = "Tour guiado por la ciudadela inca y sesión de fotos en el mirador.",
                    completada = false
                ),
                ActividadItinerario(
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
            val type = object : TypeToken<List<ActividadItinerario>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun guardarActividad(actividad: ActividadItinerario) {
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

    private fun guardarListaItinerario(lista: List<ActividadItinerario>) {
        prefs.edit().putString(KEY_ITINERARIO, gson.toJson(lista)).apply()
    }

    // --- Respaldo y Persistencia Local de Destinos de la API (Soporte Offline) ---
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
