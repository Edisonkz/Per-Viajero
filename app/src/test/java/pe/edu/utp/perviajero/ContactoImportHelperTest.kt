package pe.edu.utp.perviajero

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.utp.perviajero.data.model.Pasajero
import pe.edu.utp.perviajero.util.ContactoAgenda
import pe.edu.utp.perviajero.util.ContactoImportHelper

class ContactoImportHelperTest {

    @Test
    fun deduplicar_removesDuplicatesAndSortsAlphabetically() {
        val raw = listOf(
            ContactoAgenda("María López", "987 654 321"),
            ContactoAgenda("carlos quispe", "912345678"),
            ContactoAgenda("María López", "987654321"),
            ContactoAgenda("   ", "999999999")
        )
        val result = ContactoImportHelper.deduplicar(raw)
        assertEquals(2, result.size)
        assertEquals("carlos quispe", result[0].nombre)
        assertEquals("María López", result[1].nombre)
    }

    @Test
    fun agregarAlManifiesto_replacesPlaceholdersAndRespectsMax() {
        val iniciales = listOf(
            Pasajero("Kevin Turista", "DNI", "72481920", 24),
            Pasajero("Pasajero 2", "DNI", "00000002", 25)
        )
        val seleccionados = listOf(
            ContactoAgenda("Kevin Turista", "999111222"),
            ContactoAgenda("Ana Torres", "987654321"),
            ContactoAgenda("Luis Vega", "911222333")
        )
        val result = ContactoImportHelper.agregarAlManifiesto(iniciales, seleccionados, maxPasajeros = 3)
        assertEquals(3, result.size)
        assertEquals("Kevin Turista", result[0].nombresApellidos)
        assertEquals("Ana Torres", result[1].nombresApellidos)
        assertEquals("Luis Vega", result[2].nombresApellidos)
    }
}
