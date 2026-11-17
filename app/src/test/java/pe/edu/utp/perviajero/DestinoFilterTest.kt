package pe.edu.utp.perviajero

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.utp.perviajero.adapter.DestinoAdapter
import pe.edu.utp.perviajero.data.model.Destino

class DestinoFilterTest {

    private val sample = listOf(
        Destino("1", "Machu Picchu", "Cusco", "Arqueología", "Ciudadela inca", 4.9, ""),
        Destino("2", "Cañón del Colca", "Arequipa", "Naturaleza", "Cóndor andino", 4.8, ""),
        Destino("3", "Oasis de la Huacachina", "Ica", "Aventura", "Dunas y tubulares", 4.7, "")
    )

    @Test
    fun filterList_emptyQuery_returnsAll() {
        val result = DestinoAdapter.filterList(sample, "   ")
        assertEquals(3, result.size)
    }

    @Test
    fun filterList_byDepartmentCaseInsensitive_returnsMatches() {
        val result = DestinoAdapter.filterList(sample, "cUsCo")
        assertEquals(1, result.size)
        assertEquals("1", result.first().id)
    }

    @Test
    fun filterList_byCategory_returnsMatches() {
        val result = DestinoAdapter.filterList(sample, "aventura")
        assertEquals(1, result.size)
        assertEquals("3", result.first().id)
    }
}
