package pe.edu.utp.perviajero.ui

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.network.ApiClient
import pe.edu.utp.perviajero.util.AppLogger
import pe.edu.utp.perviajero.util.NotificationHelper
import pe.edu.utp.perviajero.util.PushNotificationManager
import pe.edu.utp.perviajero.util.ReminderScheduler
import java.util.Locale

/**
 * Pantalla técnica para inspeccionar logs en memoria, tamaño de caché, estado Push FCM y pruebas de WorkManager.
 */
class DiagnosticoLogsActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvResumen: TextView
    private lateinit var tvLogs: TextView
    private lateinit var btnTestPush: MaterialButton
    private lateinit var btnTestWorker: MaterialButton
    private lateinit var btnLimpiar: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diagnostico_logs)

        toolbar = findViewById(R.id.toolbarDiagnostico)
        pe.edu.utp.perviajero.util.WindowInsetsHelper.applyToolbarActivityInsets(this, toolbar)
        tvResumen = findViewById(R.id.tvDiagResumen)
        tvLogs = findViewById(R.id.tvDiagLogsContenido)
        btnTestPush = findViewById(R.id.btnDiagTestPush)
        btnTestWorker = findViewById(R.id.btnDiagTestWorker)
        btnLimpiar = findViewById(R.id.btnLimpiarLogs)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        btnTestPush.setOnClickListener {
            NotificationHelper.mostrarNotificacionPush(
                this,
                "Alerta Regional Cusco • PerúViajero",
                "Clima despejado en Machu Picchu y Valle Sagrado. Revisa tu itinerario."
            )
            AppLogger.i("Diagnostico", "Notificación Push de prueba emitida")
            renderizarDatos()
        }

        btnTestWorker.setOnClickListener {
            ReminderScheduler.programarRecordatorioViaje(
                this,
                "Recordatorio de Viaje (WorkManager)",
                "Tu próximo recorrido turístico en PerúViajero está próximo a iniciar.",
                delaySeconds = 10L
            )
            Toast.makeText(this, "Recordatorio programado para dentro de 10 segundos", Toast.LENGTH_SHORT).show()
            renderizarDatos()
        }

        btnLimpiar.setOnClickListener {
            AppLogger.limpiarLogs()
            renderizarDatos()
            Toast.makeText(this, "Historial de logs limpiado", Toast.LENGTH_SHORT).show()
        }

        renderizarDatos()
    }

    private fun renderizarDatos() {
        val online = ApiClient.isOnline()
        val cacheKb = ApiClient.getCacheSizeBytes() / 1024.0
        val fcmConfigured = PushNotificationManager.isFirebaseConfigured(this)
        val fcmToken = ServiceLocator.getSessionRepository(this).getFcmToken()

        tvResumen.text = String.format(
            Locale.US,
            "• Conectividad: %s\n" +
                "• Caché HTTP OkHttp: %.1f KB\n" +
                "• Firebase Push (FCM): %s\n" +
                "• Token registrado: %s",
            if (online) "En línea" else "Offline",
            cacheKb,
            if (fcmConfigured) "Activo (google-services.json detectado)" else "Modo Push Local listo (sin google-services.json)",
            fcmToken.ifEmpty { "Pendiente" }
        )

        val logs = AppLogger.obtenerLogs()
        tvLogs.text = if (logs.isEmpty()) {
            "No hay eventos registrados aún."
        } else {
            logs.joinToString("\n") { "[${it.timestamp}] ${it.level.name}/${it.tag}: ${it.message}" }
        }
    }
}
