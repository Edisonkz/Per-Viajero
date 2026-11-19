package pe.edu.utp.perviajero.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.util.AvisoPayloadParser
import pe.edu.utp.perviajero.util.NotificationHelper
import pe.edu.utp.perviajero.util.WindowInsetsHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla de detalle de un aviso turístico abierta desde la Bandeja o desde una notificación Push.
 * Lee el registro únicamente por EXTRA_AVISO_ID (fuente única de verdad en AvisoRepository),
 * lo marca como leído y cancela su notificación en la barra de estado.
 */
class DetalleAvisoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_aviso)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbarDetalleAviso)
        WindowInsetsHelper.applyToolbarActivityInsets(this, toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val tvTipo: TextView = findViewById(R.id.tvDetalleAvisoTipo)
        val tvTitulo: TextView = findViewById(R.id.tvDetalleAvisoTitulo)
        val tvMeta: TextView = findViewById(R.id.tvDetalleAvisoMeta)
        val tvCuerpo: TextView = findViewById(R.id.tvDetalleAvisoCuerpo)
        val btnAbrirUrl: MaterialButton = findViewById(R.id.btnDetalleAvisoAbrirUrl)
        val btnCerrar: MaterialButton = findViewById(R.id.btnDetalleAvisoCerrar)

        btnCerrar.setOnClickListener { finish() }

        val avisoId = intent.getIntExtra(NotificationHelper.EXTRA_AVISO_ID, -1)
        val repo = ServiceLocator.getAvisoRepository(this)
        val aviso = repo.obtenerPorId(avisoId)

        if (aviso == null) {
            tvTipo.visibility = View.GONE
            tvTitulo.text = "Aviso no encontrado"
            tvMeta.text = "El historial fue limpiado recientemente."
            tvCuerpo.text = "Este aviso ya no se encuentra en la bandeja local de PerúViajero."
            btnAbrirUrl.visibility = View.GONE
            return
        }

        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        tvTipo.text = aviso.tipo.uppercase(Locale.US)
        tvTipo.background = GradientDrawable().apply {
            cornerRadius = 100f
            setColor(NotificationHelper.colorDe(this@DetalleAvisoActivity, aviso.tipo))
        }
        tvTitulo.text = aviso.titulo
        val regionStr = aviso.region.ifBlank { "PerúViajero" }
        tvMeta.text = "$regionStr • ${formato.format(Date(aviso.hora))}"
        tvCuerpo.text = aviso.cuerpo

        if (AvisoPayloadParser.isSafeUrl(aviso.url)) {
            btnAbrirUrl.visibility = View.VISIBLE
            btnAbrirUrl.setOnClickListener {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(aviso.url)))
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(this, "No se encontró un navegador disponible", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            btnAbrirUrl.visibility = View.GONE
        }

        repo.marcarLeido(aviso.id)
        NotificationHelper.cancelarAviso(this, aviso.id)
    }
}
