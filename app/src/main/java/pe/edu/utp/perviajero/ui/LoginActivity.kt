package pe.edu.utp.perviajero.ui

import android.app.Activity
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.RolUsuario
import pe.edu.utp.perviajero.data.model.Usuario

// Autenticación de usuarios, modo invitado, roles (Turista / Administrador) y registro de cuentas
class LoginActivity : AppCompatActivity() {

    private lateinit var toolbarLogin: MaterialToolbar
    private lateinit var btnInvitado: MaterialButton
    private lateinit var btnQuickCliente: MaterialButton
    private lateinit var btnQuickAdmin: MaterialButton
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnRegistrarme: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val prefs = PreferencesHelper.getInstance(this)

        toolbarLogin = findViewById(R.id.toolbarLogin)
        btnInvitado = findViewById(R.id.btnInvitado)
        btnQuickCliente = findViewById(R.id.btnQuickCliente)
        btnQuickAdmin = findViewById(R.id.btnQuickAdmin)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegistrarme = findViewById(R.id.btnRegistrarme)

        setSupportActionBar(toolbarLogin)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbarLogin.setNavigationOnClickListener {
            finish()
        }

        // Opción 1: Continuar como Invitado (Modo Libre sin registro)
        btnInvitado.setOnClickListener {
            prefs.logout()
            Toast.makeText(this, "Navegando como Invitado (Modo Libre)", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
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

        // Login tradicional con credenciales
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
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

        // Opción: Registrarse como nuevo turista
        btnRegistrarme.setOnClickListener {
            mostrarDialogoRegistro()
        }
    }

    private fun mostrarDialogoRegistro() {
        val prefs = PreferencesHelper.getInstance(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 10)
        }

        val etRegNombre = EditText(this).apply {
            hint = "Nombre Completo"
        }
        val etRegEmail = EditText(this).apply {
            hint = "Correo Electrónico"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        val etRegTelefono = EditText(this).apply {
            hint = "Teléfono Celular"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
        }
        val etRegDocumento = EditText(this).apply {
            hint = "DNI o Pasaporte"
        }
        val etRegPass = EditText(this).apply {
            hint = "Contraseña"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        layout.addView(etRegNombre)
        layout.addView(etRegEmail)
        layout.addView(etRegTelefono)
        layout.addView(etRegDocumento)
        layout.addView(etRegPass)

        MaterialAlertDialogBuilder(this)
            .setTitle("Registro de Nuevo Turista")
            .setView(layout)
            .setPositiveButton("Crear Cuenta") { dialog, _ ->
                val nombre = etRegNombre.text.toString().trim()
                val email = etRegEmail.text.toString().trim()
                val pass = etRegPass.text.toString().trim()

                if (nombre.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                    val nuevoUsuario = Usuario(
                        id = "usr_${System.currentTimeMillis()}",
                        nombre = nombre,
                        email = email,
                        rol = RolUsuario.CLIENTE
                    )
                    prefs.login(nuevoUsuario)
                    Toast.makeText(this, "¡Cuenta creada exitosamente! Bienvenido, $nombre", Toast.LENGTH_LONG).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this, "Nombre, Correo y Contraseña son obligatorios", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
