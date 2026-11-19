package pe.edu.utp.perviajero

import android.Manifest
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.switchmaterial.SwitchMaterial
import pe.edu.utp.perviajero.ui.view.DrawerHeaderBgView
import pe.edu.utp.perviajero.data.model.RolUsuario
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.ui.AdminCatalogoFragment
import pe.edu.utp.perviajero.ui.AdminDashboardFragment
import pe.edu.utp.perviajero.ui.AjustesFragment
import pe.edu.utp.perviajero.ui.BrujulaActivity
import pe.edu.utp.perviajero.ui.ComunidadFragment
import pe.edu.utp.perviajero.ui.DestinosFragment
import pe.edu.utp.perviajero.ui.DiagnosticoLogsActivity
import pe.edu.utp.perviajero.ui.FavoritosFragment
import pe.edu.utp.perviajero.ui.GuiaViajeActivity
import pe.edu.utp.perviajero.ui.ItinerarioFragment
import pe.edu.utp.perviajero.ui.LoginActivity
import pe.edu.utp.perviajero.ui.MapaRutaActivity
import pe.edu.utp.perviajero.ui.MisReservasFragment
import pe.edu.utp.perviajero.ui.OnboardingActivity
import pe.edu.utp.perviajero.util.PermissionHelper

/**
 * Actividad principal con arquitectura híbrida:
 * - Menú Lateral (DrawerLayout + NavigationView) para herramientas, perfil, rol, tema y diagnóstico.
 * - Barra inferior de 5 menús con arco central en media luna para Mis Reservas.
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    enum class MainTab {
        EXPLORAR, COMUNIDAD, RESERVAS, ITINERARIO, FAVORITOS, PERFIL
    }

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var drawerToggle: ActionBarDrawerToggle

    // Barra inferior de 5 tabs
    private lateinit var navTabExplorar: View
    private lateinit var navTabComunidad: View
    private lateinit var navTabReservasLabelArea: View
    private lateinit var btnNavCenterReservas: MaterialCardView
    private lateinit var navTabItinerario: View
    private lateinit var navTabFavoritos: View

    private lateinit var ivNavExplorar: ImageView
    private lateinit var ivNavComunidad: ImageView
    private lateinit var ivNavCenterReservas: ImageView
    private lateinit var ivNavItinerario: ImageView
    private lateinit var ivNavFavoritos: ImageView

    private lateinit var tvNavExplorar: TextView
    private lateinit var tvNavComunidad: TextView
    private lateinit var tvNavReservas: TextView
    private lateinit var tvNavItinerario: TextView
    private lateinit var tvNavFavoritos: TextView

    private var currentTab: MainTab = MainTab.EXPLORAR

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

        inicializarVistas()
        aplicarWindowInsets()
        configurarDrawer()
        configurarBarraInferior()
        actualizarDrawerHeaderYMenu()
        actualizarNavegacionPorRol()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else if (currentTab != MainTab.EXPLORAR) {
                    seleccionarTab(MainTab.EXPLORAR)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        if (savedInstanceState == null) {
            seleccionarTab(MainTab.EXPLORAR)
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarDrawerHeaderYMenu()
        actualizarNavegacionPorRol()
    }

    private fun inicializarVistas() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        navTabExplorar = findViewById(R.id.navTabExplorar)
        navTabComunidad = findViewById(R.id.navTabComunidad)
        navTabReservasLabelArea = findViewById(R.id.navTabReservasLabelArea)
        btnNavCenterReservas = findViewById(R.id.btnNavCenterReservas)
        navTabItinerario = findViewById(R.id.navTabItinerario)
        navTabFavoritos = findViewById(R.id.navTabFavoritos)

        ivNavExplorar = findViewById(R.id.ivNavExplorar)
        ivNavComunidad = findViewById(R.id.ivNavComunidad)
        ivNavCenterReservas = findViewById(R.id.ivNavCenterReservas)
        ivNavItinerario = findViewById(R.id.ivNavItinerario)
        ivNavFavoritos = findViewById(R.id.ivNavFavoritos)

        tvNavExplorar = findViewById(R.id.tvNavExplorar)
        tvNavComunidad = findViewById(R.id.tvNavComunidad)
        tvNavReservas = findViewById(R.id.tvNavReservas)
        tvNavItinerario = findViewById(R.id.tvNavItinerario)
        tvNavFavoritos = findViewById(R.id.tvNavFavoritos)
    }

    private fun aplicarWindowInsets() {
        val density = resources.displayMetrics.density
        val mainRoot: View = findViewById(R.id.main)
        val fragmentContainer: View = findViewById(R.id.fragmentContainer)
        val bottomBarContainer: View = findViewById(R.id.bottomBarContainer)

        val typedArray = theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
        val baseActionBarHeight = typedArray.getDimensionPixelSize(0, (56 * density).toInt())
        typedArray.recycle()

        val initialToolbarLeft = toolbar.paddingLeft
        val initialToolbarRight = toolbar.paddingRight
        val initialToolbarBottom = toolbar.paddingBottom

        // Esquinas derechas redondeadas del DrawerLayout coincidiendo con el diseño mockup
        val drawerRadius = 28f * density
        val drawerBg = MaterialShapeDrawable(
            ShapeAppearanceModel.builder()
                .setTopRightCorner(CornerFamily.ROUNDED, drawerRadius)
                .setBottomRightCorner(CornerFamily.ROUNDED, drawerRadius)
                .build()
        ).apply {
            fillColor = ColorStateList.valueOf(ContextCompat.getColor(this@MainActivity, R.color.colorCardBackground))
        }
        navigationView.background = drawerBg
        navigationView.clipToOutline = true

        val headerView = navigationView.getHeaderView(0)
        val drawerHeaderBg = headerView.findViewById<DrawerHeaderBgView>(R.id.drawerHeaderBg)
        val drawerHeaderContent = headerView.findViewById<View>(R.id.drawerHeaderContent)
        val hdrLeft = drawerHeaderContent.paddingLeft
        val hdrRight = drawerHeaderContent.paddingRight
        val hdrBottom = drawerHeaderContent.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(mainRoot) { _, windowInsets ->
            val bars = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )

            // Toolbar superior con espacio para status bar y cámara frontal
            toolbar.layoutParams = toolbar.layoutParams.apply {
                height = baseActionBarHeight + bars.top
            }
            toolbar.setPadding(initialToolbarLeft, bars.top, initialToolbarRight, initialToolbarBottom)

            // Barra inferior curva elevada sobre botones del sistema (3 botones o gestos)
            bottomBarContainer.layoutParams = bottomBarContainer.layoutParams.apply {
                height = (84 * density).toInt() + bars.bottom
            }
            bottomBarContainer.setPadding(0, 0, 0, bars.bottom)

            // Ajuste de margen inferior del área de fragments
            (fragmentContainer.layoutParams as? ViewGroup.MarginLayoutParams)?.let { lp ->
                lp.bottomMargin = (64 * density).toInt() + bars.bottom
                fragmentContainer.layoutParams = lp
            }

            // Cabecera del Drawer con curva envolvente alrededor del notch/cámara
            drawerHeaderBg?.setTopInsetPx(bars.top)
            drawerHeaderContent?.setPadding(hdrLeft, (14 * density).toInt() + bars.top, hdrRight, hdrBottom)
            navigationView.setPadding(0, 0, 0, bars.bottom)

            windowInsets
        }
        ViewCompat.requestApplyInsets(mainRoot)
    }

    private fun configurarDrawer() {
        drawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.app_name,
            R.string.app_name
        )
        drawerToggle.drawerArrowDrawable.color = ContextCompat.getColor(this, android.R.color.white)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)
    }

    private fun configurarBarraInferior() {
        navTabExplorar.setOnClickListener { seleccionarTab(MainTab.EXPLORAR) }
        navTabComunidad.setOnClickListener { seleccionarTab(MainTab.COMUNIDAD) }
        btnNavCenterReservas.setOnClickListener { seleccionarTab(MainTab.RESERVAS) }
        navTabReservasLabelArea.setOnClickListener { seleccionarTab(MainTab.RESERVAS) }
        navTabItinerario.setOnClickListener { seleccionarTab(MainTab.ITINERARIO) }
        navTabFavoritos.setOnClickListener { seleccionarTab(MainTab.FAVORITOS) }
    }

    fun seleccionarTab(tab: MainTab) {
        currentTab = tab
        val sessionRepo = ServiceLocator.getSessionRepository(this)
        val rol = sessionRepo.getRolActual()

        when (tab) {
            MainTab.EXPLORAR -> {
                toolbar.title = getString(R.string.app_name)
                cambiarFragment(DestinosFragment())
            }
            MainTab.COMUNIDAD -> {
                toolbar.title = "Comunidad"
                cambiarFragment(ComunidadFragment())
            }
            MainTab.RESERVAS -> {
                if (esModoAdmin()) {
                    toolbar.title = "Panel de Agencia"
                    cambiarFragment(AdminDashboardFragment())
                } else {
                    toolbar.title = "Mis Reservas"
                    cambiarFragment(MisReservasFragment())
                }
            }
            MainTab.ITINERARIO -> {
                if (esModoAdmin()) {
                    toolbar.title = "Catálogo Turístico (Admin)"
                    cambiarFragment(AdminCatalogoFragment())
                } else {
                    toolbar.title = "Mi Itinerario"
                    cambiarFragment(ItinerarioFragment())
                }
            }
            MainTab.FAVORITOS -> {
                toolbar.title = "Mis Favoritos"
                cambiarFragment(FavoritosFragment())
            }
            MainTab.PERFIL -> {
                toolbar.title = "Mi Perfil y Permisos"
                cambiarFragment(AjustesFragment())
            }
        }
        actualizarEstilosBarraInferior(tab)
    }

    private fun esModoAdmin(): Boolean {
        val repo = ServiceLocator.getSessionRepository(this)
        return repo.isLoggedIn() && repo.getRolActual() == RolUsuario.ADMIN
    }

    private fun actualizarEstilosBarraInferior(active: MainTab) {
        val isAdmin = esModoAdmin()
        val primaryRes = if (isAdmin) R.color.colorAdminPrimary else R.color.colorPrimary
        val primaryDarkRes = if (isAdmin) R.color.colorAdminPrimaryDark else R.color.colorPrimaryDark
        val activeColor = ContextCompat.getColor(this, primaryRes)
        val inactiveColor = ContextCompat.getColor(this, R.color.colorTextSecondary)

        toolbar.setBackgroundColor(activeColor)
        val headerView = navigationView.getHeaderView(0)
        headerView.findViewById<DrawerHeaderBgView>(R.id.drawerHeaderBg)?.setPalette(
            ContextCompat.getColor(this, primaryRes),
            ContextCompat.getColor(this, primaryDarkRes)
        )

        fun applyTabStyle(iv: ImageView?, tv: TextView, isActive: Boolean) {
            val color = if (isActive) activeColor else inactiveColor
            iv?.setColorFilter(color)
            tv.setTextColor(color)
            tv.setTypeface(null, if (isActive) Typeface.BOLD else Typeface.NORMAL)
        }

        applyTabStyle(ivNavExplorar, tvNavExplorar, active == MainTab.EXPLORAR)
        applyTabStyle(ivNavComunidad, tvNavComunidad, active == MainTab.COMUNIDAD)
        applyTabStyle(ivNavItinerario, tvNavItinerario, active == MainTab.ITINERARIO)
        applyTabStyle(ivNavFavoritos, tvNavFavoritos, active == MainTab.FAVORITOS)

        val isReservas = active == MainTab.RESERVAS
        tvNavReservas.setTextColor(if (isReservas) activeColor else inactiveColor)
        tvNavReservas.setTypeface(null, if (isReservas) Typeface.BOLD else Typeface.NORMAL)
        btnNavCenterReservas.setCardBackgroundColor(
            ContextCompat.getColor(
                this,
                if (isReservas) primaryDarkRes else primaryRes
            )
        )
        btnNavCenterReservas.cardElevation = if (isReservas) 10f else 6f
    }

    private fun actualizarDrawerHeaderYMenu() {
        val sessionRepo = ServiceLocator.getSessionRepository(this)
        val usuario = sessionRepo.getUsuarioActual()
        val rol = sessionRepo.getRolActual()

        val headerView = navigationView.getHeaderView(0)
        val ivAvatar: ImageView = headerView.findViewById(R.id.ivDrawerAvatar)
        val tvNombre: TextView = headerView.findViewById(R.id.tvDrawerNombre)
        val tvEmail: TextView = headerView.findViewById(R.id.tvDrawerEmail)
        val tvRol: TextView = headerView.findViewById(R.id.tvDrawerRolBadge)

        val fotoUri = sessionRepo.getUserFotoUri()
        if (fotoUri.isNotEmpty()) {
            ivAvatar.setPadding(0, 0, 0, 0)
            Glide.with(this)
                .load(Uri.parse(fotoUri))
                .circleCrop()
                .placeholder(R.drawable.ic_person)
                .into(ivAvatar)
        } else {
            ivAvatar.setImageResource(R.drawable.ic_person)
        }

        if (usuario != null) {
            tvNombre.text = usuario.nombre
            tvEmail.text = usuario.email
            tvRol.text = if (usuario.rol == RolUsuario.ADMIN) "ADMIN AGENCIA" else "TURISTA"
        } else {
            tvNombre.text = "Invitado"
            tvEmail.text = "Modo exploración libre"
            tvRol.text = "INVITADO"
        }

        val menu = navigationView.menu
        menu.findItem(R.id.drawer_cambiar_rol)?.title =
            if (rol == RolUsuario.ADMIN) "Cambiar a Modo Turista" else "Cambiar a Modo Admin"
        menu.findItem(R.id.drawer_login_logout)?.title =
            if (sessionRepo.isLoggedIn()) "Cerrar Sesión" else "Iniciar Sesión"

        val darkItem = menu.findItem(R.id.drawer_modo_oscuro)
        val switchDark = darkItem?.actionView?.findViewById<SwitchMaterial>(R.id.switchDrawerDarkMode)
        switchDark?.isChecked = sessionRepo.isDarkMode()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val sessionRepo = ServiceLocator.getSessionRepository(this)
        when (item.itemId) {
            R.id.drawer_mapa_gps -> abrirMapaDestinoPrincipal()
            R.id.drawer_brujula -> startActivity(Intent(this, BrujulaActivity::class.java))
            R.id.drawer_itinerario -> seleccionarTab(MainTab.ITINERARIO)
            R.id.drawer_guia_iperu -> startActivity(Intent(this, GuiaViajeActivity::class.java))
            R.id.drawer_cambiar_rol -> alternarRolDesdeDrawer()
            R.id.drawer_modo_oscuro -> {
                val nuevoModo = !sessionRepo.isDarkMode()
                sessionRepo.setDarkMode(nuevoModo)
                AppCompatDelegate.setDefaultNightMode(
                    if (nuevoModo) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
            R.id.drawer_permisos_diagnostico -> startActivity(Intent(this, DiagnosticoLogsActivity::class.java))
            R.id.drawer_ajustes_perfil -> seleccionarTab(MainTab.PERFIL)
            R.id.drawer_onboarding -> startActivity(Intent(this, OnboardingActivity::class.java))
            R.id.drawer_login_logout -> {
                if (sessionRepo.isLoggedIn()) {
                    sessionRepo.logout()
                    Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
                    actualizarDrawerHeaderYMenu()
                    actualizarNavegacionPorRol()
                } else {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun alternarRolDesdeDrawer() {
        val sessionRepo = ServiceLocator.getSessionRepository(this)
        if (sessionRepo.getRolActual() == RolUsuario.CLIENTE) {
            sessionRepo.login(sessionRepo.getQuickAccessUser(RolUsuario.ADMIN))
            Toast.makeText(this, "Modo Administrador de Agencia Activado", Toast.LENGTH_SHORT).show()
        } else {
            sessionRepo.login(sessionRepo.getQuickAccessUser(RolUsuario.CLIENTE))
            Toast.makeText(this, "Modo Turista Activado", Toast.LENGTH_SHORT).show()
        }
        actualizarDrawerHeaderYMenu()
        actualizarNavegacionPorRol()
    }

    fun abrirMapaDestinoPrincipal() {
        val cached = ServiceLocator.getDestinoRepository(this).obtenerDestinosEnCache()
        if (cached.isEmpty()) {
            Toast.makeText(this, "Selecciona un destino primero para trazar la ruta", Toast.LENGTH_SHORT).show()
            return
        }
        if (cached.size == 1) {
            startActivity(Intent(this, MapaRutaActivity::class.java).apply {
                putExtra(MapaRutaActivity.EXTRA_DESTINO, cached.first())
            })
            return
        }
        val names = cached.map { "${it.nombre} (${it.departamento})" }.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle("Trazar Ruta GPS hacia...")
            .setItems(names) { _, which ->
                startActivity(Intent(this, MapaRutaActivity::class.java).apply {
                    putExtra(MapaRutaActivity.EXTRA_DESTINO, cached[which])
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_brujula -> {
                startActivity(Intent(this, BrujulaActivity::class.java))
                true
            }
            R.id.action_notificaciones -> {
                startActivity(Intent(this, pe.edu.utp.perviajero.ui.BandejaAvisosActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun actualizarNavegacionPorRol() {
        actualizarDrawerHeaderYMenu()
        val isAdmin = esModoAdmin()
        if (isAdmin) {
            tvNavReservas.text = "Agencia"
            ivNavCenterReservas.setImageResource(R.drawable.ic_dashboard)
            tvNavItinerario.text = "Catálogo"
            ivNavItinerario.setImageResource(R.drawable.ic_explore_mountain)
        } else {
            tvNavReservas.text = "Mis Reservas"
            ivNavCenterReservas.setImageResource(R.drawable.ic_receipt)
            tvNavItinerario.text = "Itinerario"
            ivNavItinerario.setImageResource(R.drawable.ic_calendar)
        }

        actualizarEstilosBarraInferior(currentTab)

        if (currentTab == MainTab.RESERVAS) {
            if (isAdmin) {
                toolbar.title = "Panel de Agencia"
                cambiarFragment(AdminDashboardFragment())
            } else {
                toolbar.title = "Mis Reservas"
                cambiarFragment(MisReservasFragment())
            }
        } else if (currentTab == MainTab.ITINERARIO) {
            if (isAdmin) {
                toolbar.title = "Catálogo Turístico (Admin)"
                cambiarFragment(AdminCatalogoFragment())
            } else {
                toolbar.title = "Mi Itinerario"
                cambiarFragment(ItinerarioFragment())
            }
        }
    }

    private fun cambiarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
