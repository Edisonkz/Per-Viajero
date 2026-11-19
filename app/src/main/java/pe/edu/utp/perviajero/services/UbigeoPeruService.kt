package pe.edu.utp.perviajero.services

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.UbigeoCatalogo
import pe.edu.utp.perviajero.data.model.UbigeoNode
import pe.edu.utp.perviajero.network.UbigeoApiService
import pe.edu.utp.perviajero.util.AppLogger
import java.text.Normalizer

/**
 * Servicio escalable de Ubigeos del Perú (Departamentos -> Provincias -> Distritos).
 * Prioridad de carga:
 * 1) Caché en memoria (instantáneo).
 * 2) API REST pública joseluisq/ubigeos-peru (25 deptos, 196 provincias, 1,874 distritos).
 * 3) Caché local en SharedPreferences.
 * 4) Respaldo offline en assets/services/ubigeo_peru_fallback.json.
 */
class UbigeoPeruService(
    private val context: Context,
    private val prefs: PreferencesHelper,
    private val apiService: UbigeoApiService
) {
    private val gson = Gson()

    @Volatile
    private var memoryCache: UbigeoCatalogo? = null

    private data class UbigeoFallbackDto(
        val departamentos: List<UbigeoNode> = emptyList(),
        val provinciasByDepto: Map<String, List<UbigeoNode>> = emptyMap(),
        val distritosByProv: Map<String, List<UbigeoNode>> = emptyMap()
    )

    companion object {
        private const val TAG = "UbigeoPeruService"
        private const val ASSET_FALLBACK = "services/ubigeo_peru_fallback.json"
        private const val KEY_UBIGEO_DEPTOS = "key_ubigeo_deptos_v1"
        private const val KEY_UBIGEO_PROVS = "key_ubigeo_provs_v1"
        private const val KEY_UBIGEO_DISTS = "key_ubigeo_dists_v1"

        /**
         * Normaliza acentos y mayúsculas para emparejar nombres como "Áncash" con "Ancash".
         */
        fun normalizeName(raw: String): String {
            val decomposed = Normalizer.normalize(raw.trim(), Normalizer.Form.NFD)
            return decomposed
                .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
                .lowercase()
                .replace(Regex("\\s+"), " ")
        }

        fun findNodeIndexByName(nodes: List<UbigeoNode>, targetName: String?): Int {
            if (targetName.isNullOrBlank() || nodes.isEmpty()) return 0
            val target = normalizeName(targetName)
            val exact = nodes.indexOfFirst { normalizeName(it.nombre) == target }
            if (exact >= 0) return exact
            val contains = nodes.indexOfFirst {
                val n = normalizeName(it.nombre)
                n.contains(target) || target.contains(n)
            }
            return if (contains >= 0) contains else 0
        }

        fun resolveProvincias(catalogo: UbigeoCatalogo, departamentoId: String): List<UbigeoNode> {
            return catalogo.provinciasByDepto[departamentoId]
                .orEmpty()
                .sortedBy { normalizeName(it.nombre) }
        }

        fun resolveDistritos(catalogo: UbigeoCatalogo, provinciaId: String): List<UbigeoNode> {
            return catalogo.distritosByProv[provinciaId]
                .orEmpty()
                .sortedBy { normalizeName(it.nombre) }
        }
    }

    suspend fun obtenerCatalogo(): UbigeoCatalogo = withContext(Dispatchers.IO) {
        memoryCache?.let { return@withContext it }

        // 1) Intentar descarga viva desde la API oficial de Ubigeos del Perú
        try {
            val remoto = coroutineScope {
                val deptosDeferred = async { apiService.getDepartamentos() }
                val provsDeferred = async { apiService.getProvincias() }
                val distsDeferred = async { apiService.getDistritos() }

                val deptos = deptosDeferred.await().sortedBy { normalizeName(it.nombre) }
                val provs = provsDeferred.await()
                val dists = distsDeferred.await()

                if (deptos.isNotEmpty() && provs.isNotEmpty() && dists.isNotEmpty()) {
                    UbigeoCatalogo(
                        departamentos = deptos,
                        provinciasByDepto = provs,
                        distritosByProv = dists,
                        fuente = "API Ubigeo Perú"
                    )
                } else null
            }

            if (remoto != null) {
                guardarEnCache(remoto)
                memoryCache = remoto
                AppLogger.i(
                    TAG,
                    "Ubigeo cargado desde API: ${remoto.departamentos.size} deptos, ${remoto.totalProvincias()} provs, ${remoto.totalDistritos()} distritos"
                )
                return@withContext remoto
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "API Ubigeo no disponible (${e.message}), usando caché/fallback local")
        }

        // 2) Caché persistente local (descargado previamente)
        leerDeCache()?.let { cached ->
            memoryCache = cached
            return@withContext cached
        }

        // 3) Respaldo offline desde assets/services/ubigeo_peru_fallback.json
        val fallback = leerFallbackAsset()
        memoryCache = fallback
        fallback
    }

    private fun guardarEnCache(catalogo: UbigeoCatalogo) {
        try {
            prefs.putRawString(KEY_UBIGEO_DEPTOS, gson.toJson(catalogo.departamentos))
            prefs.putRawString(KEY_UBIGEO_PROVS, gson.toJson(catalogo.provinciasByDepto))
            prefs.putRawString(KEY_UBIGEO_DISTS, gson.toJson(catalogo.distritosByProv))
        } catch (_: Exception) {
        }
    }

    private fun leerDeCache(): UbigeoCatalogo? {
        val dJson = prefs.getRawString(KEY_UBIGEO_DEPTOS) ?: return null
        val pJson = prefs.getRawString(KEY_UBIGEO_PROVS) ?: return null
        val diJson = prefs.getRawString(KEY_UBIGEO_DISTS) ?: return null
        return try {
            val dType = object : TypeToken<List<UbigeoNode>>() {}.type
            val mType = object : TypeToken<Map<String, List<UbigeoNode>>>() {}.type
            val deptos: List<UbigeoNode> = gson.fromJson(dJson, dType) ?: return null
            val provs: Map<String, List<UbigeoNode>> = gson.fromJson(pJson, mType) ?: return null
            val dists: Map<String, List<UbigeoNode>> = gson.fromJson(diJson, mType) ?: return null
            if (deptos.isEmpty()) null else UbigeoCatalogo(deptos, provs, dists, "Caché Ubigeo Perú")
        } catch (_: Exception) {
            null
        }
    }

    private fun leerFallbackAsset(): UbigeoCatalogo {
        val dto = JsonAssetReader.read<UbigeoFallbackDto>(
            context,
            ASSET_FALLBACK,
            UbigeoFallbackDto::class.java
        ) ?: UbigeoFallbackDto()
        return UbigeoCatalogo(
            departamentos = dto.departamentos.sortedBy { normalizeName(it.nombre) },
            provinciasByDepto = dto.provinciasByDepto,
            distritosByProv = dto.distritosByProv,
            fuente = "Catálogo Offline Perú"
        )
    }
}
