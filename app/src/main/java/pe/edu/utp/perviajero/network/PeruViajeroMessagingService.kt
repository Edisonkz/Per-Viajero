package pe.edu.utp.perviajero.network

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.util.AppLogger
import pe.edu.utp.perviajero.util.NotificationHelper

/**
 * Servicio receptor de Firebase Cloud Messaging (FCM) unificado con NotificationHelper y AvisoRepository.
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
        val data = remoteMessage.data
        val titulo = data["titulo"]
            ?: data["title"]
            ?: remoteMessage.notification?.title
            ?: "Alerta Turística PerúViajero"
        val cuerpo = data["cuerpo"]
            ?: data["body"]
            ?: remoteMessage.notification?.body
            ?: "Revisa las novedades de tu destino."
        val tipo = data["tipo"] ?: "alerta"
        val region = data["region"] ?: "Perú"
        val url = data["url"] ?: ""

        AppLogger.i(TAG, "Push FCM recibido [$tipo]: $titulo")
        NotificationHelper.mostrarNotificacionPush(
            context = applicationContext,
            titulo = titulo,
            cuerpo = cuerpo,
            tipo = tipo,
            region = region,
            url = url
        )
    }
}
