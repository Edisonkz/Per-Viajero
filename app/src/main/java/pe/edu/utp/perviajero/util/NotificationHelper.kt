package pe.edu.utp.perviajero.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import pe.edu.utp.perviajero.MainActivity
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.model.Reserva
import java.util.Locale

object NotificationHelper {

    private const val CHANNEL_ID_RESERVAS = "canal_reservas_peruviajero"
    private const val CHANNEL_NAME_RESERVAS = "Reservas y Tours PerúViajero"
    private const val CHANNEL_DESC_RESERVAS = "Confirmación de vouchers y despacho de viajes"

    private const val CHANNEL_ID_RECORDATORIOS = "canal_recordatorios_peruviajero"
    private const val CHANNEL_NAME_RECORDATORIOS = "Recordatorios y Alertas de Viaje"
    private const val CHANNEL_DESC_RECORDATORIOS = "Recordatorios programados y notificaciones push regionales"

    fun crearCanalNotificaciones(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val canalReservas = NotificationChannel(
                CHANNEL_ID_RESERVAS,
                CHANNEL_NAME_RESERVAS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_RESERVAS
                enableVibration(true)
            }

            val canalRecordatorios = NotificationChannel(
                CHANNEL_ID_RECORDATORIOS,
                CHANNEL_NAME_RECORDATORIOS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_RECORDATORIOS
                enableVibration(true)
            }

            manager.createNotificationChannel(canalReservas)
            manager.createNotificationChannel(canalRecordatorios)
        }
    }

    private fun canPostNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    private fun buildMainPendingIntent(context: Context, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun notificarReservaConfirmada(context: Context, reserva: Reserva) {
        crearCanalNotificaciones(context)
        if (!canPostNotifications(context)) return

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_RESERVAS)
            .setSmallIcon(R.drawable.ic_check_circle)
            .setContentTitle("¡Reserva Confirmada! [${reserva.codigoReserva}]")
            .setContentText("Tour a ${reserva.destinoNombre} para el ${reserva.fechaSalida} registrado.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "¡Tu viaje a ${reserva.destinoNombre} ha sido confirmado con éxito!\n" +
                        "• Código: ${reserva.codigoReserva}\n" +
                        "• Salida: ${reserva.fechaSalida} (${reserva.horarioSalida})\n" +
                        "• Pasajeros: ${reserva.cantidadPersonas}\n" +
                        "• Total: S/ ${String.format(Locale.US, "%.2f", reserva.costoTotal)}"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(buildMainPendingIntent(context, 1001))
            .setAutoCancel(true)

        val notificationId = (System.currentTimeMillis() % 10000).toInt()
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    fun notificarActualizacionEstado(
        context: Context,
        codigoReserva: String,
        destinoNombre: String,
        nuevoEstado: String
    ) {
        crearCanalNotificaciones(context)
        if (!canPostNotifications(context)) return

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_RESERVAS)
            .setSmallIcon(R.drawable.ic_dashboard)
            .setContentTitle("Actualización de Tour [$codigoReserva]")
            .setContentText("El estado de tu viaje a $destinoNombre ahora es: $nuevoEstado.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "La agencia ha actualizado tu reserva:\n" +
                        "• Voucher: $codigoReserva\n" +
                        "• Destino: $destinoNombre\n" +
                        "• Nuevo Estado: $nuevoEstado"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(buildMainPendingIntent(context, 1002))
            .setAutoCancel(true)

        val notificationId = (System.currentTimeMillis() % 10000).toInt() + 1
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    fun mostrarRecordatorioViaje(context: Context, titulo: String, mensaje: String) {
        crearCanalNotificaciones(context)
        if (!canPostNotifications(context)) return

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_RECORDATORIOS)
            .setSmallIcon(R.drawable.ic_calendar)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(buildMainPendingIntent(context, 1003))
            .setAutoCancel(true)

        val notificationId = (System.currentTimeMillis() % 10000).toInt() + 10
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    fun mostrarNotificacionPush(context: Context, titulo: String, cuerpo: String) {
        crearCanalNotificaciones(context)
        if (!canPostNotifications(context)) return

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_RECORDATORIOS)
            .setSmallIcon(R.drawable.ic_logo_peruviajero_agency)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(buildMainPendingIntent(context, 1004))
            .setAutoCancel(true)

        val notificationId = (System.currentTimeMillis() % 10000).toInt() + 20
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }
}
