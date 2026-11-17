package pe.edu.utp.perviajero.data.model

import java.io.Serializable

/**
 * Comentario realizado por un turista dentro de una publicación de la Comunidad.
 */
data class ComentarioPublicacion(
    val id: String,
    val autorNombre: String,
    val texto: String,
    val fecha: String
) : Serializable

/**
 * Publicación de experiencia turística compartida por un viajero según región y destino.
 * Modelo desacoplado de la capa de almacenamiento para facilitar futura migración a Room o Firestore.
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
    val comentarios: List<ComentarioPublicacion> = emptyList()
) : Serializable
