package pe.edu.utp.perviajero.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.model.Destino

// Pantalla para consultar cómo llegar, coordenadas geográficas e Intent Implícito a Google Maps
class RutaDestinoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DESTINO = "extra_destino_ruta"
    }

    private lateinit var toolbarRuta: MaterialToolbar
    private lateinit var tvRutaNombre: TextView
    private lateinit var tvRutaDepartamento: TextView
    private lateinit var tvRutaCoordenadas: TextView
    private lateinit var tvTransporteAereo: TextView
    private lateinit var tvTransporteTerrestre: TextView
    private lateinit var tvAccesoLocal: TextView
    private lateinit var tvRecomendacionRuta: TextView
    private lateinit var btnAbrirGoogleMaps: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ruta_destino)

        toolbarRuta = findViewById(R.id.toolbarRuta)
        tvRutaNombre = findViewById(R.id.tvRutaNombre)
        tvRutaDepartamento = findViewById(R.id.tvRutaDepartamento)
        tvRutaCoordenadas = findViewById(R.id.tvRutaCoordenadas)
        tvTransporteAereo = findViewById(R.id.tvTransporteAereo)
        tvTransporteTerrestre = findViewById(R.id.tvTransporteTerrestre)
        tvAccesoLocal = findViewById(R.id.tvAccesoLocal)
        tvRecomendacionRuta = findViewById(R.id.tvRecomendacionRuta)
        btnAbrirGoogleMaps = findViewById(R.id.btnAbrirGoogleMaps)

        setSupportActionBar(toolbarRuta)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbarRuta.setNavigationOnClickListener {
            finish()
        }

        val destino = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_DESTINO, Destino::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_DESTINO) as? Destino
        }

        if (destino != null) {
            mostrarInfoRuta(destino)
        } else {
            finish()
        }
    }

    private fun mostrarInfoRuta(destino: Destino) {
        tvRutaNombre.text = destino.nombre
        tvRutaDepartamento.text = "${destino.departamento}, Perú"

        val lat = destino.latitud ?: -9.19
        val lng = destino.longitud ?: -75.0152
        tvRutaCoordenadas.text = "$lat, $lng"

        // Datos de transporte según la región del Perú
        when (destino.departamento.lowercase()) {
            "cusco" -> {
                tvTransporteAereo.text = "Vuelos diarios desde Lima al Aeropuerto Internacional Alejandro Velasco Astete (aprox. 1h 15 min)."
                tvTransporteTerrestre.text = "Buses interprovinciales por la Panamericana Sur y Abancay (aprox. 20 a 22 horas de viaje)."
                tvAccesoLocal.text = "Para Machu Picchu: Tren desde Ollantaytambo hacia Aguas Calientes (Machu Picchu Pueblo) y bus Consettur a la ciudadela."
                tvRecomendacionRuta.text = "Reserva boletos de tren y entrada al santuario con meses de anticipación. Lleva pastillas para la altura y calzado antideslizante."
            }
            "arequipa" -> {
                tvTransporteAereo.text = "Vuelos regulares al Aeropuerto Rodríguez Ballón (1h 30 min desde Lima)."
                tvTransporteTerrestre.text = "Buses por la carretera Panamericana Sur (aprox. 14 a 16 horas)."
                tvAccesoLocal.text = "Tours y colectivos hacia Chivay y la Cruz del Cóndor en el Cañón del Colca (3 a 4 horas desde la ciudad de Arequipa)."
                tvRecomendacionRuta.text = "El mirador de la Cruz del Cóndor se visita muy temprano (6:30 a. m. a 9:00 a. m.) para observar el vuelo de las aves."
            }
            "ica" -> {
                tvTransporteAereo.text = "No cuenta con vuelos comerciales regulares de pasajeros. Sobrevuelos turísticos en Nazca e Ica."
                tvTransporteTerrestre.text = "Buses directos desde Lima por la Panamericana Sur (4 a 5 horas a Ica, 7 horas a Nazca)."
                tvAccesoLocal.text = "Taxis desde la ciudad de Ica al Oasis de Huacachina (10 minutos) o carros tubulares para el desierto."
                tvRecomendacionRuta.text = "Lleva abundante protector solar, gafas oscuras y agua. La mejor hora para el sandboarding es al atardecer."
            }
            "loreto" -> {
                tvTransporteAereo.text = "Vuelos directos desde Lima al Aeropuerto Coronel FAP Francisco Secada Vignetta de Iquitos (aprox. 1h 45 min)."
                tvTransporteTerrestre.text = "Iquitos no tiene acceso por carretera; el ingreso es exclusivamente aéreo o fluvial."
                tvAccesoLocal.text = "Navegación en lanchas rápidas y peke-pekes por el río Amazonas hacia los albergues y reservas naturales."
                tvRecomendacionRuta.text = "Vacunarse contra la fiebre amarilla con 10 días de anticipación. Llevar ropa fresca manga larga y repelente de insectos."
            }
            "puno" -> {
                tvTransporteAereo.text = "Vuelos hacia el Aeropuerto Inca Manco Cápac de Juliaca (1h 40 min), luego combi a Puno (45 min)."
                tvTransporteTerrestre.text = "Buses desde Arequipa (6h) o desde Cusco (7h con la ruta del Sol)."
                tvAccesoLocal.text = "Embarcaciones turísticas desde el puerto lacustre de Puno hacia las islas flotantes de los Uros y Taquile."
                tvRecomendacionRuta.text = "El lago está a 3,812 m s. n. m.; noche con temperaturas bajo cero en invierno. Llevar ropa térmica."
            }
            else -> {
                tvTransporteAereo.text = "Vuelos comerciales al aeropuerto departamental más cercano o escalas desde el Aeropuerto Jorge Chávez de Lima."
                tvTransporteTerrestre.text = "Transporte terrestre mediante agencias interprovinciales por la Red Vial Nacional."
                tvAccesoLocal.text = "Taxis autorizados, colectivos y transporte turístico local hasta la entrada del atractivo."
                tvRecomendacionRuta.text = "Consultar el estado del clima antes de salir y llevar identificación (DNI o Pasaporte) para el ingreso a áreas protegidas."
            }
        }

        // Intent Implícito para abrir mapa o navegación GPS
        btnAbrirGoogleMaps.setOnClickListener {
            val label = Uri.encode(destino.nombre)
            val gmmIntentUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

            try {
                startActivity(mapIntent)
            } catch (e: Exception) {
                // Fallback a URL web de Google Maps si no hay app nativa de mapas instalada
                val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
                val webIntent = Intent(Intent.ACTION_VIEW, webUri)
                startActivity(webIntent)
            }
        }
    }
}
