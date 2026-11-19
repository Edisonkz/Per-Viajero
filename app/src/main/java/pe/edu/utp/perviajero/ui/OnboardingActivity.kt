package pe.edu.utp.perviajero.ui

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import pe.edu.utp.perviajero.MainActivity
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.adapter.OnboardingAdapter
import pe.edu.utp.perviajero.data.model.OnboardingItem
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.util.WindowInsetsHelper
import kotlin.math.abs

/**
 * Experiencia de Bienvenida Épica (Onboarding) al iniciar la app por primera vez.
 * Cuenta con degradados vivos con morphing por diapositiva, parallax 3D, anillos orbitales
 * animados y entrada directa a Modo Visitante (o Login opcional en el paso final).
 */
class OnboardingActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FROM_DRAWER = "extra_onboarding_from_drawer"
    }

    private lateinit var gradientRoot: View
    private lateinit var contentContainer: View
    private lateinit var layoutHeader: View
    private lateinit var layoutFooter: View
    private lateinit var orbTop: View
    private lateinit var orbBottom: View
    private lateinit var tvStep: TextView
    private lateinit var viewPager: ViewPager2
    private lateinit var layoutPuntos: LinearLayout
    private lateinit var btnAccion: MaterialButton
    private lateinit var btnSaltar: MaterialButton
    private lateinit var btnLogin: MaterialButton

    private lateinit var adapter: OnboardingAdapter
    private lateinit var listaSlides: List<OnboardingItem>
    private val dotViews = mutableListOf<View>()
    private val argbEvaluator = ArgbEvaluator()
    private val backgroundGradient = GradientDrawable().apply {
        orientation = GradientDrawable.Orientation.TL_BR
    }

    private var fromDrawer: Boolean = false
    private var currentSelectedIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = ServiceLocator.getSessionRepository(this)
        AppCompatDelegate.setDefaultNightMode(
            if (prefs.isDarkMode()) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        @Suppress("DEPRECATION")
        window.navigationBarColor = Color.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        setContentView(R.layout.activity_onboarding)

        fromDrawer = intent.getBooleanExtra(EXTRA_FROM_DRAWER, false)

        gradientRoot = findViewById(R.id.onboardingGradientRoot)
        contentContainer = findViewById(R.id.layoutOnboardingContent)
        layoutHeader = findViewById(R.id.layoutOnboardingHeader)
        layoutFooter = findViewById(R.id.layoutOnboardingFooter)
        orbTop = findViewById(R.id.viewOnboardingOrbTop)
        orbBottom = findViewById(R.id.viewOnboardingOrbBottom)
        tvStep = findViewById(R.id.tvOnboardingStep)
        viewPager = findViewById(R.id.viewPagerOnboarding)
        layoutPuntos = findViewById(R.id.layoutIndicadorPuntos)
        btnAccion = findViewById(R.id.btnOnboardingAccion)
        btnSaltar = findViewById(R.id.btnOnboardingSaltar)
        btnLogin = findViewById(R.id.btnOnboardingLogin)

        WindowInsetsHelper.applySystemBarPadding(contentContainer, top = true, bottom = true)

        configurarSlides()
        aplicarDegradadoInterpolado(0, 0f)
        construirIndicadorPuntos()
        actualizarIndicadorPuntos(0, animate = false)
        actualizarEstadoBotones(0)
        animarEntradaEpica()

        viewPager.setPageTransformer { page, position ->
            val absPos = abs(position)
            val hero = page.findViewById<View>(R.id.layoutSlideHero)
            val haloOuter = page.findViewById<View>(R.id.viewSlideHaloOuter)
            val copy = page.findViewById<View>(R.id.layoutSlideCopy)
            val badge = page.findViewById<View>(R.id.tvOnboardingBadge)

            val scale = 1f - (absPos * 0.14f).coerceAtMost(0.24f)
            hero?.scaleX = scale
            hero?.scaleY = scale
            hero?.translationX = -position * page.width * 0.18f
            haloOuter?.translationX = -position * page.width * 0.32f
            badge?.translationX = position * page.width * 0.08f
            copy?.translationX = position * page.width * 0.14f
            copy?.alpha = (1f - absPos * 1.15f).coerceIn(0f, 1f)
            badge?.alpha = (1f - absPos * 1.25f).coerceIn(0f, 1f)
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                aplicarDegradadoInterpolado(position, positionOffset)
                orbTop.translationX = -(position + positionOffset) * 28f
                orbBottom.translationX = (position + positionOffset) * 24f
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentSelectedIndex = position
                actualizarIndicadorPuntos(position, animate = true)
                actualizarEstadoBotones(position)
            }
        })

        btnAccion.setOnClickListener {
            animarPulsoBoton(btnAccion)
            val posicionActual = viewPager.currentItem
            if (posicionActual < listaSlides.size - 1) {
                viewPager.setCurrentItem(posicionActual + 1, true)
            } else {
                completarOnboarding(abrirLogin = false)
            }
        }

        btnSaltar.setOnClickListener {
            completarOnboarding(abrirLogin = false)
        }

        btnLogin.setOnClickListener {
            completarOnboarding(abrirLogin = true)
        }
    }

    private fun configurarSlides() {
        listaSlides = listOf(
            OnboardingItem(
                badge = "¡BIENVENIDO A BORDO!",
                titulo = "Descubre la Magia del Perú",
                subtitulo = "Costa, Sierra y Selva en una sola experiencia",
                descripcion = "Explora libremente las maravillas del país desde el primer segundo, consulta el clima en vivo y descubre destinos auténticos sin barreras.",
                iconoResId = R.drawable.ic_logo_peruviajero_agency,
                highlights = listOf("24 Regiones", "Clima en Vivo", "Modo Libre"),
                accentColorHex = "#FFB300",
                gradientStartHex = "#007A42",
                gradientEndHex = "#002B15",
                tintIconWhite = false
            ),
            OnboardingItem(
                badge = "EXPLORACIÓN & RUTAS GPS",
                titulo = "Navega Como un Experto",
                subtitulo = "Mapas interactivos, brújula y guía IPERÚ",
                descripcion = "Filtra atractivos por Departamento y Provincia, traza tu ruta GPS en tiempo real, oriéntate con la brújula inteligente y guarda tus favoritos.",
                iconoResId = R.drawable.ic_map_route,
                highlights = listOf("Mapa GPS", "Brújula Real", "Guía IPERÚ"),
                accentColorHex = "#6EE7B7",
                gradientStartHex = "#0E6B62",
                gradientEndHex = "#062D2A",
                tintIconWhite = true
            ),
            OnboardingItem(
                badge = "RESERVA FORMAL CERTIFICADA",
                titulo = "Boleto Oficial con Código QR",
                subtitulo = "Pasarela SSL 256-bit y Manifiesto SUTRAN",
                descripcion = "Cotiza tu tour con transparencia total, importa acompañantes desde tus contactos, paga con Tarjeta, Yape o Plin y recibe tu Boarding Pass al instante.",
                iconoResId = R.drawable.ic_receipt,
                highlights = listOf("Pago SSL", "Boleto QR", "MINCETUR"),
                accentColorHex = "#7DD3FC",
                gradientStartHex = "#1B4B5A",
                gradientEndHex = "#0D2430",
                tintIconWhite = true
            ),
            OnboardingItem(
                badge = "COMUNIDAD & PLANIFICADOR",
                titulo = "Tu Viaje, a Tu Ritmo",
                subtitulo = "Itinerario semanal, reseñas y alertas Push",
                descripcion = "Organiza tus días con sugerencias curadas, comparte tus fotos con otros viajeros y recibe alertas turísticas oficiales en tu bandeja.",
                iconoResId = R.drawable.ic_calendar,
                highlights = listOf("Itinerario", "Comunidad", "Alertas Push"),
                accentColorHex = "#FCD34D",
                gradientStartHex = "#1B3B4B",
                gradientEndHex = "#0B1B24",
                tintIconWhite = true
            )
        )

        adapter = OnboardingAdapter(listaSlides)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1
    }

    private fun aplicarDegradadoInterpolado(position: Int, offset: Float) {
        val current = listaSlides[position.coerceIn(0, listaSlides.lastIndex)]
        val next = listaSlides[(position + 1).coerceIn(0, listaSlides.lastIndex)]

        val startA = Color.parseColor(current.gradientStartHex)
        val startB = Color.parseColor(next.gradientStartHex)
        val endA = Color.parseColor(current.gradientEndHex)
        val endB = Color.parseColor(next.gradientEndHex)

        val blendedStart = argbEvaluator.evaluate(offset, startA, startB) as Int
        val blendedEnd = argbEvaluator.evaluate(offset, endA, endB) as Int

        backgroundGradient.colors = intArrayOf(blendedStart, blendedEnd)
        gradientRoot.background = backgroundGradient
    }

    private fun construirIndicadorPuntos() {
        layoutPuntos.removeAllViews()
        dotViews.clear()
        val density = resources.displayMetrics.density
        val sizePx = (9 * density).toInt()
        val marginPx = (5 * density).toInt()

        repeat(listaSlides.size) {
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                    setMargins(marginPx, 0, marginPx, 0)
                }
                setBackgroundResource(R.drawable.bg_onboarding_dot_inactive)
            }
            dotViews.add(dot)
            layoutPuntos.addView(dot)
        }
    }

    private fun actualizarIndicadorPuntos(selected: Int, animate: Boolean) {
        val density = resources.displayMetrics.density
        val activeWidth = (30 * density).toInt()
        val inactiveWidth = (9 * density).toInt()

        dotViews.forEachIndexed { index, view ->
            val targetWidth = if (index == selected) activeWidth else inactiveWidth
            view.setBackgroundResource(
                if (index == selected) R.drawable.bg_onboarding_dot_active
                else R.drawable.bg_onboarding_dot_inactive
            )
            if (animate) {
                val startWidth = view.layoutParams.width
                ValueAnimator.ofInt(startWidth, targetWidth).apply {
                    duration = 260L
                    interpolator = DecelerateInterpolator()
                    addUpdateListener { anim ->
                        val lp = view.layoutParams
                        lp.width = anim.animatedValue as Int
                        view.layoutParams = lp
                    }
                    start()
                }
            } else {
                view.layoutParams = view.layoutParams.apply { width = targetWidth }
            }
        }
    }

    private fun actualizarEstadoBotones(position: Int) {
        tvStep.text = "Paso ${position + 1} de ${listaSlides.size}"
        val esUltimo = position == listaSlides.lastIndex
        if (esUltimo) {
            btnAccion.text = "¡Explorar Perú Ahora!"
            btnSaltar.visibility = View.INVISIBLE
            btnLogin.visibility = View.VISIBLE
            btnLogin.alpha = 0f
            btnLogin.animate().alpha(1f).setDuration(260L).start()
        } else {
            btnAccion.text = "Siguiente"
            btnSaltar.visibility = View.VISIBLE
            btnLogin.visibility = View.GONE
        }
    }

    private fun animarEntradaEpica() {
        layoutHeader.alpha = 0f
        layoutHeader.translationY = -24f
        viewPager.alpha = 0f
        viewPager.scaleX = 0.94f
        viewPager.scaleY = 0.94f
        layoutFooter.alpha = 0f
        layoutFooter.translationY = 44f

        val headerAnim = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(layoutHeader, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(layoutHeader, View.TRANSLATION_Y, -24f, 0f)
            )
            duration = 460L
            interpolator = DecelerateInterpolator()
        }

        val pagerAnim = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(viewPager, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(viewPager, View.SCALE_X, 0.94f, 1f),
                ObjectAnimator.ofFloat(viewPager, View.SCALE_Y, 0.94f, 1f)
            )
            duration = 560L
            startDelay = 90L
            interpolator = OvershootInterpolator(1.05f)
        }

        val footerAnim = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(layoutFooter, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(layoutFooter, View.TRANSLATION_Y, 44f, 0f)
            )
            duration = 500L
            startDelay = 160L
            interpolator = DecelerateInterpolator()
        }

        AnimatorSet().apply {
            playTogether(headerAnim, pagerAnim, footerAnim)
            start()
        }
    }

    private fun animarPulsoBoton(view: View) {
        view.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80L).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(120L).start()
        }.start()
    }

    private fun completarOnboarding(abrirLogin: Boolean) {
        val prefs = ServiceLocator.getSessionRepository(this)
        prefs.setFirstRunCompleted()

        if (fromDrawer && !abrirLogin) {
            finish()
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            return
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            if (abrirLogin) {
                putExtra(MainActivity.EXTRA_OPEN_LOGIN, true)
            }
            if (fromDrawer) {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        }
        startActivity(intent)
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
