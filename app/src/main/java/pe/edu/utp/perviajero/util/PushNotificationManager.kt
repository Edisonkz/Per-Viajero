package pe.edu.utp.perviajero.util

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import pe.edu.utp.perviajero.di.ServiceLocator

/**
 * Encapsula la gestión de notificaciones Push (FCM) con degradación segura cuando
 * aún no se ha vinculado un archivo google-services.json al proyecto.
 */
object PushNotificationManager {

    private const val TAG = "PushManager"

    fun isFirebaseConfigured(context: Context): Boolean {
        return try {
            FirebaseApp.getApps(context.applicationContext).isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }

    fun syncToken(context: Context, onReady: ((String, Boolean) -> Unit)? = null) {
        val sessionRepo = ServiceLocator.getSessionRepository(context)
        if (!isFirebaseConfigured(context)) {
            val localToken = sessionRepo.getFcmToken().ifEmpty {
                "fcm_demo_local_${System.currentTimeMillis() % 100000}"
            }
            sessionRepo.saveFcmToken(localToken)
            AppLogger.i(TAG, "FCM sin google-services.json: modo Push Local listo ($localToken)")
            onReady?.invoke(localToken, false)
            return
        }

        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful && !task.result.isNullOrEmpty()) {
                    val token = task.result
                    sessionRepo.saveFcmToken(token)
                    AppLogger.i(TAG, "Token FCM sincronizado correctamente")
                    onReady?.invoke(token, true)
                } else {
                    AppLogger.w(TAG, "No se pudo obtener token FCM: ${task.exception?.message}")
                    onReady?.invoke(sessionRepo.getFcmToken(), false)
                }
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error inicializando FCM", e)
            onReady?.invoke(sessionRepo.getFcmToken(), false)
        }
    }
}
