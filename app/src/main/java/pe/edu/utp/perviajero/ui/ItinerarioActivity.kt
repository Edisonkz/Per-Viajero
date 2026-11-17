package pe.edu.utp.perviajero.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.util.WindowInsetsHelper

/**
 * Contenedor de Actividad para ItinerarioFragment al abrir desde DetalleDestinoActivity o el Menú Lateral.
 */
class ItinerarioActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_LUGAR_INICIAL = "extra_lugar_inicial"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_itinerario)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbarItinerario)
        WindowInsetsHelper.applyToolbarActivityInsets(this, toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        if (savedInstanceState == null) {
            val lugarInicial = intent.getStringExtra(EXTRA_LUGAR_INICIAL).orEmpty()
            supportFragmentManager.beginTransaction()
                .replace(R.id.itinerarioFragmentHost, ItinerarioFragment.newInstance(lugarInicial))
                .commit()
        }
    }
}
