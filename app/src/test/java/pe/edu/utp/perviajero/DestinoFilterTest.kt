package pe.edu.utp.perviajero

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.utp.perviajero.adapter.DestinoAdapter
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.util.RegionPeruHelper

class DestinoFilterTest {

    private val sample = listOf(
        Destino("1", "Machu Picchu", "Cusco", "Arqueología", "Ciudadela inca", 4.9, ""),
        Destino("2", "Cañón del Colca", "Arequipa", "Naturaleza", "Cóndor andino", 4.8, ""),
        Destino("3", "Oasis de la Huacachina", "Ica", "Aventura", "Dunas y tubulares", 4.7, ""),
        Destino("4", "Fortaleza de Kuélap", "Amazonas", "Arqueología", "Cultura Chachapoyas", 4.8, "")
    )

    @Test
    fun filterList_emptyQuery_returnsAll() {
        val result = DestinoAdapter.filterList(sample, "   ")
        assertEquals(4, result.size)
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

    @Test
    fun filterByNaturalRegion_mapsSierraCostaSelvaAccurately() {
        assertEquals(2, RegionPeruHelper.filterByNaturalRegion(sample, "Sierra").size)
        assertEquals(1, RegionPeruHelper.filterByNaturalRegion(sample, "Costa").size)
        assertEquals(1, RegionPeruHelper.filterByNaturalRegion(sample, "Selva").size)
        assertEquals("Costa", RegionPeruHelper.resolveNaturalRegion("La Libertad"))
        assertEquals("Sierra", RegionPeruHelper.resolveNaturalRegion("Áncash"))
    }
}
