package pe.edu.utp.perviajero.util

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.model.AvisoTuristico
import pe.edu.utp.perviajero.data.model.Reserva
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.ui.BandejaAvisosActivity
import pe.edu.utp.perviajero.ui.DetalleAvisoActivity
import java.util.Locale

/**
 * Gestor central de notificaciones de PerúViajero:
 * - 4 canales con distinta importancia (Alerta HIGH, Reserva HIGH, Itinerario DEFAULT, Aviso LOW).
 * - Agrupación automática con InboxStyle y GROUP_ALERT_CHILDREN (≥ 2 no leídos).
 * - Pila de navegación reconstruida con TaskStackBuilder pasando únicamente EXTRA_AVISO_ID.
 * - Acción rápida "Marcar leído" directamente desde la barra de notificaciones.
 */
object NotificationHelper {

    const val CANAL_ALERTAS = "canal_alertas_peruviajero"
    const val CANAL_RESERVAS = "canal_reservas_peruviajero"
    const val CANAL_ITINERARIO = "canal_itinerario_peruviajero"
    const val CANAL_AVISOS = "canal_avisos_peruviajero"

    const val GRUPO_AVISOS = "grupo_avisos_peruviajero"
    const val ID_RESUMEN = 1

    const val ACCION_PUSH = "pe.edu.utp.perviajero.ACCION_PUSH"
    const val ACCION_MARCAR_LEIDO = "pe.edu.utp.perviajero.ACCION_MARCAR_LEIDO"
    const val EXTRA_PAYLOAD = "extra_payload_json"
    const val EXTRA_AVISO_ID = "extra_aviso_id"

