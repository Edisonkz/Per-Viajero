package pe.edu.utp.perviajero.data.repository

import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.ComentarioPublicacion
import pe.edu.utp.perviajero.data.model.PublicacionViajero

/**
 * Implementación local de ComunidadRepository respaldada por PreferencesHelper.
 * Reemplazable por Firestore/Room sin modificar ComunidadActivity ni PublicarExperienciaActivity.
 */
class LocalComunidadRepository(
    private val prefs: PreferencesHelper
) : ComunidadRepository {

    override fun obtenerPublicaciones(): List<PublicacionViajero> = prefs.getPublicaciones()

    override fun filtrarPublicaciones(region: String?, destinoId: String?): List<PublicacionViajero> {
        return filterPosts(prefs.getPublicaciones(), region, destinoId)
    }

    override fun agregarPublicacion(publicacion: PublicacionViajero) {
        val lista = prefs.getPublicaciones().toMutableList()
        lista.add(0, publicacion)
        prefs.guardarListaPublicaciones(lista)
    }

    override fun toggleLike(publicacionId: String): PublicacionViajero? {
        val lista = prefs.getPublicaciones().toMutableList()
        val index = lista.indexOfFirst { it.id == publicacionId }
        if (index == -1) return null

        val actual = lista[index]
        val nuevoLike = !actual.dioLike
        val actualizado = actual.copy(
            dioLike = nuevoLike,
            likes = (actual.likes + if (nuevoLike) 1 else -1).coerceAtLeast(0)
        )
        lista[index] = actualizado
        prefs.guardarListaPublicaciones(lista)
        return actualizado
    }

    override fun agregarComentario(publicacionId: String, comentario: ComentarioPublicacion): PublicacionViajero? {
        val lista = prefs.getPublicaciones().toMutableList()
        val index = lista.indexOfFirst { it.id == publicacionId }
        if (index == -1) return null

        val actual = lista[index]
        val actualizado = actual.copy(comentarios = actual.comentarios + comentario)
        lista[index] = actualizado
        prefs.guardarListaPublicaciones(lista)
        return actualizado
    }

    override fun contarPorDestino(destinoId: String): Int {
        return prefs.getPublicaciones().count { it.destinoId == destinoId }
    }

    companion object {
        /**
         * Lógica pura de filtrado reutilizable y testeable unitariamente en JVM.
         */
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
