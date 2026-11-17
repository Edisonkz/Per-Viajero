package pe.edu.utp.perviajero

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.utp.perviajero.util.CotizadorCalculator

class CotizadorCalculatorTest {

    @Test
    fun calcularTotal_multipliesPerPersonAddonsAndAddsGroupGuide() {
        // (150 + 40 + 90) * 2 + 80 = 280 * 2 + 80 = 640
        val total = CotizadorCalculator.calcularTotal(
            cantidadPasajeros = 2,
            extraMovilidadPorPersona = 40.0,
            extraHotelPorPersona = 90.0,
            extraGuiaGrupo = 80.0
        )
        assertEquals(640.0, total, 0.001)
    }

    @Test
    fun calcularTotal_guardsAgainstZeroOrNegativePassengers() {
        val total = CotizadorCalculator.calcularTotal(
            cantidadPasajeros = 0,
            extraMovilidadPorPersona = 0.0,
            extraHotelPorPersona = 0.0,
            extraGuiaGrupo = 0.0
        )
        assertEquals(150.0, total, 0.001)
    }

    @Test
    fun calcularDesgloseConIgv_subtotalPlusIgvEqualsTotal() {
        val desglose = CotizadorCalculator.calcularDesgloseConIgv(
            cantidadPasajeros = 3,
            extraMovilidadPorPersona = 40.0,
            extraHotelPorPersona = 0.0,
            extraGuiaGrupo = 45.0
        )
        assertEquals(desglose.total, desglose.subtotalSinIgv + desglose.igv, 0.001)
        assertEquals(615.0, desglose.total, 0.001)
    }
}
