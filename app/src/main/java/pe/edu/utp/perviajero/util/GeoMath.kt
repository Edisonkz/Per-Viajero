package pe.edu.utp.perviajero.util

import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Funciones geodésicas puras (sin dependencias de Android) para cálculo de distancias, rumbo y tiempos.
 * Permite pruebas unitarias directas y rápidas en JVM.
 */
object GeoMath {

    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Distancia entre dos coordenadas geográficas en kilómetros mediante fórmula de Haversine.
     */
    fun haversineDistanceKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)

        val a = sin(dLat / 2).pow(2) +
            cos(rLat1) * cos(rLat2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    /**
     * Calcula el rumbo (bearing) inicial en grados [0, 360) desde origen hacia destino.
     */
    fun calculateBearingDegrees(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val dLambda = Math.toRadians(lon2 - lon1)

        val y = sin(dLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(dLambda)
        val theta = Math.toDegrees(atan2(y, x))
        return ((theta + 360.0) % 360.0).toFloat()
    }

    /**
     * Convierte un ángulo en grados [0, 360) a abreviatura cardinal en español.
     */
    fun cardinalPoint(degrees: Float): String {
        val normalized = ((degrees % 360f) + 360f) % 360f
        return when {
            normalized >= 337.5f || normalized < 22.5f -> "N"
            normalized < 67.5f -> "NE"
            normalized < 112.5f -> "E"
            normalized < 157.5f -> "SE"
            normalized < 202.5f -> "S"
            normalized < 247.5f -> "SO"
            normalized < 292.5f -> "O"
            else -> "NO"
        }
    }

    fun formatDistance(distanceKm: Double): String {
        return if (distanceKm < 1.0) {
            val meters = (distanceKm * 1000).toInt()
            "$meters m"
        } else {
            String.format(Locale.US, "%.1f km", distanceKm)
        }
    }

    /**
     * Estima duración aproximada por carretera (velocidad media referencial de 58 km/h en geografía peruana).
     */
    fun formatEstimatedTravelTime(distanceKm: Double, durationMinutesOverride: Double? = null): String {
        val totalMinutes = durationMinutesOverride?.toInt()
            ?: ((distanceKm / 58.0) * 60.0).toInt().coerceAtLeast(1)
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        return when {
            hours > 0 && mins > 0 -> "${hours}h ${mins}min"
            hours > 0 -> "${hours}h"
            else -> "${mins} min"
        }
    }

    /**
     * Diferencia angular mínima en [-180, 180] para indicar hacia qué lado girar respecto al rumbo objetivo.
     */
    fun relativeTurnAngle(currentAzimuth: Float, targetBearing: Float): Float {
        var diff = (targetBearing - currentAzimuth + 540f) % 360f - 180f
        if (diff == -180f) diff = 180f
        return diff
    }
}
