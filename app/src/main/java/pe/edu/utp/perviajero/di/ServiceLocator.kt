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

/**
 * Punto único de composición (Dependency Inversion / Service Locator).
 * Para migrar a Room o Firebase en el futuro, basta sustituir la implementación devuelta aquí
 * sin modificar Activities, Fragments ni Adapters.
 */
object ServiceLocator {

    @Volatile
    private var destinoRepo: DestinoRepository? = null

    @Volatile
    private var favoritoRepo: FavoritoRepository? = null

    @Volatile
    private var reservaRepo: ReservaRepository? = null

    @Volatile
    private var itinerarioRepo: ItinerarioRepository? = null

    @Volatile
    private var comunidadRepo: ComunidadRepository? = null

    @Volatile
    private var sessionRepo: SessionRepository? = null

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
                prefs = PreferencesHelper.getInstance(context)
            ).also { reservaRepo = it }
        }
    }

    fun getItinerarioRepository(context: Context): ItinerarioRepository {
        return itinerarioRepo ?: synchronized(this) {
            itinerarioRepo ?: LocalItinerarioRepository(
                prefs = PreferencesHelper.getInstance(context)
            ).also { itinerarioRepo = it }
        }
    }

    fun getComunidadRepository(context: Context): ComunidadRepository {
        return comunidadRepo ?: synchronized(this) {
            comunidadRepo ?: LocalComunidadRepository(
                prefs = PreferencesHelper.getInstance(context)
            ).also { comunidadRepo = it }
        }
    }

    fun getSessionRepository(context: Context): SessionRepository {
        return sessionRepo ?: synchronized(this) {
            sessionRepo ?: LocalSessionRepository(
                prefs = PreferencesHelper.getInstance(context)
            ).also { sessionRepo = it }
        }
    }

    fun getRoutingService(): RoutingService = routingServiceInstance
}
