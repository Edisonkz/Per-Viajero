package pe.edu.utp.perviajero.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import pe.edu.utp.perviajero.di.ServiceLocator

/**
 * Receptor local de notificaciones push simuladas (equivalente a FirebaseMessagingService.onMessageReceived)
 * y de la acción rápida "Marcar leído" desde la barra de notificaciones.
 */
class AvisoPushReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AvisoPushReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            NotificationHelper.ACCION_MARCAR_LEIDO -> {
                val id = intent.getIntExtra(NotificationHelper.EXTRA_AVISO_ID, -1)
                if (id > 0) {
                    ServiceLocator.getAvisoRepository(context).marcarLeido(id)
                    NotificationHelper.cancelarAviso(context, id)
                    AppLogger.i(TAG, "Aviso #$id marcado como leído desde la notificación")
                }
            }
            else -> {
                val payload = intent.getStringExtra(NotificationHelper.EXTRA_PAYLOAD)
                if (payload.isNullOrBlank()) {
                    AppLogger.w(TAG, "Push recibido sin payload; ignorado")
                    return
                }
                val aviso = AvisoPayloadParser.parsePayload(payload)
                if (aviso == null) {
                    AppLogger.e(TAG, "Payload JSON inválido o incompleto, no se muestra notificación: $payload")
                    return
                }
                AppLogger.i(TAG, "Push procesado [${aviso.tipo}]: ${aviso.titulo}")
                NotificationHelper.registrarYMostrarAviso(context, aviso)
            }
        }
    }
}
