package pe.edu.utp.perviajero.util

import pe.edu.utp.perviajero.data.model.Destino
import java.text.Normalizer
import java.util.Locale

/**
 * Clasifica departamentos del Perú en su región natural (Sierra, Costa, Selva)
 * para los filtros geográficos de Favoritos y análisis de catálogo.
 */
object RegionPeruHelper {

    val REGIONES_PRINCIPALES = listOf("Todas", "Sierra", "Costa", "Selva")

    private val COSTA = setOf(
        "lima", "callao", "ica", "piura", "la libertad",
        "lambayeque", "tumbes", "tacna", "moquegua"
    )

    private val SELVA = setOf(
        "amazonas", "loreto", "san martin", "ucayali", "madre de dios"
    )

    private val SIERRA = setOf(
        "cusco", "arequipa", "puno", "ancash", "ayacucho",
        "junin", "cajamarca", "huanuco", "apurimac", "huancavelica", "pasco"
    )

    fun resolveNaturalRegion(departamento: String): String {
        val norm = normalize(departamento)
        return when {
            COSTA.contains(norm) -> "Costa"
            SELVA.contains(norm) -> "Selva"
            SIERRA.contains(norm) -> "Sierra"
            else -> "Sierra"
        }
    }

    fun filterByNaturalRegion(destinos: List<Destino>, region: String): List<Destino> {
        if (region.isBlank() || region.equals("Todas", ignoreCase = true)) return destinos
        return destinos.filter { resolveNaturalRegion(it.departamento).equals(region, ignoreCase = true) }
    }

    private fun normalize(value: String): String {
        val decomposed = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
        return decomposed
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase(Locale.US)
    }
}
