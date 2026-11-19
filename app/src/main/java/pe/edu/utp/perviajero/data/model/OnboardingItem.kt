package pe.edu.utp.perviajero.data.model

/**
 * Modelo para las diapositivas interactivas de la Bienvenida Épica (Onboarding).
 */
data class OnboardingItem(
    val badge: String,
    val titulo: String,
    val subtitulo: String,
    val descripcion: String,
    val iconoResId: Int,
    val highlights: List<String> = emptyList(),
    val accentColorHex: String = "#FFB300",
    val gradientStartHex: String = "#007A42",
    val gradientEndHex: String = "#002D16",
    val tintIconWhite: Boolean = false
)
