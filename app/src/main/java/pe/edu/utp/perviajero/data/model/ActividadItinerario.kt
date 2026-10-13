package pe.edu.utp.perviajero.data.model

import java.io.Serializable

// Modelo para el planificador interactivo de actividades e itinerario del turista con soporte de calendario
data class ActividadItinerario(
    val id: String,
    val dia: String, // "Día 1", "Día 2", "Día 3", "Día 4", "Día 5", "Día 6", "Día 7"
    val fecha: String = "", // Formato dd/MM/yyyy para vincular al calendario interactivo
    val horario: String, // "Mañana (08:00 AM)", "Tarde (02:00 PM)", "Noche (07:00 PM)"
    val lugar: String, // Nombre del atractivo o lugar sugerido
    val descripcion: String, // Notas personales escritas por el turista
    var completada: Boolean = false
) : Serializable
