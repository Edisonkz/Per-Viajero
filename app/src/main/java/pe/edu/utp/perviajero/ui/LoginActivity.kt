package pe.edu.utp.perviajero.ui

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.RolUsuario
import pe.edu.utp.perviajero.data.model.Usuario

// Autenticación de usuarios y selección de roles (Turista / Administrador)
class LoginActivity : AppCompatActivity() {

    private lateinit var toolbarLogin: MaterialToolbar
    private lateinit var btnQuickCliente: MaterialButton
    private lateinit var btnQuickAdmin: MaterialButton
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnInvitado: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val prefs = PreferencesHelper.getInstance(this)

        toolbarLogin = findViewById(R.id.toolbarLogin)
        btnQuickCliente = findViewById(R.id.btnQuickCliente)
        btnQuickAdmin = findViewById(R.id.btnQuickAdmin)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnInvitado = findViewById(R.id.btnInvitado)

        setSupportActionBar(toolbarLogin)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbarLogin.setNavigationOnClickListener {
            finish()
        }

        // Acceso rápido: Turista (Kevin)
        btnQuickCliente.setOnClickListener {
            val usuario = Usuario(
                id = "usr_kevin_01",
                nombre = "Kevin Turista",
                email = "kevin@peruviajero.pe",
                rol = RolUsuario.CLIENTE
            )
            prefs.login(usuario)
            Toast.makeText(this, "Bienvenido, Kevin (Modo Turista)", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        }

        // Acceso rápido: Administrador (Gerson)
        btnQuickAdmin.setOnClickListener {
            val usuario = Usuario(
                id = "usr_gerson_admin",
                nombre = "Gerson Administrador",
                email = "gerson@peruviajero.pe",
                rol = RolUsuario.ADMIN
            )
            prefs.login(usuario)
            Toast.makeText(this, "Panel de Administración activado (Gerson)", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        }

        // Login tradicional con formulario
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rol = if (email.contains("admin", ignoreCase = true)) {
                RolUsuario.ADMIN
            } else {
                RolUsuario.CLIENTE
            }

            val nombre = if (rol == RolUsuario.ADMIN) "Administrador Agencia" else "Viajero Registrado"
            val usuario = Usuario("usr_${System.currentTimeMillis()}", nombre, email, rol)
            prefs.login(usuario)

            Toast.makeText(this, "Sesión iniciada correctamente", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        }

        // Continuar como invitado
        btnInvitado.setOnClickListener {
            finish()
        }
    }
}

