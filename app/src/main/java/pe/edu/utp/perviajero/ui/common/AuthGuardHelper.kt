package pe.edu.utp.perviajero.ui.common

import android.content.Context
import android.content.Intent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pe.edu.utp.perviajero.data.model.RolUsuario
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.ui.LoginActivity

/**
 * Auth Gate contextual (Lazy Auth) que permite navegación pública libre en Modo Visitante
 * y solicita iniciar sesión únicamente cuando el usuario intenta acciones personales o transaccionales.
 */
object AuthGuardHelper {

    fun requireAuth(
        context: Context,
        titulo: String,
        motivo: String,
        onAuthenticated: () -> Unit
    ) {
        val sessionRepo = ServiceLocator.getSessionRepository(context)
        if (sessionRepo.isLoggedIn()) {
            onAuthenticated()
            return
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(titulo)
            .setMessage("$motivo\n\nPuedes ingresar con tu cuenta de Turista, como Administrador o usar acceso rápido.")
            .setPositiveButton("Iniciar Sesión") { _, _ ->
                context.startActivity(Intent(context, LoginActivity::class.java))
            }
            .setNeutralButton("Acceso Rápido Turista") { _, _ ->
                sessionRepo.login(sessionRepo.getQuickAccessUser(RolUsuario.CLIENTE))
                onAuthenticated()
            }
            .setNegativeButton("Seguir como Visitante", null)
            .show()
    }
}
