package pe.edu.utp.perviajero.data.repository

import pe.edu.utp.perviajero.data.model.ComentarioPublicacion
import pe.edu.utp.perviajero.data.model.PublicacionViajero
import pe.edu.utp.perviajero.services.ComunidadService

/**
 * Implementación local de ComunidadRepository respaldada por ComunidadService (assets/services/comunidad.json).
 */
class LocalComunidadRepository(
    private val comunidadService: ComunidadService
) : ComunidadRepository {

    override fun obtenerPublicaciones(): List<PublicacionViajero> = comunidadService.getPublicaciones()

    override fun filtrarPublicaciones(region: String?, destinoId: String?): List<PublicacionViajero> {
        return filterPosts(comunidadService.getPublicaciones(), region, destinoId)
    }

    override fun agregarPublicacion(publicacion: PublicacionViajero) {
        val lista = comunidadService.getPublicaciones().toMutableList()
        lista.add(0, publicacion)
        comunidadService.guardarLista(lista)
    }

    override fun toggleLike(publicacionId: String): PublicacionViajero? {
        val lista = comunidadService.getPublicaciones().toMutableList()
        val index = lista.indexOfFirst { it.id == publicacionId }
        if (index == -1) return null

        val actual = lista[index]
        val nuevoLike = !actual.dioLike
        val actualizado = actual.copy(
            dioLike = nuevoLike,
            likes = (actual.likes + if (nuevoLike) 1 else -1).coerceAtLeast(0)
        )
        lista[index] = actualizado
        comunidadService.guardarLista(lista)
        return actualizado
    }

    override fun agregarComentario(publicacionId: String, comentario: ComentarioPublicacion): PublicacionViajero? {
        val lista = comunidadService.getPublicaciones().toMutableList()
        val index = lista.indexOfFirst { it.id == publicacionId }
        if (index == -1) return null

        val actual = lista[index]
        val actualizado = actual.copy(comentarios = actual.comentarios + comentario)
        lista[index] = actualizado
        comunidadService.guardarLista(lista)
        return actualizado
    }

    override fun eliminarPublicacion(publicacionId: String) {
        val filtradas = comunidadService.getPublicaciones().filterNot { it.id == publicacionId }
        comunidadService.guardarLista(filtradas)
    }

    override fun contarPorDestino(destinoId: String): Int {
        return comunidadService.getPublicaciones().count { it.destinoId == destinoId }
    }

    companion object {
        fun filterPosts(
            posts: List<PublicacionViajero>,
            region: String? = null,
            destinoId: String? = null
        ): List<PublicacionViajero> {
            return posts.filter { post ->
                val matchesRegion = region.isNullOrBlank() ||
                    region.equals("Todas", ignoreCase = true) ||
                    post.region.equals(region, ignoreCase = true)
                val matchesDestino = destinoId.isNullOrBlank() ||
                    destinoId.equals("ALL", ignoreCase = true) ||
                    post.destinoId == destinoId
                matchesRegion && matchesDestino
            }
        }
    }
}
