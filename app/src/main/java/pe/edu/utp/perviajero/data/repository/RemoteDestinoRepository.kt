package pe.edu.utp.perviajero.data.repository

import pe.edu.utp.perviajero.data.common.AppResult
import pe.edu.utp.perviajero.data.common.ErrorClassifier
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.network.TourismApiService
import pe.edu.utp.perviajero.services.DestinoService
import pe.edu.utp.perviajero.util.AppLogger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Implementación de DestinoRepository que combina API REST pública (Retrofit) con caché local
 * y los destinos gestionados por el Administrador mediante DestinoService.
 */
class RemoteDestinoRepository(
    private val apiService: TourismApiService,
    private val prefs: PreferencesHelper,
    private val destinoService: DestinoService
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
                    val combinados = DestinoService.combinarCatalogo(
                        remotos = body,
                        personalizados = destinoService.getPersonalizados(),
                        ocultosIds = destinoService.getOcultosIds(),
                        incluirOcultos = false
                    )
                    AppLogger.i(TAG, "Catálogo REST actualizado (${combinados.size} destinos visibles)")
                    onResult(AppResult.Success(combinados, fromCache = false))
                } else {
                    val (type, msg) = ErrorClassifier.classifyHttpCode(response.code())
                    AppLogger.w(TAG, "Error HTTP al obtener destinos: $msg")
                    val cached = obtenerDestinosEnCache()
                    onResult(AppResult.Error(type, msg, cached.takeIf { it.isNotEmpty() }))
                }
            }

            override fun onFailure(call: Call<List<Destino>>, t: Throwable) {
                val (type, msg) = ErrorClassifier.classifyThrowable(t)
                AppLogger.e(TAG, "Fallo de red obteniendo destinos: $msg", t)
                val cached = obtenerDestinosEnCache()
                onResult(AppResult.Error(type, msg, cached.takeIf { it.isNotEmpty() }))
            }
        })
    }

    override fun obtenerDestinosEnCache(): List<Destino> {
        return DestinoService.combinarCatalogo(
            remotos = prefs.obtenerDestinosEnCache(),
            personalizados = destinoService.getPersonalizados(),
            ocultosIds = destinoService.getOcultosIds(),
            incluirOcultos = false
        )
    }

    override fun obtenerCatalogoCompletoAdmin(): List<Destino> {
        return DestinoService.combinarCatalogo(
            remotos = prefs.obtenerDestinosEnCache(),
            personalizados = destinoService.getPersonalizados(),
            ocultosIds = destinoService.getOcultosIds(),
            incluirOcultos = true
        )
    }

    override fun guardarDestinoAdmin(destino: Destino) = destinoService.guardarDestino(destino)

    override fun toggleOcultarDestinoAdmin(destinoId: String): Boolean = destinoService.toggleOculto(destinoId)

    override fun esDestinoOculto(destinoId: String): Boolean = destinoService.estaOculto(destinoId)
}
