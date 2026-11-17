package pe.edu.utp.perviajero.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.data.model.RolUsuario
import pe.edu.utp.perviajero.data.model.Usuario
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.util.WindowInsetsHelper

// Pantalla de Detalle con acceso directo a Cotizador, Mapa con Ruta GPS, Comunidad de Viajeros e Itinerario
class DetalleDestinoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DESTINO = "extra_destino"
    }

    private lateinit var toolbarDetalle: MaterialToolbar
    private lateinit var ivDetalleFoto: ImageView
    private lateinit var tvDetalleCategoria: TextView
    private lateinit var tvDetalleCalificacion: TextView
    private lateinit var tvDetalleNombre: TextView
    private lateinit var tvDetalleDepartamento: TextView
    private lateinit var tvDetalleDescripcion: TextView
    private lateinit var btnCotizarReserva: MaterialButton
    private lateinit var btnVerMapaRuta: MaterialButton
    private lateinit var btnVerExperiencias: MaterialButton
    private lateinit var btnPublicarVisita: MaterialButton
    private lateinit var btnPlanearItinerario: MaterialButton
    private lateinit var btnComoLlegar: MaterialButton
    private lateinit var btnDetalleFavorito: MaterialButton

    private var destinoActual: Destino? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_destino)

        toolbarDetalle = findViewById(R.id.toolbarDetalle)
        WindowInsetsHelper.applyToolbarActivityInsets(this, toolbarDetalle)
        ivDetalleFoto = findViewById(R.id.ivDetalleFoto)
        tvDetalleCategoria = findViewById(R.id.tvDetalleCategoria)
        tvDetalleCalificacion = findViewById(R.id.tvDetalleCalificacion)
        tvDetalleNombre = findViewById(R.id.tvDetalleNombre)
        tvDetalleDepartamento = findViewById(R.id.tvDetalleDepartamento)
        tvDetalleDescripcion = findViewById(R.id.tvDetalleDescripcion)
        btnCotizarReserva = findViewById(R.id.btnCotizarReserva)
        btnVerMapaRuta = findViewById(R.id.btnVerMapaRuta)
        btnVerExperiencias = findViewById(R.id.btnVerExperienciasDestino)
        btnPublicarVisita = findViewById(R.id.btnPublicarVisitaDestino)
        btnPlanearItinerario = findViewById(R.id.btnPlanearItinerario)
        btnComoLlegar = findViewById(R.id.btnComoLlegar)
        btnDetalleFavorito = findViewById(R.id.btnDetalleFavorito)

        setSupportActionBar(toolbarDetalle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbarDetalle.setNavigationOnClickListener { finish() }

        val destino = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_DESTINO, Destino::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_DESTINO) as? Destino
        }

        if (destino != null) {
            destinoActual = destino
            poblarDatos(destino)
        } else {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        destinoActual?.let { actualizarContadorExperiencias(it) }
    }

    private fun poblarDatos(destino: Destino) {
        tvDetalleNombre.text = destino.nombre
        tvDetalleDepartamento.text = "${destino.departamento}, Perú"
        tvDetalleCategoria.text = destino.categoria
        tvDetalleCalificacion.text = destino.calificacion.toString()
        tvDetalleDescripcion.text = destino.descripcion
        supportActionBar?.title = destino.nombre

        Glide.with(this)
            .load(destino.imageUrl)
            .placeholder(R.drawable.bg_destino_placeholder)
            .error(R.drawable.bg_destino_placeholder)
            .into(ivDetalleFoto)

        val sessionRepo = ServiceLocator.getSessionRepository(this)
        val favRepo = ServiceLocator.getFavoritoRepository(this)

        btnCotizarReserva.setOnClickListener {
            if (!sessionRepo.isLoggedIn()) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Acceso a Reservas")
                    .setMessage("Para cotizar y confirmar reservas en PerúViajero se requiere una cuenta. ¿Deseas iniciar sesión o usar acceso rápido?")
                    .setPositiveButton("Iniciar Sesión") { _, _ ->
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                    .setNeutralButton("Continuar como Turista") { _, _ ->
                        sessionRepo.login(sessionRepo.getQuickAccessUser(RolUsuario.CLIENTE))
                        abrirCotizador(destino)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } else {
                abrirCotizador(destino)
            }
        }

        btnVerMapaRuta.setOnClickListener {
            val intent = Intent(this, MapaRutaActivity::class.java).apply {
                putExtra(MapaRutaActivity.EXTRA_DESTINO, destino)
            }
            startActivity(intent)
        }

        btnVerExperiencias.setOnClickListener {
            val intent = Intent(this, ComunidadActivity::class.java).apply {
                putExtra(ComunidadActivity.EXTRA_FILTER_REGION, destino.departamento)
                putExtra(ComunidadActivity.EXTRA_FILTER_DESTINO_ID, destino.id)
            }
            startActivity(intent)
        }

        btnPublicarVisita.setOnClickListener {
            val intent = Intent(this, PublicarExperienciaActivity::class.java).apply {
                putExtra(PublicarExperienciaActivity.EXTRA_PRESELECTED_REGION, destino.departamento)
                putExtra(PublicarExperienciaActivity.EXTRA_PRESELECTED_DESTINO_ID, destino.id)
            }
            startActivity(intent)
        }

        btnPlanearItinerario.setOnClickListener {
            val intent = Intent(this, ItinerarioActivity::class.java).apply {
                putExtra(ItinerarioActivity.EXTRA_LUGAR_INICIAL, destino.nombre)
            }
            startActivity(intent)
        }

        btnComoLlegar.setOnClickListener {
            val intent = Intent(this, RutaDestinoActivity::class.java).apply {
                putExtra(RutaDestinoActivity.EXTRA_DESTINO, destino)
            }
            startActivity(intent)
        }

        actualizarTextoBotonFavorito(favRepo.isFavorito(destino.id))
        btnDetalleFavorito.setOnClickListener {
            val nuevoEstado = favRepo.toggleFavorito(destino.id)
            actualizarTextoBotonFavorito(nuevoEstado)
        }

        actualizarContadorExperiencias(destino)
    }

    private fun actualizarContadorExperiencias(destino: Destino) {
        val count = ServiceLocator.getComunidadRepository(this).contarPorDestino(destino.id)
        btnVerExperiencias.text = if (count > 0) "Experiencias ($count)" else "Experiencias"
    }

    private fun abrirCotizador(destino: Destino) {
        val intent = Intent(this, ReservaTourActivity::class.java).apply {
            putExtra(ReservaTourActivity.EXTRA_DESTINO, destino)
        }
        startActivity(intent)
    }

    private fun actualizarTextoBotonFavorito(esFavorito: Boolean) {
        if (esFavorito) {
            btnDetalleFavorito.text = "Quitar de Favoritos"
            btnDetalleFavorito.setIconResource(R.drawable.ic_favoritos)
        } else {
            btnDetalleFavorito.text = "Guardar en Favoritos"
            btnDetalleFavorito.setIconResource(R.drawable.ic_favoritos_border)
        }
    }
}
