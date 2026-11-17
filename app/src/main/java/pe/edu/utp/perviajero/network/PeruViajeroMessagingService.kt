package pe.edu.utp.perviajero.network

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.util.AppLogger
import pe.edu.utp.perviajero.util.NotificationHelper

/**
 * Servicio receptor de Firebase Cloud Messaging (FCM) para notificaciones push en primer y segundo plano.
 */
class PeruViajeroMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "PeruViajeroFCM"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        ServiceLocator.getSessionRepository(applicationContext).saveFcmToken(token)
        AppLogger.i(TAG, "Nuevo token FCM registrado")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val titulo = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Alerta Turística PerúViajero"
        val cuerpo = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "Revisa las novedades de tu destino."

        AppLogger.i(TAG, "Push recibido: $titulo")
        NotificationHelper.mostrarNotificacionPush(applicationContext, titulo, cuerpo)
    }
}
