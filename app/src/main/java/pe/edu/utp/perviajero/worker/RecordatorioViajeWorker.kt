package pe.edu.utp.perviajero.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import pe.edu.utp.perviajero.util.AppLogger
import pe.edu.utp.perviajero.util.NotificationHelper

/**
 * Worker de WorkManager para disparar recordatorios locales programados en segundo plano.
 */
class RecordatorioViajeWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {

    companion object {
        const val KEY_TITULO = "key_titulo"
        const val KEY_MENSAJE = "key_mensaje"
        private const val TAG = "RecordatorioWorker"
    }

    override fun doWork(): Result {
        val titulo = inputData.getString(KEY_TITULO) ?: "Recordatorio de Viaje • PerúViajero"
        val mensaje = inputData.getString(KEY_MENSAJE) ?: "Tienes actividades o salidas programadas próximamente."

        AppLogger.i(TAG, "Ejecutando recordatorio programado: $titulo")
        NotificationHelper.mostrarRecordatorioViaje(applicationContext, titulo, mensaje)
        return Result.success()
    }
}
