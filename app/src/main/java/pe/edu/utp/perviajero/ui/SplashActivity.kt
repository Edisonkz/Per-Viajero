package pe.edu.utp.perviajero.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import pe.edu.utp.perviajero.MainActivity
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.di.ServiceLocator

/**
 * Pantalla de bienvenida épica (Launcher) con animación coreografiada del logo oficial PerúViajero.
 * Al finalizar dirige a OnboardingActivity (primera vez) o directo a MainActivity en Modo Visitante.
 */
class SplashActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var navigated = false

    private val navigateRunnable = Runnable {
        if (navigated || isFinishing) return@Runnable
        navigated = true
        val sessionRepo = ServiceLocator.getSessionRepository(this)
        val destino = if (sessionRepo.isFirstRun()) {
            Intent(this, OnboardingActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }
        startActivity(destino)
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val sessionRepo = ServiceLocator.getSessionRepository(this)
        AppCompatDelegate.setDefaultNightMode(
            if (sessionRepo.isDarkMode()) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        ejecutarAnimacionEpica()
        handler.postDelayed(navigateRunnable, 1850L)
    }

    override fun onDestroy() {
        handler.removeCallbacks(navigateRunnable)
        super.onDestroy()
    }

    private fun ejecutarAnimacionEpica() {
        val ivLogo: ImageView = findViewById(R.id.ivSplashLogo)
        val ringInner: View = findViewById(R.id.viewSplashRingInner)
        val ringOuter: View = findViewById(R.id.viewSplashRingOuter)
        val tvBrand: TextView = findViewById(R.id.tvSplashBrand)
        val accentBar: View = findViewById(R.id.viewSplashAccentBar)
        val tvSubtitle: TextView = findViewById(R.id.tvSplashSubtitle)
        val tvFooter: TextView = findViewById(R.id.tvSplashFooter)

        ivLogo.scaleX = 0.42f
        ivLogo.scaleY = 0.42f
        ringInner.scaleX = 0.5f
        ringInner.scaleY = 0.5f
        ringOuter.scaleX = 0.5f
        ringOuter.scaleY = 0.5f
        tvBrand.translationY = 40f
        tvSubtitle.translationY = 28f
        accentBar.scaleX = 0f

        val logoEntrance = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(ivLogo, View.SCALE_X, 0.42f, 1.0f),
                ObjectAnimator.ofFloat(ivLogo, View.SCALE_Y, 0.42f, 1.0f),
                ObjectAnimator.ofFloat(ivLogo, View.ALPHA, 0f, 1f)
            )
            duration = 680L
            interpolator = OvershootInterpolator(1.5f)
        }

        val ringsPulse = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(ringInner, View.SCALE_X, 0.55f, 1.25f),
                ObjectAnimator.ofFloat(ringInner, View.SCALE_Y, 0.55f, 1.25f),
                ObjectAnimator.ofFloat(ringInner, View.ALPHA, 0f, 0.75f, 0.2f),
                ObjectAnimator.ofFloat(ringOuter, View.SCALE_X, 0.55f, 1.55f),
                ObjectAnimator.ofFloat(ringOuter, View.SCALE_Y, 0.55f, 1.55f),
                ObjectAnimator.ofFloat(ringOuter, View.ALPHA, 0f, 0.45f, 0f)
            )
            duration = 1200L
            startDelay = 120L
            interpolator = DecelerateInterpolator()
        }

        val textReveal = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(tvBrand, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(tvBrand, View.TRANSLATION_Y, 40f, 0f),
                ObjectAnimator.ofFloat(accentBar, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(accentBar, View.SCALE_X, 0f, 1f),
                ObjectAnimator.ofFloat(tvSubtitle, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(tvSubtitle, View.TRANSLATION_Y, 28f, 0f),
                ObjectAnimator.ofFloat(tvFooter, View.ALPHA, 0f, 1f)
            )
            duration = 560L
            startDelay = 420L
            interpolator = DecelerateInterpolator()
        }

        val logoBreath = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(ivLogo, View.SCALE_X, 1f, 1.06f, 1f),
                ObjectAnimator.ofFloat(ivLogo, View.SCALE_Y, 1f, 1.06f, 1f)
            )
            duration = 520L
            startDelay = 1050L
        }

        AnimatorSet().apply {
            playTogether(logoEntrance, ringsPulse, textReveal, logoBreath)
            start()
        }
    }
}
