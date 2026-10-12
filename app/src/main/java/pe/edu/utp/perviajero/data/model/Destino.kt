package pe.edu.utp.perviajero.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// REQ-05 / REQ-06: Modelo de datos para destinos turísticos obtenidos desde la API REST
data class Destino(
    @SerializedName("id")
    val id: String,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("departamento")
    val departamento: String,

    @SerializedName("categoria")
    val categoria: String,

    @SerializedName("descripcion")
    val descripcion: String,

    @SerializedName("calificacion")
    val calificacion: Double,

    @SerializedName("imageUrl")
    val imageUrl: String,

    @SerializedName("latitud")
    val latitud: Double? = null,

    @SerializedName("longitud")
    val longitud: Double? = null
) : Serializable

