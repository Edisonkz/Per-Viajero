package pe.edu.utp.perviajero.di

import android.content.Context
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.repository.ComunidadRepository
import pe.edu.utp.perviajero.data.repository.DestinoRepository
import pe.edu.utp.perviajero.data.repository.FavoritoRepository
import pe.edu.utp.perviajero.data.repository.ItinerarioRepository
import pe.edu.utp.perviajero.data.repository.LocalComunidadRepository
import pe.edu.utp.perviajero.data.repository.LocalFavoritoRepository
import pe.edu.utp.perviajero.data.repository.LocalItinerarioRepository
import pe.edu.utp.perviajero.data.repository.LocalReservaRepository
import pe.edu.utp.perviajero.data.repository.LocalSessionRepository
import pe.edu.utp.perviajero.data.repository.RemoteDestinoRepository
import pe.edu.utp.perviajero.data.repository.ReservaRepository
import pe.edu.utp.perviajero.data.repository.SessionRepository
import pe.edu.utp.perviajero.network.ApiClient
import pe.edu.utp.perviajero.network.RoutingService
import pe.edu.utp.perviajero.services.ComunidadService
import pe.edu.utp.perviajero.services.ItinerarioService
import pe.edu.utp.perviajero.services.ReservaService
import pe.edu.utp.perviajero.services.UsuarioService

/**
 * Punto único de composición (Dependency Inversion / Service Locator).
 * Los repositorios locales consumen las clases de la capa services/ (respaldadas por JSON en assets/services/).
 */
object ServiceLocator {

    @Volatile private var destinoRepo: DestinoRepository? = null
    @Volatile private var favoritoRepo: FavoritoRepository? = null
    @Volatile private var reservaRepo: ReservaRepository? = null
    @Volatile private var itinerarioRepo: ItinerarioRepository? = null
    @Volatile private var comunidadRepo: ComunidadRepository? = null
    @Volatile private var sessionRepo: SessionRepository? = null

    private val routingServiceInstance: RoutingService by lazy { RoutingService() }

    fun getDestinoRepository(context: Context): DestinoRepository {
        return destinoRepo ?: synchronized(this) {
            destinoRepo ?: RemoteDestinoRepository(
                apiService = ApiClient.service,
                prefs = PreferencesHelper.getInstance(context)
            ).also { destinoRepo = it }
        }
    }

    fun getFavoritoRepository(context: Context): FavoritoRepository {
        return favoritoRepo ?: synchronized(this) {
            favoritoRepo ?: LocalFavoritoRepository(
                prefs = PreferencesHelper.getInstance(context)
            ).also { favoritoRepo = it }
        }
    }

    fun getReservaRepository(context: Context): ReservaRepository {
        return reservaRepo ?: synchronized(this) {
            reservaRepo ?: LocalReservaRepository(
                reservaService = ReservaService(context.applicationContext, PreferencesHelper.getInstance(context))
            ).also { reservaRepo = it }
        }
    }

    fun getItinerarioRepository(context: Context): ItinerarioRepository {
        return itinerarioRepo ?: synchronized(this) {
            itinerarioRepo ?: LocalItinerarioRepository(
                itinerarioService = ItinerarioService(context.applicationContext, PreferencesHelper.getInstance(context))
            ).also { itinerarioRepo = it }
        }
    }

    fun getComunidadRepository(context: Context): ComunidadRepository {
        return comunidadRepo ?: synchronized(this) {
            comunidadRepo ?: LocalComunidadRepository(
                comunidadService = ComunidadService(context.applicationContext, PreferencesHelper.getInstance(context))
            ).also { comunidadRepo = it }
        }
    }

    fun getSessionRepository(context: Context): SessionRepository {
        return sessionRepo ?: synchronized(this) {
            sessionRepo ?: LocalSessionRepository(
                prefs = PreferencesHelper.getInstance(context),
                usuarioService = UsuarioService(context.applicationContext, PreferencesHelper.getInstance(context))
            ).also { sessionRepo = it }
        }
    }

    fun getRoutingService(): RoutingService = routingServiceInstance
}
