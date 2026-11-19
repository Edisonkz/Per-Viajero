package pe.edu.utp.perviajero.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Modelo de aviso o notificación turística persistida en la Bandeja de Avisos.
 * Tipos admitidos: "alerta", "reserva", "itinerario", "aviso".
 */
data class AvisoTuristico(
    @SerializedName("id")
    val id: Int,
    @SerializedName("tipo")
    val tipo: String,
    @SerializedName("titulo")
    val titulo: String,
    @SerializedName("cuerpo")
    val cuerpo: String,
    @SerializedName("region")
    val region: String = "",
    @SerializedName("url")
    val url: String = "",
    @SerializedName("hora")
    val hora: Long = System.currentTimeMillis(),
    @SerializedName("leido")
    val leido: Boolean = false
) : Serializable
