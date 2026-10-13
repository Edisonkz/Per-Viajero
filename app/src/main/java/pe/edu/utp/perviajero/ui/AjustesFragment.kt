package pe.edu.utp.perviajero.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import pe.edu.utp.perviajero.MainActivity
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.RolUsuario
import pe.edu.utp.perviajero.data.model.Usuario

// REQ-10: Pantalla de Ajustes, control de sesión, alternancia de roles y selector de tema visual
class AjustesFragment : Fragment() {

    private lateinit var tvPerfilNombre: TextView
    private lateinit var tvPerfilEmail: TextView
    private lateinit var tvPerfilRol: TextView
    private lateinit var btnCambiarRol: MaterialButton
    private lateinit var btnMiItinerario: MaterialButton
    private lateinit var btnLoginLogout: MaterialButton
    private lateinit var switchModoOscuro: SwitchMaterial

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ajustes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvPerfilNombre = view.findViewById(R.id.tvPerfilNombre)
        tvPerfilEmail = view.findViewById(R.id.tvPerfilEmail)
        tvPerfilRol = view.findViewById(R.id.tvPerfilRol)
        btnCambiarRol = view.findViewById(R.id.btnCambiarRol)
        btnMiItinerario = view.findViewById(R.id.btnMiItinerario)
        btnLoginLogout = view.findViewById(R.id.btnLoginLogout)
        switchModoOscuro = view.findViewById(R.id.switchModoOscuro)

        btnMiItinerario.setOnClickListener {
            val intent = Intent(requireContext(), ItinerarioActivity::class.java)
            startActivity(intent)
        }

        val prefs = PreferencesHelper.getInstance(requireContext())

        // Cargar estado inicial del tema guardado
        switchModoOscuro.isChecked = prefs.isDarkMode()
        switchModoOscuro.setOnCheckedChangeListener { _, isChecked ->
            prefs.setDarkMode(isChecked)
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Configuración de botones de perfil
        configurarPerfilYRoles()
    }

    override fun onResume() {
        super.onResume()
        configurarPerfilYRoles()
    }

    private fun configurarPerfilYRoles() {
        val prefs = PreferencesHelper.getInstance(requireContext())
        val usuario = prefs.getUsuarioActual()
        val rolActual = prefs.getRolActual()

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

        // Acción: Alternar rol instantáneamente (Ideal para sustentación)
        btnCambiarRol.setOnClickListener {
            if (rolActual == RolUsuario.CLIENTE) {
                val admin = Usuario(
                    id = "usr_gerson_admin",
                    nombre = "Gerson Administrador",
                    email = "gerson@peruviajero.pe",
                    rol = RolUsuario.ADMIN
                )
                prefs.login(admin)
                Toast.makeText(requireContext(), "Modo Administrador de Agencia Activado", Toast.LENGTH_SHORT).show()
            } else {
                val cliente = Usuario(
                    id = "usr_kevin_01",
                    nombre = "Kevin Turista",
                    email = "kevin@peruviajero.pe",
                    rol = RolUsuario.CLIENTE
                )
                prefs.login(cliente)
                Toast.makeText(requireContext(), "Modo Turista Activado", Toast.LENGTH_SHORT).show()
            }
            configurarPerfilYRoles()
            (activity as? MainActivity)?.actualizarNavegacionPorRol()
        }

        // Acción: Iniciar / Cerrar sesión
        btnLoginLogout.setOnClickListener {
            if (prefs.isLoggedIn()) {
                prefs.logout()
                Toast.makeText(requireContext(), "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
                configurarPerfilYRoles()
                (activity as? MainActivity)?.actualizarNavegacionPorRol()
            } else {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
