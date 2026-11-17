package pe.edu.utp.perviajero.data.repository

import pe.edu.utp.perviajero.data.common.AppResult
import pe.edu.utp.perviajero.data.common.ErrorClassifier
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.network.TourismApiService
import pe.edu.utp.perviajero.util.AppLogger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Implementación de DestinoRepository que combina API REST pública (Retrofit) con caché local.
 */
class RemoteDestinoRepository(
    private val apiService: TourismApiService,
    private val prefs: PreferencesHelper
) : DestinoRepository {

    companion object {
        private const val TAG = "DestinoRepo"
    }

    override fun obtenerDestinos(
        forzarRed: Boolean,
        onResult: (AppResult<List<Destino>>) -> Unit
    ) {
        apiService.getDestinos().enqueue(object : Callback<List<Destino>> {
            override fun onResponse(call: Call<List<Destino>>, response: Response<List<Destino>>) {
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    prefs.guardarDestinosEnCache(body)
                    AppLogger.i(TAG, "Catálogo REST actualizado (${body.size} destinos)")
                    onResult(AppResult.Success(body, fromCache = false))
                } else {
                    val (type, msg) = ErrorClassifier.classifyHttpCode(response.code())
                    AppLogger.w(TAG, "Error HTTP al obtener destinos: $msg")
                    val cached = prefs.obtenerDestinosEnCache()
                    onResult(AppResult.Error(type, msg, cached.takeIf { it.isNotEmpty() }))
                }
            }

            override fun onFailure(call: Call<List<Destino>>, t: Throwable) {
                val (type, msg) = ErrorClassifier.classifyThrowable(t)
                AppLogger.e(TAG, "Fallo de red obteniendo destinos: $msg", t)
                val cached = prefs.obtenerDestinosEnCache()
                onResult(AppResult.Error(type, msg, cached.takeIf { it.isNotEmpty() }))
            }
        })
    }

    override fun obtenerDestinosEnCache(): List<Destino> = prefs.obtenerDestinosEnCache()
}
