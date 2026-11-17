package pe.edu.utp.perviajero

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import pe.edu.utp.perviajero.data.model.RolUsuario
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.ui.AdminDashboardFragment
import pe.edu.utp.perviajero.ui.AjustesFragment
import pe.edu.utp.perviajero.ui.BrujulaActivity
import pe.edu.utp.perviajero.ui.ComunidadActivity
import pe.edu.utp.perviajero.ui.DestinosFragment
import pe.edu.utp.perviajero.ui.FavoritosFragment
import pe.edu.utp.perviajero.ui.GuiaViajeFragment
import pe.edu.utp.perviajero.ui.MisReservasFragment
import pe.edu.utp.perviajero.ui.OnboardingActivity
import pe.edu.utp.perviajero.util.PermissionHelper

// Actividad principal con navegación adaptativa según rol (Turista / Administrador) y accesos rápidos
class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNavigation: BottomNavigationView

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Estado reflejado automáticamente en AjustesFragment */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        val sessionRepo = ServiceLocator.getSessionRepository(this)
        if (sessionRepo.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        if (sessionRepo.isFirstRun()) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        solicitarPermisoNotificacionesSiAplica()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_destinos -> {
                    toolbar.title = getString(R.string.app_name)
                    cambiarFragment(DestinosFragment())
                    true
                }
                R.id.navigation_guia -> {
                    toolbar.title = getString(R.string.nav_guia)
                    cambiarFragment(GuiaViajeFragment())
                    true
                }
                R.id.navigation_reservas -> {
                    val rol = sessionRepo.getRolActual()
                    if (rol == RolUsuario.ADMIN) {
                        toolbar.title = "Panel de Agencia"
                        cambiarFragment(AdminDashboardFragment())
                    } else {
                        toolbar.title = "Mis Reservas de Tour"
                        cambiarFragment(MisReservasFragment())
                    }
                    true
                }
                R.id.navigation_favoritos -> {
                    toolbar.title = getString(R.string.nav_favoritos)
                    cambiarFragment(FavoritosFragment())
                    true
                }
                R.id.navigation_ajustes -> {
                    toolbar.title = getString(R.string.nav_ajustes)
                    cambiarFragment(AjustesFragment())
                    true
                }
                else -> false
            }
        }

        actualizarNavegacionPorRol()

        if (savedInstanceState == null) {
            bottomNavigation.selectedItemId = R.id.navigation_destinos
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_comunidad -> {
                startActivity(Intent(this, ComunidadActivity::class.java))
                true
            }
            R.id.action_brujula -> {
                startActivity(Intent(this, BrujulaActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun solicitarPermisoNotificacionesSiAplica() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !PermissionHelper.hasNotificationPermission(this)
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun actualizarNavegacionPorRol() {
        val rol = ServiceLocator.getSessionRepository(this).getRolActual()
        val menuItem = bottomNavigation.menu.findItem(R.id.navigation_reservas)

        if (rol == RolUsuario.ADMIN) {
            menuItem?.title = "Agencia"
            menuItem?.setIcon(R.drawable.ic_dashboard)
        } else {
            menuItem?.title = "Reservas"
            menuItem?.setIcon(R.drawable.ic_receipt)
        }

        if (bottomNavigation.selectedItemId == R.id.navigation_reservas) {
            if (rol == RolUsuario.ADMIN) {
                toolbar.title = "Panel de Agencia"
                cambiarFragment(AdminDashboardFragment())
            } else {
                toolbar.title = "Mis Reservas de Tour"
                cambiarFragment(MisReservasFragment())
            }
        }
    }

    private fun cambiarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
