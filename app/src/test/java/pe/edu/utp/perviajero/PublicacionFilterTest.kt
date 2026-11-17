package pe.edu.utp.perviajero

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.utp.perviajero.data.model.PublicacionViajero
import pe.edu.utp.perviajero.data.repository.LocalComunidadRepository

class PublicacionFilterTest {

    private val posts = listOf(
        PublicacionViajero("p1", "Ana", "a@p.pe", "", "Cusco", "1", "Machu Picchu", "", 5f, "Excelente", "10/11/2026"),
        PublicacionViajero("p2", "Luis", "l@p.pe", "", "Cusco", "5", "Vinicunca", "", 4.5f, "Frío pero hermoso", "11/11/2026"),
        PublicacionViajero("p3", "Sofía", "s@p.pe", "", "Ica", "3", "Huacachina", "", 4.8f, "Gran atardecer", "12/11/2026")
    )

    @Test
    fun filterPosts_allRegionsAndAllDestinations_returnsEverything() {
        val result = LocalComunidadRepository.filterPosts(posts, "Todas", "ALL")
        assertEquals(3, result.size)
    }

    @Test
    fun filterPosts_byRegionOnly_returnsRegionPosts() {
        val result = LocalComunidadRepository.filterPosts(posts, "Cusco", "ALL")
        assertEquals(2, result.size)
    }

    @Test
    fun filterPosts_byRegionAndDestino_returnsExactMatch() {
        val result = LocalComunidadRepository.filterPosts(posts, "Cusco", "5")
        assertEquals(1, result.size)
        assertEquals("p2", result.first().id)
    }
}
