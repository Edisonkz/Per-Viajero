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

object NotificationHelper {

    private const val CHANNEL_ID = "canal_reservas_peruviajero"
    private const val CHANNEL_NAME = "Reservas y Tours PerúViajero"
    private const val CHANNEL_DESC = "Notificaciones de confirmación de vouchers y despacho de viajes"

    fun crearCanalNotificaciones(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun notificarReservaConfirmada(context: Context, reserva: Reserva) {
        crearCanalNotificaciones(context)

        // Verificar permiso en Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_check_circle)
            .setContentTitle("🎉 ¡Reserva Confirmada! [${reserva.codigoReserva}]")
            .setContentText("Tour a ${reserva.destinoNombre} para el ${reserva.fechaSalida} registrado.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "¡Tu viaje a ${reserva.destinoNombre} ha sido confirmado con éxito!\n" +
                    "• Código: ${reserva.codigoReserva}\n" +
                    "• Salida: ${reserva.fechaSalida}\n" +
                    "• Pasajeros: ${reserva.cantidadPersonas}\n" +
                    "• Guía: ${reserva.guiaTuristico}\n" +
                    "• Total: S/ ${String.format(java.util.Locale.US, "%.2f", reserva.costoTotal)}\n" +
                    "Toca para abrir tu aplicación y revisar tu voucher."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dashboard)
            .setContentTitle("🚐 Actualización de Tour [${codigoReserva}]")
            .setContentText("El estado de tu viaje a $destinoNombre ahora es: $nuevoEstado.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "La agencia de viajes ha actualizado el estado de tu tour:\n" +
                    "• Código de Voucher: $codigoReserva\n" +
                    "• Destino: $destinoNombre\n" +
                    "• Nuevo Estado: $nuevoEstado\n" +
                    "¡Gracias por viajar con PerúViajero!"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationId = (System.currentTimeMillis() % 10000).toInt() + 1
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }
}

