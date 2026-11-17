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

    /**
     * Resuelve la provincia oficial del destino turístico en Perú usando el campo explícito
     * si viene de API o el catálogo de atractivos/departamentos peruanos.
     */
    fun resolveProvincia(destino: Destino): String {
        val explicit = destino.provincia?.trim()
        if (!explicit.isNullOrEmpty()) return explicit

        val nombreNorm = normalize(destino.nombre)
        return when {
            nombreNorm.contains("machu picchu") -> "Urubamba"
            nombreNorm.contains("vinicunca") || nombreNorm.contains("siete colores") -> "Canchis"
            nombreNorm.contains("colca") -> "Caylloma"
            nombreNorm.contains("huacachina") -> "Ica"
            nombreNorm.contains("nazca") || nombreNorm.contains("nasca") -> "Nazca"
            nombreNorm.contains("paracas") -> "Pisco"
            nombreNorm.contains("titicaca") || nombreNorm.contains("uros") -> "Puno"
            nombreNorm.contains("kuelap") -> "Luya"
            nombreNorm.contains("laguna 69") || nombreNorm.contains("huascaran") -> "Yungay"
            nombreNorm.contains("mancora") -> "Talara"
            nombreNorm.contains("iquitos") || nombreNorm.contains("amazonas") -> "Maynas"
            nombreNorm.contains("chan chan") -> "Trujillo"
            else -> destino.departamento.ifBlank { "Perú" }
        }
    }

    fun extractDepartamentos(destinos: List<Destino>): List<String> {
        return destinos.map { it.departamento.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    fun extractProvincias(destinos: List<Destino>, departamento: String): List<String> {
        val scoped = if (departamento.isBlank() || departamento.equals("Todos", ignoreCase = true)) {
            destinos
        } else {
            destinos.filter { it.departamento.equals(departamento, ignoreCase = true) }
        }
        return scoped.map { resolveProvincia(it) }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    private fun normalize(value: String): String {
        val decomposed = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
        return decomposed
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase(Locale.US)
    }
}
