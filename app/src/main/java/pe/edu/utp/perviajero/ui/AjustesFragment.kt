package pe.edu.utp.perviajero.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import pe.edu.utp.perviajero.MainActivity
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.model.RolUsuario
import pe.edu.utp.perviajero.data.model.Usuario
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.util.CameraFileHelper
import pe.edu.utp.perviajero.util.PermissionHelper

// Pantalla de Ajustes: foto de perfil con cámara, estado de permisos, diagnóstico, roles y tema visual
class AjustesFragment : Fragment() {

    private lateinit var ivPerfilAvatar: ImageView
    private lateinit var btnCambiarFotoPerfil: View
    private lateinit var tvPerfilNombre: TextView
    private lateinit var tvPerfilEmail: TextView
    private lateinit var tvPerfilRol: TextView
    private lateinit var btnMiItinerario: MaterialButton
    private lateinit var btnVerOnboarding: MaterialButton
    private lateinit var btnCambiarRol: MaterialButton
    private lateinit var btnLoginLogout: MaterialButton
    private lateinit var tvEstadoPermisos: TextView
    private lateinit var btnSolicitarPermisos: MaterialButton
    private lateinit var btnAbrirDiagnostico: MaterialButton
    private lateinit var switchModoOscuro: SwitchMaterial

    private var pendingAvatarFileUri: String = ""

