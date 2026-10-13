package pe.edu.utp.perviajero.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.data.model.Reserva
import pe.edu.utp.perviajero.util.NotificationHelper
import java.util.Calendar
import java.util.Locale

// Cotizador interactivo con cálculo dinámico en tiempo real, selección de guía, notificaciones nativas y voucher
class ReservaTourActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DESTINO = "extra_destino"
        private const val PRECIO_BASE_POR_PERSONA = 150.0
    }

    private lateinit var toolbarReserva: MaterialToolbar
    private lateinit var ivReservaFoto: ImageView
    private lateinit var tvReservaDestino: TextView
    private lateinit var tvReservaDepartamento: TextView
    private lateinit var tvFechaSeleccionada: TextView
    private lateinit var btnElegirFecha: MaterialButton
    private lateinit var btnMenosPasajeros: MaterialButton
    private lateinit var btnMasPasajeros: MaterialButton
    private lateinit var tvCantidadPasajeros: TextView
    private lateinit var rgMovilidad: RadioGroup
    private lateinit var rgHotel: RadioGroup
    private lateinit var rgGuia: RadioGroup
    private lateinit var tvCostoTotal: TextView
    private lateinit var btnConfirmarReserva: MaterialButton

    private var destino: Destino? = null
    private var fechaSeleccionada: String = ""
    private var cantidadPasajeros: Int = 1
    private var extraMovilidad: Double = 0.0
    private var nombreMovilidad: String = "Van Turística Compartida"
    private var extraHotel: Double = 0.0
    private var nombreHotel: String = "Solo Tour"
    private var extraGuia: Double = 0.0
    private var nombreGuia: String = "Sin Guía Privado"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reserva_tour)

        destino = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_DESTINO, Destino::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_DESTINO) as? Destino
        }

        if (destino == null) {
            Toast.makeText(this, "Destino no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        inicializarVistas()
        configurarDatosDestino()
        configurarEventos()
        actualizarCalculoCosto()
    }

    private fun inicializarVistas() {
        toolbarReserva = findViewById(R.id.toolbarReserva)
        ivReservaFoto = findViewById(R.id.ivReservaFoto)
        tvReservaDestino = findViewById(R.id.tvReservaDestino)
        tvReservaDepartamento = findViewById(R.id.tvReservaDepartamento)
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada)
        btnElegirFecha = findViewById(R.id.btnElegirFecha)
        btnMenosPasajeros = findViewById(R.id.btnMenosPasajeros)
        btnMasPasajeros = findViewById(R.id.btnMasPasajeros)
        tvCantidadPasajeros = findViewById(R.id.tvCantidadPasajeros)
        rgMovilidad = findViewById(R.id.rgMovilidad)
        rgHotel = findViewById(R.id.rgHotel)
        rgGuia = findViewById(R.id.rgGuia)
        tvCostoTotal = findViewById(R.id.tvCostoTotal)
        btnConfirmarReserva = findViewById(R.id.btnConfirmarReserva)

        setSupportActionBar(toolbarReserva)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbarReserva.setNavigationOnClickListener {
            finish()
        }
    }

    private fun configurarDatosDestino() {
        val dest = destino ?: return
        tvReservaDestino.text = dest.nombre
        tvReservaDepartamento.text = "${dest.departamento}, Perú"

        Glide.with(this)
            .load(dest.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_dialog_alert)
            .into(ivReservaFoto)

        // Por defecto, fecha sugerida a 3 días en el futuro
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 3) }
        val dia = cal.get(Calendar.DAY_OF_MONTH)
        val mes = cal.get(Calendar.MONTH) + 1
        val anio = cal.get(Calendar.YEAR)
        fechaSeleccionada = String.format(Locale.US, "%02d/%02d/%d", dia, mes, anio)
        tvFechaSeleccionada.text = fechaSeleccionada
    }

    private fun configurarEventos() {
        // Selector de fecha mediante DatePickerDialog
        btnElegirFecha.setOnClickListener {
            val cal = Calendar.getInstance()
            val dpd = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    fechaSeleccionada = String.format(Locale.US, "%02d/%02d/%d", dayOfMonth, month + 1, year)
                    tvFechaSeleccionada.text = fechaSeleccionada
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            dpd.datePicker.minDate = System.currentTimeMillis() - 1000
            dpd.show()
        }

        // Contador de pasajeros
        btnMenosPasajeros.setOnClickListener {
            if (cantidadPasajeros > 1) {
                cantidadPasajeros--
                tvCantidadPasajeros.text = cantidadPasajeros.toString()
                actualizarCalculoCosto()
            }
        }

        btnMasPasajeros.setOnClickListener {
            if (cantidadPasajeros < 10) {
                cantidadPasajeros++
                tvCantidadPasajeros.text = cantidadPasajeros.toString()
                actualizarCalculoCosto()
            }
        }

        // Selector de Movilidad
        rgMovilidad.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbMovilidadVan -> {
                    extraMovilidad = 0.0
                    nombreMovilidad = "Van Turística Compartida"
                }
                R.id.rbMovilidadBus -> {
                    extraMovilidad = 40.0
                    nombreMovilidad = "Minibús Panorámico"
                }
                R.id.rbMovilidad4x4 -> {
                    extraMovilidad = 120.0
                    nombreMovilidad = "Camioneta 4x4 Privada"
                }
                R.id.rbMovilidadTren -> {
                    extraMovilidad = 250.0
                    nombreMovilidad = "Tren VIP Ejecutivo"
                }
            }
            actualizarCalculoCosto()
        }

        // Selector de Hotel
        rgHotel.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbHotelNinguno -> {
                    extraHotel = 0.0
                    nombreHotel = "Solo Tour"
                }
                R.id.rbHotel3 -> {
                    extraHotel = 90.0
                    nombreHotel = "Hotel Turista 3★"
                }
                R.id.rbHotel4 -> {
                    extraHotel = 180.0
                    nombreHotel = "Hotel Superior 4★"
                }
                R.id.rbHotel5 -> {
                    extraHotel = 320.0
                    nombreHotel = "Resort de Lujo 5★"
                }
            }
            actualizarCalculoCosto()
        }

        // Selector de Guía Turístico Oficial
        rgGuia.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbGuiaNinguno -> {
                    extraGuia = 0.0
                    nombreGuia = "Sin Guía Privado"
                }
                R.id.rbGuiaEspanol -> {
                    extraGuia = 45.0
                    nombreGuia = "Guía Oficial en Español"
                }
                R.id.rbGuiaBilingue -> {
                    extraGuia = 80.0
                    nombreGuia = "Guía Bilingüe Certificado"
                }
                R.id.rbGuiaExperto -> {
                    extraGuia = 130.0
                    nombreGuia = "Guía Experto de Montaña"
                }
            }
            actualizarCalculoCosto()
        }

        // Confirmar y generar reserva
        btnConfirmarReserva.setOnClickListener {
            confirmarYGuardarReserva()
        }
    }

    private fun calcularCostoTotal(): Double {
        val costoPorPersona = PRECIO_BASE_POR_PERSONA + extraMovilidad + extraHotel
        return (costoPorPersona * cantidadPasajeros) + extraGuia
    }

    private fun actualizarCalculoCosto() {
        val total = calcularCostoTotal()
        tvCostoTotal.text = String.format(Locale.US, "S/ %.2f", total)
    }

    private fun confirmarYGuardarReserva() {
        val dest = destino ?: return
        if (fechaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Por favor selecciona una fecha de salida", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = PreferencesHelper.getInstance(this)
        val usuario = prefs.getUsuarioActual()
        val empresa = prefs.getEmpresaInfo()
        val nombreUsuario = usuario?.nombre ?: "Kevin Turista"
        val emailUsuario = usuario?.email ?: "kevin@peruviajero.pe"

        val codigoGenerado = "PV-2026-" + (1000..9999).random()
        val total = calcularCostoTotal()

        val nuevaReserva = Reserva(
            codigoReserva = codigoGenerado,
            destinoId = dest.id,
            destinoNombre = dest.nombre,
            destinoFoto = dest.imageUrl,
            fechaSalida = fechaSeleccionada,
            cantidadPersonas = cantidadPasajeros,
            tipoMovilidad = nombreMovilidad,
            categoriaHotel = nombreHotel,
            guiaTuristico = nombreGuia,
            costoTotal = total,
            estado = "Confirmada",
            usuarioNombre = nombreUsuario,
            usuarioEmail = emailUsuario
        )

        prefs.guardarReserva(nuevaReserva)

        // Lanzar Notificación Nativa de Android en la Barra de Estado
        NotificationHelper.notificarReservaConfirmada(this, nuevaReserva)

        // Diálogo de confirmación con voucher generado e Intent para compartir
        MaterialAlertDialogBuilder(this)
            .setTitle("¡Voucher Oficial Generado!")
            .setMessage(
                "${empresa.razonSocial}\n" +
                "RUC: ${empresa.ruc} • Reg. ${empresa.registroMincetur}\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "• Código: $codigoGenerado\n" +
                "• Destino: ${dest.nombre}\n" +
                "• Salida: $fechaSeleccionada\n" +
                "• Pasajeros: $cantidadPasajeros persona(s)\n" +
                "• Transporte: $nombreMovilidad\n" +
                "• Alojamiento: $nombreHotel\n" +
                "• Servicio de Guía: $nombreGuia\n" +
                "• Total Pagado: S/ ${String.format(Locale.US, "%.2f", total)}\n\n" +
                "Se ha enviado una notificación a tu dispositivo. Revisa tu voucher en 'Mis Reservas'."
            )
            .setIcon(R.drawable.ic_check_circle)
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setNeutralButton("Compartir") { _, _ ->
                compartirVoucher(nuevaReserva, empresa.razonSocial, empresa.ruc)
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun compartirVoucher(reserva: Reserva, razonSocial: String, ruc: String) {
        val mensaje = "🎟️ VOUCHER DE VIAJE OFICIAL - $razonSocial (RUC: $ruc)\n\n" +
                "¡Hola! He reservado el tour a ${reserva.destinoNombre}.\n" +
                "• Código: ${reserva.codigoReserva}\n" +
                "• Fecha de salida: ${reserva.fechaSalida}\n" +
                "• Pasajeros: ${reserva.cantidadPersonas}\n" +
                "• Transporte: ${reserva.tipoMovilidad}\n" +
                "• Alojamiento: ${reserva.categoriaHotel}\n" +
                "• Guía Oficial: ${reserva.guiaTuristico}\n" +
                "• Monto Total: S/ ${String.format(Locale.US, "%.2f", reserva.costoTotal)}\n\n" +
                "Gestionado con la aplicación PerúViajero."

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, mensaje)
        }
        val chooser = Intent.createChooser(sendIntent, "Compartir Voucher de Tour")
        startActivity(chooser)
    }
}
