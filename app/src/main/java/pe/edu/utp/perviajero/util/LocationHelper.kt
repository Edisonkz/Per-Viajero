package pe.edu.utp.perviajero.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import pe.edu.utp.perviajero.di.ServiceLocator

data class UserLocationResult(
    val latitude: Double,
    val longitude: Double,
    val isFallbackCached: Boolean
)

/**
 * Encapsula la lectura de ubicación GPS usando FusedLocationProviderClient con respaldo de última ubicación.
 */
class LocationHelper(context: Context) {

    private val appContext = context.applicationContext
    private val fusedClient = LocationServices.getFusedLocationProviderClient(appContext)
    private val sessionRepo = ServiceLocator.getSessionRepository(appContext)

    companion object {
        private const val TAG = "LocationHelper"
    }

    fun isGpsProviderEnabled(): Boolean {
        val lm = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    fun obtainCurrentLocation(onResult: (UserLocationResult?) -> Unit) {
        if (!PermissionHelper.hasLocationPermission(appContext)) {
            val cached = sessionRepo.getLastLocation()
            onResult(cached?.let { UserLocationResult(it.first, it.second, isFallbackCached = true) })
            return
        }

        val tokenSource = CancellationTokenSource()
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    sessionRepo.saveLastLocation(location.latitude, location.longitude)
                    AppLogger.i(TAG, "GPS obtenido: ${location.latitude}, ${location.longitude}")
                    onResult(UserLocationResult(location.latitude, location.longitude, isFallbackCached = false))
                } else {
                    fetchLastKnownLocation(onResult)
                }
            }
            .addOnFailureListener { e ->
                AppLogger.w(TAG, "getCurrentLocation falló (${e.message}), intentando lastLocation")
                fetchLastKnownLocation(onResult)
            }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLastKnownLocation(onResult: (UserLocationResult?) -> Unit) {
        fusedClient.lastLocation
            .addOnSuccessListener { last ->
                if (last != null) {
                    sessionRepo.saveLastLocation(last.latitude, last.longitude)
                    onResult(UserLocationResult(last.latitude, last.longitude, isFallbackCached = true))
                } else {
                    val cached = sessionRepo.getLastLocation()
                    onResult(cached?.let { UserLocationResult(it.first, it.second, isFallbackCached = true) })
                }
            }
            .addOnFailureListener {
                val cached = sessionRepo.getLastLocation()
                onResult(cached?.let { UserLocationResult(it.first, it.second, isFallbackCached = true) })
            }
    }
}