    fun crearCanalNotificaciones(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannels(
                listOf(
                    NotificationChannel(
                        CANAL_ALERTAS,
                        "Alertas Climáticas y Rutas",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Avisos urgentes de clima, cierres de vías o seguridad turística"
                        enableVibration(true)
                    },
                    NotificationChannel(
                        CANAL_RESERVAS,
                        "Reservas y Embarque",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Confirmaciones de boletos, vouchers y cambios de estado de tours"
                        enableVibration(true)
                    },
                    NotificationChannel(
                        CANAL_ITINERARIO,
                        "Recordatorios de Itinerario",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "Recordatorios programados de salidas y actividades"
                    },
                    NotificationChannel(
                        CANAL_AVISOS,
                        "Novedades y Promociones",
                        NotificationManager.IMPORTANCE_LOW
                    ).apply {
                        description = "Comunicados informativos y nuevas rutas sin interrupción"
                    }
                )
            )
        }
    }

    fun canPostNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    private fun canalDe(tipo: String): String = when (tipo.lowercase(Locale.US)) {
        "alerta" -> CANAL_ALERTAS
        "reserva" -> CANAL_RESERVAS
        "itinerario" -> CANAL_ITINERARIO
        else -> CANAL_AVISOS
    }

    private fun prioridadDe(tipo: String): Int = when (tipo.lowercase(Locale.US)) {
        "alerta", "reserva" -> NotificationCompat.PRIORITY_HIGH
        "itinerario" -> NotificationCompat.PRIORITY_DEFAULT
        else -> NotificationCompat.PRIORITY_LOW
    }

    fun colorDe(context: Context, tipo: String): Int {
        val colorRes = when (tipo.lowercase(Locale.US)) {
            "alerta" -> R.color.colorAccent
            "reserva" -> R.color.colorPrimary
            "itinerario" -> R.color.colorPrimaryDark
            else -> R.color.colorStar
        }
        return ContextCompat.getColor(context, colorRes)
    }

    private fun iconoDe(tipo: String): Int = when (tipo.lowercase(Locale.US)) {
        "reserva" -> R.drawable.ic_receipt
        "itinerario" -> R.drawable.ic_calendar
        "alerta" -> R.drawable.ic_notifications
        else -> R.drawable.ic_explore_mountain
    }

    private fun pendingDetalle(context: Context, avisoId: Int): PendingIntent? {
        val intent = Intent(context, DetalleAvisoActivity::class.java).apply {
            putExtra(EXTRA_AVISO_ID, avisoId)
        }
        return TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(intent)
            .getPendingIntent(
                avisoId,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
    }

    private fun pendingMarcarLeido(context: Context, avisoId: Int): PendingIntent {
        val intent = Intent(context, AvisoPushReceiver::class.java).apply {
            action = ACCION_MARCAR_LEIDO
            putExtra(EXTRA_AVISO_ID, avisoId)
        }
        return PendingIntent.getBroadcast(
            context,
            avisoId + 50000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    fun registrarYMostrarAviso(context: Context, aviso: AvisoTuristico) {
        crearCanalNotificaciones(context)
        ServiceLocator.getAvisoRepository(context).agregarAviso(aviso)
        mostrarAvisoSinGuardar(context, aviso)
    }

    @SuppressLint("MissingPermission")
    fun mostrarAvisoSinGuardar(context: Context, aviso: AvisoTuristico) {
        crearCanalNotificaciones(context)
        if (!canPostNotifications(context)) return

        val builder = NotificationCompat.Builder(context, canalDe(aviso.tipo))
            .setSmallIcon(iconoDe(aviso.tipo))
            .setContentTitle(aviso.titulo)
            .setContentText(aviso.cuerpo)
            .setSubText(aviso.region.ifBlank { null })
            .setStyle(NotificationCompat.BigTextStyle().bigText(aviso.cuerpo))
            .setPriority(prioridadDe(aviso.tipo))
            .setColor(colorDe(context, aviso.tipo))
            .setGroup(GRUPO_AVISOS)
            .setAutoCancel(true)
            .setContentIntent(pendingDetalle(context, aviso.id))
            .addAction(
                R.drawable.ic_check_circle,
                "Marcar leído",
                pendingMarcarLeido(context, aviso.id)
            )

        NotificationManagerCompat.from(context).notify(aviso.id, builder.build())
        mostrarResumenGrupo(context)
    }

    @SuppressLint("MissingPermission")
    private fun mostrarResumenGrupo(context: Context) {
        if (!canPostNotifications(context)) return
        val sinLeer = ServiceLocator.getAvisoRepository(context).listarAvisos().filter { !it.leido }
        if (sinLeer.size < 2) return

        val inbox = NotificationCompat.InboxStyle()
            .setBigContentTitle("${sinLeer.size} avisos sin leer en PerúViajero")
        sinLeer.take(5).forEach { inbox.addLine("[${it.tipo.uppercase(Locale.US)}] ${it.titulo}") }

        val abrirBandeja = TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(Intent(context, BandejaAvisosActivity::class.java))
            .getPendingIntent(
                ID_RESUMEN,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val resumen = NotificationCompat.Builder(context, CANAL_AVISOS)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle("PerúViajero")
            .setContentText("${sinLeer.size} avisos nuevos")
            .setStyle(inbox)
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .setGroup(GRUPO_AVISOS)
            .setGroupSummary(true)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
            .setAutoCancel(true)
            .setContentIntent(abrirBandeja)
            .build()

        NotificationManagerCompat.from(context).notify(ID_RESUMEN, resumen)
    }

    fun cancelarAviso(context: Context, avisoId: Int) {
        val manager = NotificationManagerCompat.from(context)
        manager.cancel(avisoId)
        if (ServiceLocator.getAvisoRepository(context).contarNoLeidos() < 2) {
            manager.cancel(ID_RESUMEN)
        }
    }

    fun cancelarTodas(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }

    private fun nextId(): Int = ((System.currentTimeMillis() % 900000L).toInt() + 100)

    fun notificarReservaConfirmada(context: Context, reserva: Reserva) {
        val cuerpo = "¡Tu viaje a ${reserva.destinoNombre} ha sido confirmado!\n" +
            "• Código: ${reserva.codigoReserva}\n" +
            "• Salida: ${reserva.fechaSalida} (${reserva.horarioSalida})\n" +
            "• Pasajeros: ${reserva.cantidadPersonas} • Total: S/ ${String.format(Locale.US, "%.2f", reserva.costoTotal)}"
        registrarYMostrarAviso(
            context,
            AvisoTuristico(
                id = nextId(),
                tipo = "reserva",
                titulo = "¡Reserva Confirmada! [${reserva.codigoReserva}]",
                cuerpo = cuerpo,
                region = reserva.destinoNombre,
                hora = System.currentTimeMillis(),
                leido = false
            )
        )
    }

    fun notificarActualizacionEstado(
        context: Context,
        codigoReserva: String,
        destinoNombre: String,
        nuevoEstado: String
    ) {
        registrarYMostrarAviso(
            context,
            AvisoTuristico(
                id = nextId(),
                tipo = "reserva",
                titulo = "Actualización de Tour [$codigoReserva]",
                cuerpo = "La agencia actualizó tu viaje a $destinoNombre al estado: $nuevoEstado.",
                region = destinoNombre,
                hora = System.currentTimeMillis(),
                leido = false
            )
        )
    }

    fun mostrarRecordatorioViaje(context: Context, titulo: String, mensaje: String) {
        registrarYMostrarAviso(
            context,
            AvisoTuristico(
                id = nextId(),
                tipo = "itinerario",
                titulo = titulo,
                cuerpo = mensaje,
                region = "Itinerario",
                hora = System.currentTimeMillis(),
                leido = false
            )
        )
    }

    fun mostrarNotificacionPush(
        context: Context,
        titulo: String,
        cuerpo: String,
        tipo: String = "alerta",
        region: String = "Perú",
        url: String = ""
    ) {
        registrarYMostrarAviso(
            context,
            AvisoTuristico(
                id = nextId(),
                tipo = tipo,
                titulo = titulo,
                cuerpo = cuerpo,
                region = region,
                url = if (AvisoPayloadParser.isSafeUrl(url)) url else "",
                hora = System.currentTimeMillis(),
                leido = false
            )
        )
    }
}
