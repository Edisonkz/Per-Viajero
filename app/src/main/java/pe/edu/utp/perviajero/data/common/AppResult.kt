package pe.edu.utp.perviajero.data.common

import com.google.gson.JsonSyntaxException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Clasificación tipada de errores para ofrecer mensajes claros al usuario y facilitar diagnóstico.
 */
enum class NetworkErrorType {
    NO_INTERNET,
    TIMEOUT,
    SERVER_ERROR,
    PARSE_ERROR,
    UNKNOWN
}

/**
 * Envoltorio funcional para resultados de repositorios (éxito o fallo tipado con respaldo opcional en caché).
 */
sealed class AppResult<out T> {
    data class Success<T>(
        val data: T,
        val fromCache: Boolean = false
    ) : AppResult<T>()

    data class Error<T>(
        val errorType: NetworkErrorType,
        val message: String,
        val cachedData: T? = null
    ) : AppResult<T>()
}

/**
 * Traduce excepciones técnicas y códigos HTTP en tipos de error comprensibles y mensajes amigables.
 */
object ErrorClassifier {

    fun classifyThrowable(t: Throwable): Pair<NetworkErrorType, String> {
        return when (t) {
            is SocketTimeoutException -> NetworkErrorType.TIMEOUT to
                "El servidor tardó demasiado en responder. Verifica tu señal e inténtalo de nuevo."
            is UnknownHostException -> NetworkErrorType.NO_INTERNET to
                "Sin conexión a internet. Verifica tu Wi-Fi o datos móviles."
            is JsonSyntaxException -> NetworkErrorType.PARSE_ERROR to
                "Los datos recibidos tienen un formato inesperado."
            is IOException -> NetworkErrorType.NO_INTERNET to
                (t.message?.takeIf { it.isNotBlank() } ?: "Error de red al conectar con el servidor.")
            else -> NetworkErrorType.UNKNOWN to
                (t.message ?: "Ocurrió un error inesperado.")
        }
    }

    fun classifyHttpCode(code: Int): Pair<NetworkErrorType, String> {
        return when (code) {
            in 500..599 -> NetworkErrorType.SERVER_ERROR to
                "El servicio turístico está temporalmente no disponible (HTTP $code)."
            in 400..499 -> NetworkErrorType.SERVER_ERROR to
                "No se pudo procesar la solicitud en el servidor (HTTP $code)."
            else -> NetworkErrorType.UNKNOWN to
                "Respuesta inesperada del servidor (HTTP $code)."
        }
    }
}
