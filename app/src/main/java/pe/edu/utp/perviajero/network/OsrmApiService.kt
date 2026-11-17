package pe.edu.utp.perviajero.network

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class OsrmRouteResponse(
    @SerializedName("code") val code: String? = null,
    @SerializedName("routes") val routes: List<OsrmRoute>? = null
)

data class OsrmRoute(
    @SerializedName("distance") val distanceMeters: Double = 0.0,
    @SerializedName("duration") val durationSeconds: Double = 0.0,
    @SerializedName("geometry") val geometry: OsrmGeometry? = null
)

data class OsrmGeometry(
    @SerializedName("coordinates") val coordinates: List<List<Double>>? = null // [lng, lat]
)

interface OsrmApiService {
    @GET("route/v1/driving/{coordinates}")
    fun getDrivingRoute(
        @Path("coordinates", encoded = true) coordinates: String,
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "geojson"
    ): Call<OsrmRouteResponse>
}
