package pe.edu.utp.perviajero.data.repository

import pe.edu.utp.perviajero.data.model.ComentarioPublicacion
import pe.edu.utp.perviajero.data.model.PublicacionViajero

/**
 * Contrato para el módulo social Comunidad de Viajeros.
 * Preparado para migrar a una base de datos remota (Firestore/API REST) o local (Room) sin tocar las vistas.
 */
interface ComunidadRepository {
    fun obtenerPublicaciones(): List<PublicacionViajero>
    fun filtrarPublicaciones(region: String? = null, destinoId: String? = null): List<PublicacionViajero>
    fun agregarPublicacion(publicacion: PublicacionViajero)
    fun toggleLike(publicacionId: String): PublicacionViajero?
    fun agregarComentario(publicacionId: String, comentario: ComentarioPublicacion): PublicacionViajero?
    fun eliminarPublicacion(publicacionId: String)
    fun contarPorDestino(destinoId: String): Int
}
