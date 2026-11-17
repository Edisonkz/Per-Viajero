package pe.edu.utp.perviajero.services

import android.content.Context
import com.google.gson.Gson
import pe.edu.utp.perviajero.util.AppLogger
import java.lang.reflect.Type

/**
 * Lector genérico de archivos JSON en assets/services/ para simular respuestas de API REST locales.
 */
object JsonAssetReader {

    private const val TAG = "JsonAssetReader"
    private val gson = Gson()

    fun <T> read(context: Context, assetPath: String, type: Type): T? {
        return try {
            val json = context.applicationContext.assets.open(assetPath)
                .bufferedReader()
                .use { it.readText() }
            gson.fromJson<T>(json, type)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error leyendo asset JSON '$assetPath'", e)
            null
        }
    }
}
