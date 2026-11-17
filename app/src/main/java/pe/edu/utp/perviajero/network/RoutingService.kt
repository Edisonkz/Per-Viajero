package pe.edu.utp.perviajero.network

import org.osmdroid.util.GeoPoint
import pe.edu.utp.perviajero.util.AppLogger
import pe.edu.utp.perviajero.util.GeoMath
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

data class RouteCalculationResult(
    val points: List<GeoPoint>,
    val distanceKm: Double,
    val estimatedMinutes: Double?,
    val isRoadRoute: Boolean
)

/**
 * Servicio de cálculo de rutas entre la ubicación GPS actual y el destino turístico.
 * Usa OSRM cuando hay conectividad y cae automáticamente a línea geodésica directa en modo offline.
 */
class RoutingService {

    companion object {
        private const val TAG = "RoutingService"
    }

    fun calculateRoute(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double,
        onComplete: (RouteCalculationResult) -> Unit
    ) {
        val straightDistanceKm = GeoMath.haversineDistanceKm(originLat, originLng, destLat, destLng)
        val fallbackPoints = listOf(GeoPoint(originLat, originLng), GeoPoint(destLat, destLng))

        if (!ApiClient.isOnline()) {
            AppLogger.i(TAG, "Sin conexión: usando trazado geodésico directo (${GeoMath.formatDistance(straightDistanceKm)})")
            onComplete(
                RouteCalculationResult(
                    points = fallbackPoints,
                    distanceKm = straightDistanceKm,
                    estimatedMinutes = null,
                    isRoadRoute = false
                )
            )
            return
        }

        val coords = String.format(
            Locale.US,
            "%.6f,%.6f;%.6f,%.6f",
            originLng, originLat, destLng, destLat
        )

        ApiClient.osrmService.getDrivingRoute(coords).enqueue(object : Callback<OsrmRouteResponse> {
            override fun onResponse(call: Call<OsrmRouteResponse>, response: Response<OsrmRouteResponse>) {
                val route = response.body()?.routes?.firstOrNull()
                val rawCoords = route?.geometry?.coordinates
                if (response.isSuccessful && route != null && !rawCoords.isNullOrEmpty()) {
                    val geoPoints = rawCoords.mapNotNull { pair ->
                        if (pair.size >= 2) GeoPoint(pair[1], pair[0]) else null
                    }
                    if (geoPoints.size >= 2) {
                        val km = route.distanceMeters / 1000.0
                        val mins = route.durationSeconds / 60.0
                        AppLogger.i(TAG, "Ruta vial OSRM trazada (${geoPoints.size} puntos, ${GeoMath.formatDistance(km)})")
                        onComplete(
                            RouteCalculationResult(
                                points = geoPoints,
                                distanceKm = km,
                                estimatedMinutes = mins,
                                isRoadRoute = true
                            )
                        )
                        return
                    }
                }

                AppLogger.w(TAG, "OSRM sin ruta vial disponible (code=${response.code()}); usando línea directa")
                onComplete(
                    RouteCalculationResult(
                        points = fallbackPoints,
                        distanceKm = straightDistanceKm,
                        estimatedMinutes = null,
                        isRoadRoute = false
                    )
                )
            }

            override fun onFailure(call: Call<OsrmRouteResponse>, t: Throwable) {
                AppLogger.w(TAG, "Fallo consultando OSRM (${t.message}); usando trazado directo")
                onComplete(
                    RouteCalculationResult(
                        points = fallbackPoints,
                        distanceKm = straightDistanceKm,
                        estimatedMinutes = null,
                        isRoadRoute = false
                    )
                )
            }
        })
    }
}