    private val cameraPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        actualizarEstadoPermisos()
        if (granted) {
            tomarFotoPerfil()
        } else {
            PermissionHelper.showPermanentlyDeniedDialog(
                requireActivity(),
                "Permiso de Cámara",
                "Habilita la cámara para actualizar tu foto de perfil."
            )
        }
    }

    private val allPermsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        actualizarEstadoPermisos()
        Toast.makeText(requireContext(), "Estado de permisos actualizado", Toast.LENGTH_SHORT).show()
    }

    private val takeAvatarLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingAvatarFileUri.isNotEmpty()) {
            ServiceLocator.getSessionRepository(requireContext()).setUserFotoUri(pendingAvatarFileUri)
            cargarAvatarUsuario()
            Toast.makeText(requireContext(), "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ajustes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivPerfilAvatar = view.findViewById(R.id.ivPerfilAvatar)
        btnCambiarFotoPerfil = view.findViewById(R.id.btnCambiarFotoPerfil)
        tvPerfilNombre = view.findViewById(R.id.tvPerfilNombre)
        tvPerfilEmail = view.findViewById(R.id.tvPerfilEmail)
        tvPerfilRol = view.findViewById(R.id.tvPerfilRol)
        btnMiItinerario = view.findViewById(R.id.btnMiItinerario)
        btnVerOnboarding = view.findViewById(R.id.btnVerOnboarding)
        btnCambiarRol = view.findViewById(R.id.btnCambiarRol)
        btnLoginLogout = view.findViewById(R.id.btnLoginLogout)
        tvEstadoPermisos = view.findViewById(R.id.tvEstadoPermisos)
        btnSolicitarPermisos = view.findViewById(R.id.btnSolicitarPermisos)
        btnAbrirDiagnostico = view.findViewById(R.id.btnAbrirDiagnostico)
        switchModoOscuro = view.findViewById(R.id.switchModoOscuro)

        btnCambiarFotoPerfil.setOnClickListener { iniciarCambioFotoPerfil() }
        ivPerfilAvatar.setOnClickListener { iniciarCambioFotoPerfil() }

        btnMiItinerario.setOnClickListener {
            startActivity(Intent(requireContext(), ItinerarioActivity::class.java))
        }

        btnVerOnboarding.setOnClickListener {
            startActivity(Intent(requireContext(), OnboardingActivity::class.java))
        }

        btnAbrirDiagnostico.setOnClickListener {
            startActivity(Intent(requireContext(), DiagnosticoLogsActivity::class.java))
        }

        btnSolicitarPermisos.setOnClickListener {
            solicitarTodosLosPermisosPendientes()
        }

        val sessionRepo = ServiceLocator.getSessionRepository(requireContext())
        switchModoOscuro.isChecked = sessionRepo.isDarkMode()
        switchModoOscuro.setOnCheckedChangeListener { _, isChecked ->
            sessionRepo.setDarkMode(isChecked)
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        configurarPerfilYRoles()
        cargarAvatarUsuario()
        actualizarEstadoPermisos()
    }

    override fun onResume() {
        super.onResume()
        configurarPerfilYRoles()
        cargarAvatarUsuario()
        actualizarEstadoPermisos()
    }

    private fun iniciarCambioFotoPerfil() {
        if (PermissionHelper.hasCameraPermission(requireContext())) {
            tomarFotoPerfil()
        } else if (PermissionHelper.shouldShowRationale(requireActivity(), Manifest.permission.CAMERA)) {
            PermissionHelper.showRationaleDialog(
                requireActivity(),
                "Foto de Perfil con Cámara",
                "Autoriza la cámara para tomar tu foto de viajero."
            ) {
                cameraPermLauncher.launch(Manifest.permission.CAMERA)
            }
        } else {
            cameraPermLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun tomarFotoPerfil() {
        val (contentUri, fileUri) = CameraFileHelper.createPhotoOutputUri(requireContext(), "avatar")
        pendingAvatarFileUri = fileUri
        takeAvatarLauncher.launch(contentUri)
    }

    private fun cargarAvatarUsuario() {
        val fotoUri = ServiceLocator.getSessionRepository(requireContext()).getUserFotoUri()
        if (fotoUri.isNotEmpty()) {
            ivPerfilAvatar.setPadding(0, 0, 0, 0)
            ivPerfilAvatar.imageTintList = null
            Glide.with(this)
                .load(Uri.parse(fotoUri))
                .circleCrop()
                .placeholder(R.drawable.ic_person)
                .into(ivPerfilAvatar)
        }
    }

    private fun actualizarEstadoPermisos() {
        val ctx = requireContext()
        val cam = if (PermissionHelper.hasCameraPermission(ctx)) "Concedido" else "Pendiente"
        val gps = if (PermissionHelper.hasLocationPermission(ctx)) "Concedido" else "Pendiente"
        val notif = if (PermissionHelper.hasNotificationPermission(ctx)) "Concedido" else "Pendiente"
        tvEstadoPermisos.text = "• Cámara (Comunidad/Perfil): $cam\n• Ubicación GPS (Mapa y Ruta): $gps\n• Notificaciones (Alertas/Push): $notif"
    }

    private fun solicitarTodosLosPermisosPendientes() {
        val ctx = requireContext()
        val pendientes = mutableListOf<String>()
        if (!PermissionHelper.hasCameraPermission(ctx)) pendientes.add(Manifest.permission.CAMERA)
        if (!PermissionHelper.hasLocationPermission(ctx)) {
            pendientes.add(Manifest.permission.ACCESS_FINE_LOCATION)
            pendientes.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !PermissionHelper.hasNotificationPermission(ctx)
        ) {
            pendientes.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (pendientes.isEmpty()) {
            Toast.makeText(ctx, "Todos los permisos principales ya están concedidos", Toast.LENGTH_SHORT).show()
            PermissionHelper.openAppSettings(ctx)
        } else {
            allPermsLauncher.launch(pendientes.toTypedArray())
        }
    }

    private fun configurarPerfilYRoles() {
        val sessionRepo = ServiceLocator.getSessionRepository(requireContext())
        val usuario = sessionRepo.getUsuarioActual()
        val rolActual = sessionRepo.getRolActual()

        if (usuario != null) {
            tvPerfilNombre.text = usuario.nombre
            tvPerfilEmail.text = usuario.email
            tvPerfilRol.text = "ROL: ${usuario.rol.name}"
            btnLoginLogout.text = "Cerrar Sesión"

            if (usuario.rol == RolUsuario.CLIENTE) {
                btnCambiarRol.text = "Cambiar a Modo Administrador"
                btnCambiarRol.setIconResource(R.drawable.ic_dashboard)
            } else {
                btnCambiarRol.text = "Cambiar a Modo Turista"
                btnCambiarRol.setIconResource(R.drawable.ic_person)
            }
        } else {
            tvPerfilNombre.text = "Invitado (Modo Libre)"
            tvPerfilEmail.text = "Exploración libre sin registro"
            tvPerfilRol.text = "ROL: INVITADO"
            btnLoginLogout.text = "Iniciar Sesión"
            btnCambiarRol.text = "Cambiar a Modo Administrador"
            btnCambiarRol.setIconResource(R.drawable.ic_dashboard)
        }

        btnCambiarRol.setOnClickListener {
            if (rolActual == RolUsuario.CLIENTE) {
                sessionRepo.login(
                    Usuario("usr_gerson_admin", "Gerson Administrador", "gerson@peruviajero.pe", RolUsuario.ADMIN)
                )
                Toast.makeText(requireContext(), "Modo Administrador de Agencia Activado", Toast.LENGTH_SHORT).show()
            } else {
                sessionRepo.login(
                    Usuario("usr_kevin_01", "Kevin Turista", "kevin@peruviajero.pe", RolUsuario.CLIENTE)
                )
                Toast.makeText(requireContext(), "Modo Turista Activado", Toast.LENGTH_SHORT).show()
            }
            configurarPerfilYRoles()
            (activity as? MainActivity)?.actualizarNavegacionPorRol()
        }

        btnLoginLogout.setOnClickListener {
            if (sessionRepo.isLoggedIn()) {
                sessionRepo.logout()
                Toast.makeText(requireContext(), "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
                configurarPerfilYRoles()
                (activity as? MainActivity)?.actualizarNavegacionPorRol()
            } else {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }
        }
    }
}
