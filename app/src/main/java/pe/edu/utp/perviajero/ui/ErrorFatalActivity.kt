package pe.edu.utp.perviajero.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import pe.edu.utp.perviajero.MainActivity
import pe.edu.utp.perviajero.R

/**
 * Pantalla de recuperación amigable cuando el manejador global captura una excepción no controlada.
 */
class ErrorFatalActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ERROR_DETAIL = "extra_error_detail"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error_fatal)

        val detail = intent.getStringExtra(EXTRA_ERROR_DETAIL) ?: "Se produjo un error inesperado."
        findViewById<TextView>(R.id.tvErrorFatalDetalle).text = detail

        findViewById<MaterialButton>(R.id.btnReiniciarApp).setOnClickListener {
            val restartIntent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(restartIntent)
            finish()
        }
    }
}
