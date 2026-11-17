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
import pe.edu.utp.perviajero.data.model.RolUsuario
import pe.edu.utp.perviajero.di.ServiceLocator

// Autenticación de usuarios contra UsuarioService (assets/services/usuarios.json), modo invitado y registro
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

        val sessionRepo = ServiceLocator.getSessionRepository(this)

        toolbarLogin = findViewById(R.id.toolbarLogin)
        pe.edu.utp.perviajero.util.WindowInsetsHelper.applyToolbarActivityInsets(this, toolbarLogin)
        btnInvitado = findViewById(R.id.btnInvitado)
        btnQuickCliente = findViewById(R.id.btnQuickCliente)
        btnQuickAdmin = findViewById(R.id.btnQuickAdmin)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegistrarme = findViewById(R.id.btnRegistrarme)

        setSupportActionBar(toolbarLogin)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbarLogin.setNavigationOnClickListener { finish() }

        btnInvitado.setOnClickListener {
            sessionRepo.logout()
            Toast.makeText(this, "Navegando como Invitado (Modo Libre)", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        }

        btnQuickCliente.setOnClickListener {
            val usuario = sessionRepo.getQuickAccessUser(RolUsuario.CLIENTE)
            sessionRepo.login(usuario)
            Toast.makeText(this, "Bienvenido, ${usuario.nombre} (Modo Turista)", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        }

        btnQuickAdmin.setOnClickListener {
            val usuario = sessionRepo.getQuickAccessUser(RolUsuario.ADMIN)
            sessionRepo.login(usuario)
            Toast.makeText(this, "Panel de Administración activado (${usuario.nombre})", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val autenticado = sessionRepo.authenticate(email, password)
            if (autenticado != null) {
                sessionRepo.login(autenticado)
                Toast.makeText(this, "Bienvenido, ${autenticado.nombre}", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Credenciales inválidas. Usa el acceso rápido o regístrate.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        btnRegistrarme.setOnClickListener {
            mostrarDialogoRegistro()
        }
    }

    private fun mostrarDialogoRegistro() {
        val sessionRepo = ServiceLocator.getSessionRepository(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 10)
        }

        val etRegNombre = EditText(this).apply { hint = "Nombre Completo" }
        val etRegEmail = EditText(this).apply {
            hint = "Correo Electrónico"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        val etRegTelefono = EditText(this).apply {
            hint = "Teléfono Celular"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
        }
        val etRegDocumento = EditText(this).apply { hint = "DNI o Pasaporte" }
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
                    val nuevoUsuario = sessionRepo.registerUser(nombre, email, pass)
                    sessionRepo.login(nuevoUsuario)
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
