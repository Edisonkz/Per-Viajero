package pe.edu.utp.perviajero

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.data.model.Reserva
import pe.edu.utp.perviajero.services.DestinoService
import pe.edu.utp.perviajero.util.AdminFilterHelper

class AdminFilterHelperTest {

    private val reservas = listOf(
        Reserva("PV-1", "1", "Machu Picchu", "", "22/11/2026", "Wanchaq", "06:00", 2, emptyList(), "Van", "Solo", "Guía", 420.0, "Tarjeta", "OP1", "Confirmada", "Kevin Turista", "kevin@peruviajero.pe"),
        Reserva("PV-2", "3", "Huacachina", "", "23/11/2026", "Ica", "09:00", 1, emptyList(), "Van", "Solo", "Guía", 180.0, "Yape", "OP2", "En Ruta", "Ana López", "ana@utp.edu.pe"),
        Reserva("PV-3", "1", "Machu Picchu", "", "25/11/2026", "Wanchaq", "06:00", 3, emptyList(), "Tren", "4*", "Guía", 950.0, "CIP", "OP3", "Completada", "Carlos Ruiz", "carlos@utp.edu.pe")
    )

    @Test
    fun filtrarReservas_byDestinationStateAndQuery_worksAccurately() {
        assertEquals(2, AdminFilterHelper.filtrarReservas(reservas, destinoFiltro = "Machu Picchu").size)
        assertEquals(1, AdminFilterHelper.filtrarReservas(reservas, destinoFiltro = "Machu Picchu", estadoFiltro = "Completada").size)
        assertEquals("PV-2", AdminFilterHelper.filtrarReservas(reservas, query = "ana").first().codigoReserva)
    }

    @Test
    fun combinarCatalogo_mergesCustomDestinationsAndRespectsHidden() {
        val remotos = listOf(
            Destino("1", "Machu Picchu", "Cusco", "Arqueología", "Desc", 4.9, ""),
            Destino("2", "Colca", "Arequipa", "Naturaleza", "Desc", 4.8, "")
        )
        val personalizados = listOf(
            Destino("admin_1", "Laguna Humantay", "Cusco", "Naturaleza", "Nueva", 4.9, "", provincia = "Anta")
        )
        val publico = DestinoService.combinarCatalogo(remotos, personalizados, ocultosIds = setOf("2"), incluirOcultos = false)
        assertEquals(2, publico.size)
        assertEquals("admin_1", publico[0].id)

        val admin = DestinoService.combinarCatalogo(remotos, personalizados, ocultosIds = setOf("2"), incluirOcultos = true)
        assertEquals(3, admin.size)
    }
}
