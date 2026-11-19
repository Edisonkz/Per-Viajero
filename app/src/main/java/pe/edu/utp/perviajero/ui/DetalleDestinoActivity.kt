package pe.edu.utp.perviajero.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.ui.common.VoucherDialogHelper
import pe.edu.utp.perviajero.util.CotizadorCalculator
import pe.edu.utp.perviajero.util.GeoMath
import pe.edu.utp.perviajero.util.LocationHelper

/**
 * Pantalla Hero de Detalle del Destino con CollapsingToolbarLayout,
 * botones flotantes, grid de accesos 4-en-1, previa de Comunidad y barra sticky inferior.
 */
class DetalleDestinoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DESTINO = "extra_destino"
    }

    private lateinit var ivDetalleFoto: ImageView
    private lateinit var btnHeroBack: ImageButton
    private lateinit var btnHeroFavorito: ImageButton
    private lateinit var btnHeroCompartir: ImageButton

    private lateinit var tvDetalleCategoria: TextView
    private lateinit var tvDetalleCalificacion: TextView
    private lateinit var tvDetalleNombre: TextView
    private lateinit var tvDetalleDepartamento: TextView
    private lateinit var tvDetalleDescripcion: TextView

    private lateinit var cardHeroRutaGps: View
    private lateinit var cardHeroBrujula: View
    private lateinit var cardHeroResenas: View
    private lateinit var tvHeroResenasLabel: TextView
    private lateinit var cardHeroItinerario: View
    private lateinit var btnComoLlegar: MaterialButton

    private lateinit var cardPreviaComunidad: View
    private lateinit var tvDetalleComunidadAutor: TextView
    private lateinit var tvDetalleComunidadEstrellas: TextView
    private lateinit var tvDetalleComunidadTexto: TextView
    private lateinit var btnPublicarVisitaDestino: View

    private lateinit var tvDetallePrecioBase: TextView
    private lateinit var btnCotizarReserva: MaterialButton

    private var destinoActual: Destino? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_destino)

        inicializarVistas()
        aplicarWindowInsetsHero()

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
        destinoActual?.let { actualizarPreviaComunidad(it) }
    }

    private fun inicializarVistas() {
        ivDetalleFoto = findViewById(R.id.ivDetalleFoto)
        btnHeroBack = findViewById(R.id.btnHeroBack)
        btnHeroFavorito = findViewById(R.id.btnHeroFavorito)
        btnHeroCompartir = findViewById(R.id.btnHeroCompartir)

        tvDetalleCategoria = findViewById(R.id.tvDetalleCategoria)
        tvDetalleCalificacion = findViewById(R.id.tvDetalleCalificacion)
        tvDetalleNombre = findViewById(R.id.tvDetalleNombre)
        tvDetalleDepartamento = findViewById(R.id.tvDetalleDepartamento)
        tvDetalleDescripcion = findViewById(R.id.tvDetalleDescripcion)

        cardHeroRutaGps = findViewById(R.id.cardHeroRutaGps)
        cardHeroBrujula = findViewById(R.id.cardHeroBrujula)
        cardHeroResenas = findViewById(R.id.cardHeroResenas)
        tvHeroResenasLabel = findViewById(R.id.tvHeroResenasLabel)
        cardHeroItinerario = findViewById(R.id.cardHeroItinerario)
        btnComoLlegar = findViewById(R.id.btnComoLlegar)

        cardPreviaComunidad = findViewById(R.id.cardPreviaComunidad)
        tvDetalleComunidadAutor = findViewById(R.id.tvDetalleComunidadAutor)
        tvDetalleComunidadEstrellas = findViewById(R.id.tvDetalleComunidadEstrellas)
        tvDetalleComunidadTexto = findViewById(R.id.tvDetalleComunidadTexto)
        btnPublicarVisitaDestino = findViewById(R.id.btnPublicarVisitaDestino)

        tvDetallePrecioBase = findViewById(R.id.tvDetallePrecioBase)
        btnCotizarReserva = findViewById(R.id.btnCotizarReserva)

        btnHeroBack.setOnClickListener { finish() }
    }

    private fun aplicarWindowInsetsHero() {
        val root: View = findViewById(R.id.detalleCoordinator)
        val topButtonsBar: View = findViewById(R.id.heroTopButtonsBar)
        val stickyBottomBar: View = findViewById(R.id.detalleStickyBottomBar)
        val sheetBody: View = findViewById(R.id.detalleSheetBody)

        val initTopBarPadding = topButtonsBar.paddingTop
        val initBottomBarPadding = stickyBottomBar.paddingBottom
        val initSheetBottomPadding = sheetBody.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
            val bars = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            topButtonsBar.setPadding(
                topButtonsBar.paddingLeft,
                initTopBarPadding + bars.top,
                topButtonsBar.paddingRight,
                topButtonsBar.paddingBottom
            )
            stickyBottomBar.setPadding(
                stickyBottomBar.paddingLeft + bars.left,
                stickyBottomBar.paddingTop,
                stickyBottomBar.paddingRight + bars.right,
                initBottomBarPadding + bars.bottom
            )
            sheetBody.setPadding(
                sheetBody.paddingLeft,
                sheetBody.paddingTop,
                sheetBody.paddingRight,
                initSheetBottomPadding + bars.bottom
            )
            windowInsets
        }
        ViewCompat.requestApplyInsets(root)
    }

    private fun poblarDatos(destino: Destino) {
        tvDetalleNombre.text = destino.nombre
        tvDetalleCategoria.text = destino.categoria
        tvDetalleCalificacion.text = destino.calificacion.toString()
        tvDetalleDescripcion.text = destino.descripcion
        tvDetallePrecioBase.text = "S/ ${CotizadorCalculator.PRECIO_BASE_POR_PERSONA.toInt()}"

        actualizarUbicacionConDistancia(destino)

        val heroImageModel: Any = when {
            destino.imageUrl.isBlank() -> R.drawable.bg_destino_placeholder
            destino.imageUrl.startsWith("http") -> destino.imageUrl
            else -> Uri.parse(destino.imageUrl)
        }
        Glide.with(this)
            .load(heroImageModel)
            .placeholder(R.drawable.bg_destino_placeholder)
            .error(R.drawable.bg_destino_placeholder)
            .into(ivDetalleFoto)

        val favRepo = ServiceLocator.getFavoritoRepository(this)
        actualizarIconoFavorito(favRepo.isFavorito(destino.id))
        btnHeroFavorito.setOnClickListener {
            val nuevoEstado = favRepo.toggleFavorito(destino.id)
            actualizarIconoFavorito(nuevoEstado)
        }

        btnHeroCompartir.setOnClickListener { compartirDestino(destino) }

        btnCotizarReserva.setOnClickListener {
            VoucherDialogHelper.openCotizadorConValidacion(this, destino)
        }

        cardHeroRutaGps.setOnClickListener {
            startActivity(Intent(this, MapaRutaActivity::class.java).apply {
                putExtra(MapaRutaActivity.EXTRA_DESTINO, destino)
            })
        }

        cardHeroBrujula.setOnClickListener {
            startActivity(Intent(this, BrujulaActivity::class.java).apply {
                putExtra(BrujulaActivity.EXTRA_DESTINO, destino)
            })
        }

        val abrirResenasDestino = {
            startActivity(Intent(this, ComunidadActivity::class.java).apply {
                putExtra(ComunidadActivity.EXTRA_FILTER_REGION, destino.departamento)
                putExtra(ComunidadActivity.EXTRA_FILTER_DESTINO_ID, destino.id)
            })
        }
        cardHeroResenas.setOnClickListener { abrirResenasDestino() }
        cardPreviaComunidad.setOnClickListener { abrirResenasDestino() }

        btnPublicarVisitaDestino.setOnClickListener {
            pe.edu.utp.perviajero.ui.common.AuthGuardHelper.requireAuth(
                this,
                "Inicia sesión para publicar",
                "Comparte fotos y consejos sobre ${destino.nombre} iniciando sesión en PerúViajero."
            ) {
                startActivity(Intent(this, PublicarExperienciaActivity::class.java).apply {
                    putExtra(PublicarExperienciaActivity.EXTRA_PRESELECTED_REGION, destino.departamento)
                    putExtra(PublicarExperienciaActivity.EXTRA_PRESELECTED_DESTINO_ID, destino.id)
                })
            }
        }

        cardHeroItinerario.setOnClickListener {
            startActivity(Intent(this, ItinerarioActivity::class.java).apply {
                putExtra(ItinerarioActivity.EXTRA_LUGAR_INICIAL, destino.nombre)
            })
        }

        btnComoLlegar.setOnClickListener {
            startActivity(Intent(this, RutaDestinoActivity::class.java).apply {
                putExtra(RutaDestinoActivity.EXTRA_DESTINO, destino)
            })
        }

        actualizarPreviaComunidad(destino)
    }

    private fun actualizarUbicacionConDistancia(destino: Destino) {
        val origin = ServiceLocator.getSessionRepository(this).getLastLocation()
        renderUbicacion(destino, origin)

        LocationHelper(this).obtainCurrentLocation { loc ->
            if (isFinishing || isDestroyed || loc == null) return@obtainCurrentLocation
            runOnUiThread {
                renderUbicacion(destino, loc.latitude to loc.longitude)
            }
        }
    }

    private fun renderUbicacion(destino: Destino, origin: Pair<Double, Double>?) {
        val ubicacionBase = listOfNotNull(
            destino.distrito?.takeIf { it.isNotBlank() },
            destino.provincia?.takeIf { it.isNotBlank() && !it.equals(destino.distrito, ignoreCase = true) },
            destino.departamento.takeIf { it.isNotBlank() }
        ).distinct().joinToString(", ")

        tvDetalleDepartamento.text = if (origin != null && destino.latitud != null && destino.longitud != null) {
            val km = GeoMath.haversineDistanceKm(origin.first, origin.second, destino.latitud, destino.longitud)
            "$ubicacionBase, Perú • A ${GeoMath.formatDistance(km)} de ti"
        } else {
            "$ubicacionBase, Perú"
        }
    }

    private fun actualizarPreviaComunidad(destino: Destino) {
        val repo = ServiceLocator.getComunidadRepository(this)
        val posts = repo.filtrarPublicaciones(destinoId = destino.id)
        val count = posts.size
        tvHeroResenasLabel.text = if (count > 0) "Reseñas ($count)" else "Reseñas"

        val latest = posts.firstOrNull()
        if (latest != null) {
            tvDetalleComunidadAutor.text = "${latest.autorNombre} • Comunidad"
            tvDetalleComunidadEstrellas.text = "★".repeat(latest.calificacion.toInt().coerceIn(1, 5))
            tvDetalleComunidadTexto.text = latest.comentario
        } else {
            tvDetalleComunidadAutor.text = "Comunidad de Viajeros"
            tvDetalleComunidadEstrellas.text = "★★★★★"
            tvDetalleComunidadTexto.text = "¡Sé el primero en compartir una foto o reseña de ${destino.nombre}!"
        }
    }

    private fun actualizarIconoFavorito(esFavorito: Boolean) {
        if (esFavorito) {
            btnHeroFavorito.setImageResource(R.drawable.ic_favoritos)
            btnHeroFavorito.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent))
        } else {
            btnHeroFavorito.setImageResource(R.drawable.ic_favoritos_border)
            btnHeroFavorito.setColorFilter(ContextCompat.getColor(this, R.color.colorTextSecondary))
        }
    }

    private fun compartirDestino(destino: Destino) {
        val shareText = "Descubre ${destino.nombre} (${destino.departamento}, Perú) en PerúViajero.\n" +
            "Categoría: ${destino.categoria} • ★ ${destino.calificacion}\n" +
            "Tours desde S/ ${CotizadorCalculator.PRECIO_BASE_POR_PERSONA.toInt()} por persona."
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(sendIntent, "Compartir destino"))
    }
}
