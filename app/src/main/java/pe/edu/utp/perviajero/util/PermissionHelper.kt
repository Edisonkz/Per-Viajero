package pe.edu.utp.perviajero.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Gestor centralizado de permisos en tiempo de ejecución (Cámara, Ubicación GPS y Notificaciones).
 * Implementa el flujo recomendado por Android: verificación -> rationale explicativo -> solicitud -> Ajustes.
 */
object PermissionHelper {

    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun shouldShowRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    fun showRationaleDialog(
        activity: Activity,
        title: String,
        message: String,
        onContinue: () -> Unit
    ) {
        MaterialAlertDialogBuilder(activity)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Continuar") { _, _ -> onContinue() }
            .setNegativeButton("Ahora no", null)
            .show()
    }

    fun showPermanentlyDeniedDialog(
        activity: Activity,
        title: String,
        message: String
    ) {
        MaterialAlertDialogBuilder(activity)
            .setTitle(title)
            .setMessage("$message\n\nPuedes habilitar el permiso manualmente en Ajustes de la aplicación.")
            .setPositiveButton("Ir a Ajustes") { _, _ ->
                openAppSettings(activity)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
