package pe.edu.utp.perviajero.data.repository

import pe.edu.utp.perviajero.data.local.PreferencesHelper

class LocalFavoritoRepository(
    private val prefs: PreferencesHelper
) : FavoritoRepository {
    override fun isFavorito(destinoId: String): Boolean = prefs.isFavorito(destinoId)
    override fun toggleFavorito(destinoId: String): Boolean = prefs.toggleFavorito(destinoId)
    override fun getFavoritosIds(): Set<String> = prefs.getFavoritosIds()
}
