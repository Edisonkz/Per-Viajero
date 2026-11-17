package pe.edu.utp.perviajero.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.util.WindowInsetsHelper

/**
 * Activity contenedora de ComunidadFragment para accesos directos filtrados desde DetalleDestinoActivity.
 */
class ComunidadActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FILTER_REGION = "extra_filter_region"
        const val EXTRA_FILTER_DESTINO_ID = "extra_filter_destino_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comunidad)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbarComunidad)
        WindowInsetsHelper.applyToolbarActivityInsets(this, toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        if (savedInstanceState == null) {
            val region = intent.getStringExtra(EXTRA_FILTER_REGION) ?: "Todas"
            val destinoId = intent.getStringExtra(EXTRA_FILTER_DESTINO_ID) ?: "ALL"
            supportFragmentManager.beginTransaction()
                .replace(R.id.comunidadFragmentHost, ComunidadFragment.newInstance(region, destinoId))
                .commit()
        }
    }
}
