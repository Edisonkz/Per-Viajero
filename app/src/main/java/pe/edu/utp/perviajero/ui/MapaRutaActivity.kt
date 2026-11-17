package pe.edu.utp.perviajero.ui

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.util.GeoMath
import pe.edu.utp.perviajero.util.LocationHelper
import pe.edu.utp.perviajero.util.PermissionHelper
import java.util.Locale

/**
 * Mapa interactivo embebido con trazado de ruta desde la ubicación GPS actual hasta el destino turístico.
 */
class MapaRutaActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DESTINO = "extra_destino_mapa"
        private const val FALLBACK_LIMA_LAT = -12.0464
        private const val FALLBACK_LIMA_LNG = -77.0428
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var mapView: MapView
    private lateinit var tvEstadoRuta: TextView
    private lateinit var tvDestinoNombre: TextView
    private lateinit var tvDistancia: TextView
    private lateinit var tvTiempo: TextView
    private lateinit var tvCoordenadasOrigen: TextView
    private lateinit var fabRecentrar: FloatingActionButton
    private lateinit var btnAbrirBrujula: MaterialButton
    private lateinit var btnGoogleMaps: MaterialButton

    private var destino: Destino? = null
    private lateinit var locationHelper: LocationHelper

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            cargarUbicacionYTrazarRuta()
        } else {
            if (!PermissionHelper.shouldShowRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                PermissionHelper.showPermanentlyDeniedDialog(
                    this,
                    "Permiso de Ubicación GPS",
                    "Habilita la ubicación para trazar la ruta exacta desde tu posición actual."
                )
            }
            trazarConUbicacionReferencial("Permiso GPS denegado • Usando Lima como referencia")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_mapa_ruta)

        destino = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_DESTINO, Destino::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_DESTINO) as? Destino
        }

        if (destino == null) {
            finish()
            return
        }

        locationHelper = LocationHelper(this)
        inicializarVistas()
        configurarEventos()
        verificarPermisoYCalcularRuta()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    private fun inicializarVistas() {
        toolbar = findViewById(R.id.toolbarMapaRuta)
        pe.edu.utp.perviajero.util.WindowInsetsHelper.applyToolbarActivityInsets(this, toolbar)
        mapView = findViewById(R.id.mapViewRuta)
        tvEstadoRuta = findViewById(R.id.tvMapaEstadoRuta)
        tvDestinoNombre = findViewById(R.id.tvMapaDestinoNombre)
        tvDistancia = findViewById(R.id.tvMapaDistancia)
        tvTiempo = findViewById(R.id.tvMapaTiempo)
        tvCoordenadasOrigen = findViewById(R.id.tvMapaCoordenadasOrigen)
        fabRecentrar = findViewById(R.id.fabRecentrarmapa)
        btnAbrirBrujula = findViewById(R.id.btnMapaAbrirBrujula)
        btnGoogleMaps = findViewById(R.id.btnMapaGoogleMaps)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(6.5)

        val dest = destino!!
        tvDestinoNombre.text = "${dest.nombre} (${dest.departamento})"
    }

    private fun configurarEventos() {
        val dest = destino ?: return

        fabRecentrar.setOnClickListener {
            verificarPermisoYCalcularRuta()
        }

        btnAbrirBrujula.setOnClickListener {
            val intent = Intent(this, BrujulaActivity::class.java).apply {
                putExtra(BrujulaActivity.EXTRA_DESTINO, dest)
            }
            startActivity(intent)
        }

        btnGoogleMaps.setOnClickListener {
            val lat = dest.latitud ?: -13.1631
            val lng = dest.longitud ?: -72.5450
            val label = Uri.encode(dest.nombre)
            val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)")
            try {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (_: Exception) {
                val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
                startActivity(Intent(Intent.ACTION_VIEW, webUri))
            }
        }
    }

    private fun verificarPermisoYCalcularRuta() {
        when {
            PermissionHelper.hasLocationPermission(this) -> cargarUbicacionYTrazarRuta()
            PermissionHelper.shouldShowRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) -> {
                PermissionHelper.showRationaleDialog(
                    this,
                    "Acceso al GPS del Dispositivo",
                    "Necesitamos tu ubicación actual para medir la distancia real y trazar la ruta en el mapa hasta tu destino."
                ) {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
            else -> {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun cargarUbicacionYTrazarRuta() {
        tvEstadoRuta.text = "Obteniendo señal GPS..."
        locationHelper.obtainCurrentLocation { loc ->
            if (loc != null) {
                val tag = if (loc.isFallbackCached) "Último GPS guardado" else "GPS en tiempo real"
                dibujarMapaYRuta(loc.latitude, loc.longitude, tag)
            } else {
                trazarConUbicacionReferencial("GPS sin fijación • Referencia Lima")
            }
        }
    }

    private fun trazarConUbicacionReferencial(etiqueta: String) {
        val cached = ServiceLocator.getSessionRepository(this).getLastLocation()
        if (cached != null) {
            dibujarMapaYRuta(cached.first, cached.second, "Último GPS guardado")
        } else {
            dibujarMapaYRuta(FALLBACK_LIMA_LAT, FALLBACK_LIMA_LNG, etiqueta)
        }
    }

    private fun dibujarMapaYRuta(originLat: Double, originLng: Double, originLabel: String) {
        val dest = destino ?: return
        val destLat = dest.latitud ?: -13.1631
        val destLng = dest.longitud ?: -72.5450

        tvCoordenadasOrigen.text = String.format(
            Locale.US,
            "%s: %.4f, %.4f",
            originLabel,
            originLat,
            originLng
        )

        ServiceLocator.getRoutingService().calculateRoute(
            originLat = originLat,
            originLng = originLng,
            destLat = destLat,
            destLng = destLng
        ) { route ->
            runOnUiThread {
                renderizarEnMapa(originLat, originLng, destLat, destLng, dest.nombre, route.points, route.isRoadRoute)
                tvDistancia.text = "Distancia: ${GeoMath.formatDistance(route.distanceKm)}"
                tvTiempo.text = "Est.: ${GeoMath.formatEstimatedTravelTime(route.distanceKm, route.estimatedMinutes)}"
                tvEstadoRuta.text = if (route.isRoadRoute) {
                    "Ruta vial trazada por GPS"
                } else {
                    "Ruta geodésica directa (Offline / Tramo libre)"
                }
            }
        }
    }

    private fun renderizarEnMapa(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double,
        destName: String,
        points: List<GeoPoint>,
        isRoadRoute: Boolean
    ) {
        mapView.overlays.clear()

        val line = Polyline(mapView).apply {
            setPoints(points)
            outlinePaint.color = if (isRoadRoute) {
                ContextCompat.getColor(this@MapaRutaActivity, R.color.colorPrimary)
            } else {
                ContextCompat.getColor(this@MapaRutaActivity, R.color.colorAccent)
            }
            outlinePaint.strokeWidth = 10f
        }
        mapView.overlays.add(line)

        val originMarker = Marker(mapView).apply {
            position = GeoPoint(originLat, originLng)
            title = "Tu ubicación"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        val destMarker = Marker(mapView).apply {
            position = GeoPoint(destLat, destLng)
            title = destName
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        mapView.overlays.add(originMarker)
        mapView.overlays.add(destMarker)

        val box = BoundingBox.fromGeoPoints(points)
        mapView.post {
            try {
                mapView.zoomToBoundingBox(box, true, 120)
            } catch (_: Exception) {
                mapView.controller.setCenter(GeoPoint(destLat, destLng))
                mapView.controller.setZoom(9.0)
            }
        }
        mapView.invalidate()
    }
}
