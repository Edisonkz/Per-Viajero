package pe.edu.utp.perviajero

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.RolUsuario
import pe.edu.utp.perviajero.network.ApiClient
import pe.edu.utp.perviajero.ui.AdminDashboardFragment
import pe.edu.utp.perviajero.ui.AjustesFragment
import pe.edu.utp.perviajero.ui.DestinosFragment
import pe.edu.utp.perviajero.ui.FavoritosFragment
import pe.edu.utp.perviajero.ui.GuiaViajeFragment
import pe.edu.utp.perviajero.ui.MisReservasFragment

// REQ-01 / REQ-02 / REQ-04: Actividad principal con navegación adaptativa según rol (Turista / Administrador)
class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        // REQ-10: Aplicar preferencia de tema guardada antes de inflar vistas
        val prefs = PreferencesHelper.getInstance(this)
        if (prefs.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // Si es el primer inicio, mostrar pantalla de bienvenida / Onboarding
        if (prefs.isFirstRun()) {
            startActivity(android.content.Intent(this, pe.edu.utp.perviajero.ui.OnboardingActivity::class.java))
            finish()
            return
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar cliente HTTP
        ApiClient.initialize(this)

        // Inicializar canal de notificaciones para reservas y despacho
        pe.edu.utp.perviajero.util.NotificationHelper.crearCanalNotificaciones(this)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // Configuración de Toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Configuración de BottomNavigationView
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
                    val rol = prefs.getRolActual()
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

        // Cargar fragment inicial (Destinos)
        if (savedInstanceState == null) {
            bottomNavigation.selectedItemId = R.id.navigation_destinos
        }
    }

    fun actualizarNavegacionPorRol() {
        val prefs = PreferencesHelper.getInstance(this)
        val rol = prefs.getRolActual()
        val menuItem = bottomNavigation.menu.findItem(R.id.navigation_reservas)

        if (rol == RolUsuario.ADMIN) {
            menuItem?.title = "Agencia"
            menuItem?.setIcon(R.drawable.ic_dashboard)
        } else {
            menuItem?.title = "Reservas"
            menuItem?.setIcon(R.drawable.ic_receipt)
        }

        // Si actualmente estamos en la pestaña de reservas, recargar el fragment adecuado
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