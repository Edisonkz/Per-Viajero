package pe.edu.utp.perviajero.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

/**
 * Simulador de servidor Push diferido usando AlarmManager (setAndAllowWhileIdle),
 * permitiendo probar notificaciones JSON incluso con la aplicación cerrada.
 */
object AvisoPushSimulator {

    fun programarPushSimulado(context: Context, payloadJson: String, segundos: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, AvisoPushReceiver::class.java).apply {
            action = NotificationHelper.ACCION_PUSH
            putExtra(NotificationHelper.EXTRA_PAYLOAD, payloadJson)
        }
        val requestCode = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + segundos.coerceIn(3, 600) * 1000L,
            pending
        )
        AppLogger.i("AvisoPushSimulator", "Push JSON programado en $segundos s")
    }
}
