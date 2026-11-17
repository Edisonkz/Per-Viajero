package pe.edu.utp.perviajero.data.repository

import pe.edu.utp.perviajero.data.common.AppResult
import pe.edu.utp.perviajero.data.model.Destino

/**
 * Contrato de acceso al catálogo de destinos turísticos.
 * Permite reemplazar la fuente remota o caché local por Room/Firestore sin impactar la UI.
 */
interface DestinoRepository {
    fun obtenerDestinos(
        forzarRed: Boolean = false,
        onResult: (AppResult<List<Destino>>) -> Unit
    )

    fun obtenerDestinosEnCache(): List<Destino>
}
