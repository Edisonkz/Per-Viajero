package pe.edu.utp.perviajero

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.utp.perviajero.data.model.UbigeoCatalogo
import pe.edu.utp.perviajero.data.model.UbigeoNode
import pe.edu.utp.perviajero.services.UbigeoPeruService

class UbigeoPeruServiceTest {

    private val sampleCatalog = UbigeoCatalogo(
        departamentos = listOf(
            UbigeoNode(id = "2625", nombre = "Ancash"),
            UbigeoNode(id = "3292", nombre = "Cusco")
        ),
        provinciasByDepto = mapOf(
            "3292" to listOf(
                UbigeoNode(id = "3305", nombre = "Urubamba", idPadre = "3292"),
                UbigeoNode(id = "3293", nombre = "Cusco", idPadre = "3292")
            )
        ),
        distritosByProv = mapOf(
            "3305" to listOf(
                UbigeoNode(id = "3310", nombre = "Ollantaytambo", idPadre = "3305"),
                UbigeoNode(id = "3308", nombre = "Machupicchu", idPadre = "3305")
            )
        ),
        fuente = "Test"
    )

    @Test
    fun normalizeAndFindMatchesAccentedDepartmentNames() {
        val idx = UbigeoPeruService.findNodeIndexByName(sampleCatalog.departamentos, "Áncash")
        assertEquals(0, idx)
    }

    @Test
    fun resolveProvinciasAndDistritosReturnsSortedCascade() {
        val provs = UbigeoPeruService.resolveProvincias(sampleCatalog, "3292")
        assertEquals(listOf("Cusco", "Urubamba"), provs.map { it.nombre })

        val dists = UbigeoPeruService.resolveDistritos(sampleCatalog, "3305")
        assertEquals(listOf("Machupicchu", "Ollantaytambo"), dists.map { it.nombre })
    }
}
