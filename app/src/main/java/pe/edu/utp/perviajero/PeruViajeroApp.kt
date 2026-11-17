package pe.edu.utp.perviajero

import android.app.Application
import android.content.Intent
import pe.edu.utp.perviajero.network.ApiClient
import pe.edu.utp.perviajero.ui.ErrorFatalActivity
import pe.edu.utp.perviajero.util.AppLogger
import pe.edu.utp.perviajero.util.NotificationHelper
import pe.edu.utp.perviajero.util.PushNotificationManager
import kotlin.system.exitProcess

/**
 * Clase Application global encargada de inicializar observabilidad, red, canales y manejo de fallos.
 */
class PeruViajeroApp : Application() {

    companion object {
        private const val TAG = "PeruViajeroApp"
    }

    override fun onCreate() {
        super.onCreate()
        AppLogger.init(this)
        ApiClient.initialize(this)
        NotificationHelper.crearCanalNotificaciones(this)
        PushNotificationManager.syncToken(this)
        registrarManejadorGlobalDeErrores()
        AppLogger.i(TAG, "Aplicación iniciada correctamente")
    }

    private fun registrarManejadorGlobalDeErrores() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                AppLogger.e(TAG, "Excepción no controlada en hilo ${thread.name}", throwable)
                val intent = Intent(this, ErrorFatalActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtra(
                        ErrorFatalActivity.EXTRA_ERROR_DETAIL,
                        "${throwable.javaClass.simpleName}: ${throwable.message ?: "Error inesperado"}"
                    )
                }
                startActivity(intent)
                exitProcess(1)
            } catch (_: Exception) {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
