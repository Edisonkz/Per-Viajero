package pe.edu.utp.perviajero.util

data class CotizacionDesglose(
    val total: Double,
    val subtotalSinIgv: Double,
    val igv: Double
)

/**
 * Calculadora pura de tarifas turísticas (sin dependencias de Android) para pruebas unitarias JVM.
 */
object CotizadorCalculator {

    const val PRECIO_BASE_POR_PERSONA = 150.0
    private const val IGV_FACTOR = 1.18

    fun calcularTotal(
        cantidadPasajeros: Int,
        extraMovilidadPorPersona: Double,
        extraHotelPorPersona: Double,
        extraGuiaGrupo: Double
    ): Double {
        val pasajerosSeguros = cantidadPasajeros.coerceAtLeast(1)
        val costoPorPersona = PRECIO_BASE_POR_PERSONA + extraMovilidadPorPersona + extraHotelPorPersona
        return (costoPorPersona * pasajerosSeguros) + extraGuiaGrupo
    }

    fun calcularDesgloseConIgv(
        cantidadPasajeros: Int,
        extraMovilidadPorPersona: Double,
        extraHotelPorPersona: Double,
        extraGuiaGrupo: Double
    ): CotizacionDesglose {
        val total = calcularTotal(
            cantidadPasajeros,
            extraMovilidadPorPersona,
            extraHotelPorPersona,
            extraGuiaGrupo
        )
        val subtotal = total / IGV_FACTOR
        val igv = total - subtotal
        return CotizacionDesglose(total = total, subtotalSinIgv = subtotal, igv = igv)
    }
}
