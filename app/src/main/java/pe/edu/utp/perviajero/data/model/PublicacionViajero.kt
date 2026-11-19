package pe.edu.utp.perviajero.data.model

import java.io.Serializable

/**
 * Comentario realizado por un turista o administrador dentro de una publicación de la Comunidad.
 */
data class ComentarioPublicacion(
    val id: String,
    val autorNombre: String,
    val texto: String,
    val fecha: String,
    val autorRol: String? = "TURISTA"
) : Serializable {
    fun rolNormalizado(): String = autorRol?.takeIf { it.isNotBlank() } ?: "TURISTA"
}

/**
 * Publicación de experiencia turística compartida por un viajero según región y destino.
 * Soporta múltiples fotografías (fotosUris) manteniendo compatibilidad con fotoUri.
 */
data class PublicacionViajero(
    val id: String,
    val autorNombre: String,
    val autorEmail: String,
    val autorFotoUri: String = "",
    val region: String,
    val destinoId: String,
    val destinoNombre: String,
    val fotoUri: String,
    val calificacion: Float,
    val comentario: String,
    val fechaPublicacion: String,
    val likes: Int = 0,
    val dioLike: Boolean = false,
    val comentarios: List<ComentarioPublicacion> = emptyList(),
    val fotosUris: List<String>? = null
) : Serializable {

    fun obtenerFotos(): List<String> {
        val extras = fotosUris.orEmpty().filter { it.isNotBlank() }
        return if (extras.isNotEmpty()) {
            extras
        } else {
            listOfNotNull(fotoUri.takeIf { it.isNotBlank() })
        }
    }

    fun fotoPrincipal(): String = obtenerFotos().firstOrNull().orEmpty()
}
