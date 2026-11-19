package pe.edu.utp.perviajero.util

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import pe.edu.utp.perviajero.data.model.AvisoTuristico
import java.util.Locale

/**
 * Parser puro (JVM-testable) para interpretar payloads JSON de notificaciones Push.
 * Exige campos obligatorios (titulo y cuerpo), aplica valores seguros por defecto a los opcionales
 * y valida URLs entrantes antes de permitir ACTION_VIEW.
 */
object AvisoPayloadParser {

    val TIPOS_VALIDOS = setOf("alerta", "reserva", "itinerario", "aviso")

    fun parsePayload(
        rawJson: String,
        idGenerator: () -> Int = { ((System.currentTimeMillis() % Int.MAX_VALUE).toInt()).coerceAtLeast(100) },
        nowMillis: Long = System.currentTimeMillis()
    ): AvisoTuristico? {
        if (rawJson.isBlank()) return null
        return try {
            val element = JsonParser.parseString(rawJson)
            if (!element.isJsonObject) return null
            val obj = element.asJsonObject

            val titulo = obj.getRequiredNonBlank("titulo") ?: return null
            val cuerpo = obj.getRequiredNonBlank("cuerpo") ?: return null
            val rawTipo = obj.getOptionalString("tipo", "aviso").lowercase(Locale.US)
            val tipo = if (rawTipo in TIPOS_VALIDOS) rawTipo else "aviso"
            val region = obj.getOptionalString("region", "")
            val rawUrl = obj.getOptionalString("url", "")
            val safeUrl = if (isSafeUrl(rawUrl)) rawUrl.trim() else ""

            AvisoTuristico(
                id = idGenerator(),
                tipo = tipo,
                titulo = titulo,
                cuerpo = cuerpo,
                region = region,
                url = safeUrl,
                hora = nowMillis,
                leido = false
            )
        } catch (_: Exception) {
            null
        }
    }

    fun isSafeUrl(url: String?): Boolean {
        val cleaned = url?.trim().orEmpty()
        if (cleaned.isEmpty()) return false
        val lower = cleaned.lowercase(Locale.US)
        return (lower.startsWith("https://") || lower.startsWith("http://")) &&
            cleaned.length > 10 &&
            !cleaned.contains(" ")
    }

    private fun JsonObject.getRequiredNonBlank(key: String): String? {
        val el = get(key) ?: return null
        if (!el.isJsonPrimitive || !el.asJsonPrimitive.isString) return null
        return el.asString.trim().takeIf { it.isNotEmpty() }
    }

    private fun JsonObject.getOptionalString(key: String, defaultValue: String): String {
        val el = get(key) ?: return defaultValue
        if (!el.isJsonPrimitive || !el.asJsonPrimitive.isString) return defaultValue
        return el.asString.trim().ifEmpty { defaultValue }
    }
}
