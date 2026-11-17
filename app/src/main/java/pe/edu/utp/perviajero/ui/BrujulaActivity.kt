package pe.edu.utp.perviajero.ui

import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.ui.view.CompassView
import pe.edu.utp.perviajero.util.CompassSensorManager
import pe.edu.utp.perviajero.util.GeoMath
import pe.edu.utp.perviajero.util.LocationHelper
import java.util.Locale
import kotlin.math.abs

/**
 * Brújula Turística Offline basada en Acelerómetro + Magnetómetro.
 * Calcula el Norte magnético en tiempo real y el rumbo hacia el destino elegido sin internet.
 */
class BrujulaActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DESTINO = "extra_destino_brujula"
        private const val FALLBACK_LIMA_LAT = -12.0464
        private const val FALLBACK_LIMA_LNG = -77.0428
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvGrados: TextView
    private lateinit var tvPrecision: TextView
    private lateinit var compassView: CompassView
    private lateinit var spDestino: Spinner
    private lateinit var tvRumboDestino: TextView
    private lateinit var tvConsejoGiro: TextView
    private lateinit var tvSensoresRaw: TextView

    private lateinit var compassManager: CompassSensorManager
    private lateinit var locationHelper: LocationHelper

    private var destinos: List<Destino> = emptyList()
    private var selectedDestino: Destino? = null
    private var currentOrigin: Pair<Double, Double> = FALLBACK_LIMA_LAT to FALLBACK_LIMA_LNG

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brujula)

        selectedDestino = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_DESTINO, Destino::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_DESTINO) as? Destino
        }

        locationHelper = LocationHelper(this)
        inicializarVistas()
        cargarOrigenOfflineYDestinos()
        configurarSensores()
    }

    override fun onResume() {
        super.onResume()
        compassManager.start()
    }

    override fun onPause() {
        super.onPause()
        compassManager.stop()
    }

    private fun inicializarVistas() {
        toolbar = findViewById(R.id.toolbarBrujula)
        pe.edu.utp.perviajero.util.WindowInsetsHelper.applyToolbarActivityInsets(this, toolbar)
        tvGrados = findViewById(R.id.tvBrujulaGrados)
        tvPrecision = findViewById(R.id.tvBrujulaPrecision)
        compassView = findViewById(R.id.compassView)
        spDestino = findViewById(R.id.spBrujulaDestino)
        tvRumboDestino = findViewById(R.id.tvBrujulaRumboDestino)
        tvConsejoGiro = findViewById(R.id.tvBrujulaConsejoGiro)
        tvSensoresRaw = findViewById(R.id.tvBrujulaSensoresRaw)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun cargarOrigenOfflineYDestinos() {
        val sessionRepo = ServiceLocator.getSessionRepository(this)
        currentOrigin = sessionRepo.getLastLocation() ?: (FALLBACK_LIMA_LAT to FALLBACK_LIMA_LNG)

        // Intentar actualizar con GPS en segundo plano si tiene permiso
        locationHelper.obtainCurrentLocation { loc ->
            if (loc != null) {
                currentOrigin = loc.latitude to loc.longitude
            }
        }

        val cached = ServiceLocator.getDestinoRepository(this).obtenerDestinosEnCache()
        destinos = if (cached.isNotEmpty()) {
            cached
        } else {
            listOfNotNull(selectedDestino).ifEmpty {
                listOf(
                    Destino(
                        id = "1",
                        nombre = "Santuario Histórico de Machu Picchu",
                        departamento = "Cusco",
                        categoria = "Arqueología",
                        descripcion = "",
                        calificacion = 4.9,
                        imageUrl = "",
                        latitud = -13.1631,
                        longitud = -72.5450
                    )
                )
            }
        }

        val names = destinos.map { "${it.nombre} (${it.departamento})" }
        spDestino.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)

        val preIndex = selectedDestino?.let { target ->
            destinos.indexOfFirst { it.id == target.id }
        }?.takeIf { it >= 0 } ?: 0
        spDestino.setSelection(preIndex)
        selectedDestino = destinos.getOrNull(preIndex)

        spDestino.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedDestino = destinos.getOrNull(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun configurarSensores() {
        compassManager = CompassSensorManager(
            context = this,
            onReadingChanged = { reading ->
                val dest = selectedDestino
                var targetBearing: Float? = null

                if (dest?.latitud != null && dest.longitud != null) {
                    val bearing = GeoMath.calculateBearingDegrees(
                        currentOrigin.first,
                        currentOrigin.second,
                        dest.latitud,
                        dest.longitud
                    )
                    val distKm = GeoMath.haversineDistanceKm(
                        currentOrigin.first,
                        currentOrigin.second,
                        dest.latitud,
                        dest.longitud
                    )
                    targetBearing = bearing
                    tvRumboDestino.text = String.format(
                        Locale.US,
                        "Rumbo a %s: %.0f° %s • Distancia: %s",
                        dest.nombre,
                        bearing,
                        GeoMath.cardinalPoint(bearing),
                        GeoMath.formatDistance(distKm)
                    )

                    val turn = GeoMath.relativeTurnAngle(reading.azimuthDegrees, bearing)
                    tvConsejoGiro.text = when {
                        abs(turn) <= 8f -> "Alineado: estás apuntando hacia el destino"
                        turn > 0 -> String.format(Locale.US, "Gira %.0f° a la derecha hacia el marcador verde", turn)
                        else -> String.format(Locale.US, "Gira %.0f° a la izquierda hacia el marcador verde", abs(turn))
                    }
                }

                compassView.updateOrientation(reading.azimuthDegrees, targetBearing)
                tvGrados.text = String.format(Locale.US, "%03.0f° %s", reading.azimuthDegrees, reading.cardinal)

                val precisionLabel = when (reading.accuracy) {
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "Alta"
                    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Media"
                    SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Baja (mueve el teléfono en 8 para calibrar)"
                    else -> "Sin calibrar"
                }
                tvPrecision.text = "Precisión del sensor: $precisionLabel"

                tvSensoresRaw.text = String.format(
                    Locale.US,
                    "Acelerómetro: X=%.2f  Y=%.2f  Z=%.2f m/s²\nMagnetómetro: X=%.2f  Y=%.2f  Z=%.2f µT",
                    reading.accelValues[0], reading.accelValues[1], reading.accelValues[2],
                    reading.magnetValues[0], reading.magnetValues[1], reading.magnetValues[2]
                )
            },
            onSensorsUnavailable = {
                tvPrecision.text = "Este dispositivo/emulador no reporta magnetómetro o acelerómetro físico."
                Toast.makeText(this, "Sensores de brújula no disponibles en este hardware", Toast.LENGTH_LONG).show()
            }
        )
    }
}
