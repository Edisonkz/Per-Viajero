package pe.edu.utp.perviajero.ui

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import pe.edu.utp.perviajero.MainActivity
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.adapter.OnboardingAdapter
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.OnboardingItem

// Experiencia de Bienvenida / Onboarding en el primer inicio de la aplicación
class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var layoutPuntos: LinearLayout
    private lateinit var btnAccion: MaterialButton
    private lateinit var btnSaltar: MaterialButton
    private lateinit var adapter: OnboardingAdapter
    private lateinit var listaSlides: List<OnboardingItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = PreferencesHelper.getInstance(this)
        if (prefs.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.viewPagerOnboarding)
        layoutPuntos = findViewById(R.id.layoutIndicadorPuntos)
        btnAccion = findViewById(R.id.btnOnboardingAccion)
        btnSaltar = findViewById(R.id.btnOnboardingSaltar)

        configurarSlides()
        configurarIndicadorPuntos(0)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                configurarIndicadorPuntos(position)
                if (position == listaSlides.size - 1) {
                    btnAccion.text = "¡Comenzar Aventura!"
                    btnSaltar.visibility = android.view.View.INVISIBLE
                } else {
                    btnAccion.text = "Siguiente"
                    btnSaltar.visibility = android.view.View.VISIBLE
                }
            }
        })

        btnAccion.setOnClickListener {
            val posicionActual = viewPager.currentItem
            if (posicionActual < listaSlides.size - 1) {
                viewPager.currentItem = posicionActual + 1
            } else {
                completarOnboarding()
            }
        }

        btnSaltar.setOnClickListener {
            completarOnboarding()
        }
    }

    private fun configurarSlides() {
        listaSlides = listOf(
            OnboardingItem(
                badge = "¡BIENVENIDO A BORDO!",
                titulo = "Descubre la Magia del Perú",
                subtitulo = "Costa, Sierra y Selva en la palma de tu mano",
                descripcion = "Explora los atractivos turísticos más fascinantes del país, consulta clima en tiempo real, mapas de ruta e información oficial de viaje.",
                iconoResId = R.drawable.ic_logo_peruviajero_agency
            ),
            OnboardingItem(
                badge = "FAVORITOS & EXPLORACIÓN",
                titulo = "Encuentra tus Lugares Favoritos",
                subtitulo = "Guarda tus destinos y viaja seguro",
                descripcion = "Marca tus lugares imperdibles con un solo toque y elige guías turísticos acreditados por MINCETUR en español, quechua o inglés.",
                iconoResId = R.drawable.ic_favoritos
            ),
            OnboardingItem(
                badge = "RESERVAS FORMALES MTC",
                titulo = "Boletos y Embarque Seguro",
                subtitulo = "Manifiesto con DNI y terminales oficiales",
                descripcion = "Selecciona tu terminal de salida y horario de bus turístico. Registra a todos los pasajeros con su documento de identidad y descarga tu voucher digital.",
                iconoResId = R.drawable.ic_receipt
            ),
            OnboardingItem(
                badge = "PLANIFICADOR SEMANAL",
                titulo = "Organiza tu Semana con Calendario",
                subtitulo = "Sugerencias de expertos y actividades",
                descripcion = "Crea tu itinerario día por día, explora sugerencias exclusivas de lugares para cada día de la semana y monitorea tus actividades con el calendario interactivo.",
                iconoResId = R.drawable.ic_calendar
            )
        )

        adapter = OnboardingAdapter(listaSlides)
        viewPager.adapter = adapter
    }

    private fun configurarIndicadorPuntos(posicionSeleccionada: Int) {
        layoutPuntos.removeAllViews()
        val total = listaSlides.size

        for (i in 0 until total) {
            val dot = ImageView(this).apply {
                val params = LinearLayout.LayoutParams(
                    if (i == posicionSeleccionada) 28 else 10,
                    10
                ).apply {
                    setMargins(6, 0, 6, 0)
                }
                layoutParams = params
                setBackgroundResource(
                    if (i == posicionSeleccionada) R.drawable.bg_badge_categoria
                    else android.R.drawable.presence_invisible
                )
                if (i != posicionSeleccionada) {
                    setColorFilter(ContextCompat.getColor(this@OnboardingActivity, android.R.color.darker_gray))
                }
            }
            layoutPuntos.addView(dot)
        }
    }

    private fun completarOnboarding() {
        val prefs = PreferencesHelper.getInstance(this)
        prefs.setFirstRunCompleted()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

