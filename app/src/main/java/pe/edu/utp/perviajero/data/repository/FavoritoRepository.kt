package pe.edu.utp.perviajero.data.repository

/**
 * Contrato de persistencia para destinos marcados como favoritos.
 */
interface FavoritoRepository {
    fun isFavorito(destinoId: String): Boolean
    fun toggleFavorito(destinoId: String): Boolean
    fun getFavoritosIds(): Set<String>
}
