package pe.edu.utp.perviajero.ui

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.util.WindowInsetsHelper

/**
 * Contenedor de Actividad para abrir Guía del Viajero e IPERÚ desde el Menú Lateral o Accesos Rápidos.
 */
class GuiaViajeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolbar = MaterialToolbar(this).apply {
            title = "Guía Práctica e IPERÚ"
            setBackgroundColor(getColor(R.color.colorPrimary))
            setTitleTextColor(android.graphics.Color.WHITE)
            setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            setNavigationIconTint(android.graphics.Color.WHITE)
            setNavigationOnClickListener { finish() }
        }

        val container = FrameLayout(this).apply {
            id = android.view.View.generateViewId()
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        root.addView(toolbar)
        root.addView(container)
        setContentView(root)
        WindowInsetsHelper.applyToolbarActivityInsets(this, toolbar)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(container.id, GuiaViajeFragment())
                .commit()
        }
    }
}
