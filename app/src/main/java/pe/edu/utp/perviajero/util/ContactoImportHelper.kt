package pe.edu.utp.perviajero.util

import pe.edu.utp.perviajero.data.model.Pasajero
import java.util.Locale

data class ContactoAgenda(
    val nombre: String,
    val telefono: String
)

/**
 * Funciones puras para deduplicar contactos importados desde ContentResolver
 * e integrarlos limpiamente en el manifiesto de pasajeros (normativa SUTRAN).
 */
object ContactoImportHelper {

    fun deduplicar(contactos: List<ContactoAgenda>): List<ContactoAgenda> {
        val vistos = mutableSetOf<String>()
        val resultado = mutableListOf<ContactoAgenda>()
        for (c in contactos) {
            val nombreLimpio = c.nombre.trim().ifBlank { continue }
            val telLimpio = c.telefono.trim()
            val clave = "${nombreLimpio.lowercase(Locale.US)}|${telLimpio.replace("\\s+".toRegex(), "")}"
            if (vistos.add(clave)) {
                resultado.add(ContactoAgenda(nombreLimpio, telLimpio))
            }
        }
        return resultado.sortedBy { it.nombre.lowercase(Locale.US) }
    }

    fun agregarAlManifiesto(
        actuales: List<Pasajero>,
        seleccionados: List<ContactoAgenda>,
        maxPasajeros: Int = 10
    ): List<Pasajero> {
        val resultado = actuales.toMutableList()
        val nombresExistentes = resultado.map { it.nombresApellidos.trim().lowercase(Locale.US) }.toMutableSet()

        for (contacto in seleccionados) {
            if (resultado.size >= maxPasajeros) break
            val nombre = contacto.nombre.trim()
            if (nombre.isEmpty() || !nombresExistentes.add(nombre.lowercase(Locale.US))) continue

            // Reemplaza placeholders genéricos "Pasajero #N" antes de expandir la lista
            val placeholderIdx = resultado.indexOfFirst {
                it.nombresApellidos.startsWith("Pasajero ", ignoreCase = true)
            }
            val nuevoPasajero = Pasajero(
                nombresApellidos = nombre,
                tipoDocumento = "DNI",
                numeroDocumento = contacto.telefono.filter { it.isDigit() }.takeLast(8).ifBlank { "Por completar" },
                edad = 25
            )
            if (placeholderIdx >= 0) {
                resultado[placeholderIdx] = nuevoPasajero
            } else {
                resultado.add(nuevoPasajero)
            }
        }
        return resultado
    }
}
