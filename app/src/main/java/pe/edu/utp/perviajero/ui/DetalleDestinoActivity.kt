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
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.Destino

// REQ-03: Pantalla de Detalle recibiendo datos mediante Intent y Bundle
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
    private lateinit var btnComoLlegar: MaterialButton
    private lateinit var btnDetalleFavorito: MaterialButton
    private lateinit var btnCotizarReserva: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_destino)

        // Inicializar vistas
        toolbarDetalle = findViewById(R.id.toolbarDetalle)
        ivDetalleFoto = findViewById(R.id.ivDetalleFoto)
        tvDetalleCategoria = findViewById(R.id.tvDetalleCategoria)
        tvDetalleCalificacion = findViewById(R.id.tvDetalleCalificacion)
        tvDetalleNombre = findViewById(R.id.tvDetalleNombre)
        tvDetalleDepartamento = findViewById(R.id.tvDetalleDepartamento)
        tvDetalleDescripcion = findViewById(R.id.tvDetalleDescripcion)
        btnComoLlegar = findViewById(R.id.btnComoLlegar)
        btnDetalleFavorito = findViewById(R.id.btnDetalleFavorito)
        btnCotizarReserva = findViewById(R.id.btnCotizarReserva)

        // Configuración de Toolbar con botón de retroceso
        setSupportActionBar(toolbarDetalle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbarDetalle.setNavigationOnClickListener {
            finish()
        }

        // Obtener el objeto serializado del Intent
        val destino = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_DESTINO, Destino::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_DESTINO) as? Destino
        }

        if (destino != null) {
            poblarDatos(destino)
        } else {
            finish()
        }
    }

    private fun poblarDatos(destino: Destino) {
        tvDetalleNombre.text = destino.nombre
        tvDetalleDepartamento.text = "${destino.departamento}, Perú"
        tvDetalleCategoria.text = destino.categoria
        tvDetalleCalificacion.text = destino.calificacion.toString()
        tvDetalleDescripcion.text = destino.descripcion
        supportActionBar?.title = destino.nombre

        // Cargar imagen con Glide
        Glide.with(this)
            .load(destino.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_dialog_alert)
            .into(ivDetalleFoto)

        // Cotizador interactivo: Verifica si hay sesión activa o invita a identificarse
        val prefs = PreferencesHelper.getInstance(this)
        btnCotizarReserva.setOnClickListener {
            if (!prefs.isLoggedIn()) {
                com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Acceso a Reservas")
                    .setMessage("Para cotizar y confirmar reservas en PerúViajero se requiere una cuenta. ¿Deseas iniciar sesión o usar acceso rápido?")
                    .setPositiveButton("Iniciar Sesión") { _, _ ->
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    }
                    .setNeutralButton("Continuar como Turista") { _, _ ->
                        prefs.login(
                            pe.edu.utp.perviajero.data.model.Usuario(
                                "usr_kevin_01",
                                "Kevin Turista",
                                "kevin@peruviajero.pe",
                                pe.edu.utp.perviajero.data.model.RolUsuario.CLIENTE
                            )
                        )
                        abrirCotizador(destino)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } else {
                abrirCotizador(destino)
            }
        }

        // Botón ¿Cómo llegar y Ubicación? (Intent explícito hacia RutaDestinoActivity)
        btnComoLlegar.setOnClickListener {
            val intent = Intent(this, RutaDestinoActivity::class.java).apply {
                putExtra(RutaDestinoActivity.EXTRA_DESTINO, destino)
            }
            startActivity(intent)
        }

        // Configurar estado del botón favorito
        actualizarTextoBotonFavorito(prefs.isFavorito(destino.id))

        btnDetalleFavorito.setOnClickListener {
            val nuevoEstado = prefs.toggleFavorito(destino.id)
            actualizarTextoBotonFavorito(nuevoEstado)
        }
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
