package pe.edu.utp.perviajero.util

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Gestor reutilizable de permisos en tiempo de ejecución con máquina de 4 estados:
 * CONCEDIDO, PENDIENTE, DENEGADO y BLOQUEADO (distinguiendo primer pedido de bloqueo permanente).
 */
class GestorPermisos(private val activity: AppCompatActivity) {

    enum class Estado {
        CONCEDIDO,
        PENDIENTE,
        DENEGADO,
        BLOQUEADO
    }

    private val prefs: SharedPreferences by lazy {
        activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun estadoDe(permisos: Array<String>): Estado {
        if (permisos.isEmpty()) return Estado.CONCEDIDO
        val concedido = permisos.any {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
        if (concedido) return Estado.CONCEDIDO

        val sePuedeExplicar = permisos.any {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
        }
        if (sePuedeExplicar) return Estado.DENEGADO

        return if (prefs.getBoolean(permisos[0], false)) Estado.BLOQUEADO else Estado.PENDIENTE
    }

    fun marcarPreguntado(permisos: Array<String>) {
        val editor = prefs.edit()
        permisos.forEach { editor.putBoolean(it, true) }
        editor.apply()
    }

    fun abrirAjustes() {
        activity.startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", activity.packageName, null)
            )
        )
    }

    fun ejecutarConPermiso(
        permisos: Array<String>,
        lanzador: ActivityResultLauncher<Array<String>>,
        motivo: String,
        accion: () -> Unit
    ) {
        when (estadoDe(permisos)) {
            Estado.CONCEDIDO -> accion()
            Estado.PENDIENTE -> lanzador.launch(permisos)
            Estado.DENEGADO -> MaterialAlertDialogBuilder(activity)
                .setTitle("¿Por qué lo necesitamos?")
                .setMessage(motivo)
                .setPositiveButton("Entendido, permitir") { _, _ -> lanzador.launch(permisos) }
                .setNegativeButton("Ahora no", null)
                .show()
            Estado.BLOQUEADO -> MaterialAlertDialogBuilder(activity)
                .setTitle("Permiso bloqueado")
                .setMessage("$motivo\n\nPuedes activarlo manualmente en Ajustes > Permisos de PerúViajero.")
                .setPositiveButton("Abrir ajustes") { _, _ -> abrirAjustes() }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    companion object {
        private const val PREFS_NAME = "gestor_permisos_peruviajero"
    }
}
