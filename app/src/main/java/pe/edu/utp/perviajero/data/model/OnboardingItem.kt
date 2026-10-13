package pe.edu.utp.perviajero.data.model

// Modelo para las diapositivas interactivas de bienvenida / Onboarding
data class OnboardingItem(
    val badge: String,
    val titulo: String,
    val subtitulo: String,
    val descripcion: String,
    val iconoResId: Int
)

