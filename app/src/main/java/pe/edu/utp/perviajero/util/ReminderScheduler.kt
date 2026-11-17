package pe.edu.utp.perviajero.util

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import pe.edu.utp.perviajero.worker.RecordatorioViajeWorker
import java.util.concurrent.TimeUnit

/**
 * Programador de recordatorios locales mediante WorkManager (persistentes ante cierre de app).
 */
object ReminderScheduler {

    private const val TAG = "ReminderScheduler"

    fun programarRecordatorioViaje(
        context: Context,
        titulo: String,
        mensaje: String,
        delaySeconds: Long
    ) {
        val data = Data.Builder()
            .putString(RecordatorioViajeWorker.KEY_TITULO, titulo)
            .putString(RecordatorioViajeWorker.KEY_MENSAJE, mensaje)
            .build()

        val request = OneTimeWorkRequestBuilder<RecordatorioViajeWorker>()
            .setInitialDelay(delaySeconds.coerceAtLeast(1L), TimeUnit.SECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context.applicationContext).enqueue(request)
        AppLogger.i(TAG, "Recordatorio programado en ${delaySeconds}s: $titulo")
    }
}
