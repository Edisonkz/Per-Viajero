package pe.edu.utp.perviajero.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.Spinner
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
import pe.edu.utp.perviajero.data.model.Pasajero
import pe.edu.utp.perviajero.data.model.Reserva
import pe.edu.utp.perviajero.util.NotificationHelper
import java.util.Calendar
import java.util.Locale

// Cotizador interactivo con selección de terminal, horario de salida, manifiesto oficial de pasajeros y voucher
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

    private lateinit var spTerminalEmbarque: Spinner
    private lateinit var spHorarioSalida: Spinner

    private lateinit var btnMenosPasajeros: MaterialButton
    private lateinit var btnMasPasajeros: MaterialButton
    private lateinit var tvCantidadPasajeros: TextView
    private lateinit var btnRegistrarPasajeros: MaterialButton

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

    // Manifiesto de pasajeros (normativa SUTRAN / MTC)
    private val listaManifiesto = mutableListOf<Pasajero>()

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
        configurarSpinnersTerminalYHorario()
        sincronizarListaManifiesto()
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

        spTerminalEmbarque = findViewById(R.id.spTerminalEmbarque)
        spHorarioSalida = findViewById(R.id.spHorarioSalida)

        btnMenosPasajeros = findViewById(R.id.btnMenosPasajeros)
        btnMasPasajeros = findViewById(R.id.btnMasPasajeros)
        tvCantidadPasajeros = findViewById(R.id.tvCantidadPasajeros)
        btnRegistrarPasajeros = findViewById(R.id.btnRegistrarPasajeros)

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

        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 3) }
        val dia = cal.get(Calendar.DAY_OF_MONTH)
        val mes = cal.get(Calendar.MONTH) + 1
        val anio = cal.get(Calendar.YEAR)
        fechaSeleccionada = String.format(Locale.US, "%02d/%02d/%d", dia, mes, anio)
        tvFechaSeleccionada.text = fechaSeleccionada
    }

    private fun configurarSpinnersTerminalYHorario() {
        val terminales = arrayOf(
            "Terminal Javier Prado (La Victoria, Lima)",
            "Terminal Terrestre Plaza Norte (Independencia, Lima)",
            "Estación de Tren San Pedro (Cusco)",
            "Terminal Terrestre de Cusco (Wanchaq)",
            "Terminal Terrestre de Arequipa (Jacobo Hunter)",
            "Recojo directo en Hotel (Centro Histórico)"
        )
        val adapterTerminal = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, terminales)
        spTerminalEmbarque.adapter = adapterTerminal

        val horarios = arrayOf(
            "04:30 AM (Madrugada - Tour Amanecer)",
            "06:30 AM (Primer Turno Oficial)",
            "08:30 AM (Turno Mañana Estándar)",
            "01:30 PM (Turno Tarde)",
            "08:00 PM (Servicio Nocturno / Bus Cama)"
        )
        val adapterHorarios = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, horarios)
        spHorarioSalida.adapter = adapterHorarios
        spHorarioSalida.setSelection(1) // Por defecto: 06:30 AM
    }

    private fun sincronizarListaManifiesto() {
        val prefs = PreferencesHelper.getInstance(this)
        val usuario = prefs.getUsuarioActual()
        val nombreTitular = usuario?.nombre ?: "Kevin Turista Quispe"

        while (listaManifiesto.size < cantidadPasajeros) {
            val num = listaManifiesto.size + 1
            if (num == 1) {
                listaManifiesto.add(Pasajero(nombreTitular, "DNI", "74891234", 23, "984112233"))
            } else {
                listaManifiesto.add(Pasajero("Acompañante $num", "DNI", "7000000$num", 25, ""))
            }
        }
        while (listaManifiesto.size > cantidadPasajeros) {
            listaManifiesto.removeAt(listaManifiesto.size - 1)
        }

        btnRegistrarPasajeros.text = "📝 Manifiesto: ${listaManifiesto.size} pasajero(s) registrado(s)"
    }

    private fun configurarEventos() {
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

        btnMenosPasajeros.setOnClickListener {
            if (cantidadPasajeros > 1) {
                cantidadPasajeros--
                tvCantidadPasajeros.text = cantidadPasajeros.toString()
                sincronizarListaManifiesto()
                actualizarCalculoCosto()
            }
        }

        btnMasPasajeros.setOnClickListener {
            if (cantidadPasajeros < 10) {
                cantidadPasajeros++
                tvCantidadPasajeros.text = cantidadPasajeros.toString()
                sincronizarListaManifiesto()
                actualizarCalculoCosto()
            }
        }

        btnRegistrarPasajeros.setOnClickListener {
            mostrarDialogoManifiestoPasajeros()
        }

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

        btnConfirmarReserva.setOnClickListener {
            confirmarYGuardarReserva()
        }
    }

    private fun mostrarDialogoManifiestoPasajeros() {
        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val inputsNombres = mutableListOf<EditText>()
        val inputsDni = mutableListOf<EditText>()
        val inputsEdad = mutableListOf<EditText>()

        for (i in 0 until cantidadPasajeros) {
            val pasajeroActual = listaManifiesto.getOrNull(i)

            val tvTitulo = TextView(this).apply {
                text = "👤 Pasajero #${i + 1}"
                textSize = 15f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, if (i == 0) 0 else 24, 0, 4)
            }
            val etNombre = EditText(this).apply {
                hint = "Nombres y Apellidos Completos"
                setText(pasajeroActual?.nombresApellidos ?: "")
            }
            val etDni = EditText(this).apply {
                hint = "DNI / Pasaporte (8 dígitos)"
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                setText(pasajeroActual?.numeroDocumento ?: "")
            }
            val etEdad = EditText(this).apply {
                hint = "Edad"
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                setText((pasajeroActual?.edad ?: 25).toString())
            }

            inputsNombres.add(etNombre)
            inputsDni.add(etDni)
            inputsEdad.add(etEdad)

            layout.addView(tvTitulo)
            layout.addView(etNombre)
            layout.addView(etDni)
            layout.addView(etEdad)
        }

        scrollView.addView(layout)

        MaterialAlertDialogBuilder(this)
            .setTitle("Manifiesto Oficial de Pasajeros")
            .setView(scrollView)
            .setPositiveButton("Guardar Datos") { dialog, _ ->
                listaManifiesto.clear()
                for (i in 0 until cantidadPasajeros) {
                    val nom = inputsNombres[i].text.toString().trim()
                    val dni = inputsDni[i].text.toString().trim()
                    val edadStr = inputsEdad[i].text.toString().trim()
                    val edad = edadStr.toIntOrNull() ?: 25

                    listaManifiesto.add(
                        Pasajero(
                            nombresApellidos = if (nom.isNotEmpty()) nom else "Pasajero ${i + 1}",
                            tipoDocumento = "DNI",
                            numeroDocumento = if (dni.isNotEmpty()) dni else "0000000${i + 1}",
                            edad = edad
                        )
                    )
                }
                btnRegistrarPasajeros.text = "📝 Manifiesto: ${listaManifiesto.size} pasajero(s) registrado(s)"
                Toast.makeText(this, "Datos de los pasajeros guardados", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
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

        val terminalElegido = spTerminalEmbarque.selectedItem.toString()
        val horarioElegido = spHorarioSalida.selectedItem.toString()

        val nuevaReserva = Reserva(
            codigoReserva = codigoGenerado,
            destinoId = dest.id,
            destinoNombre = dest.nombre,
            destinoFoto = dest.imageUrl,
            fechaSalida = fechaSeleccionada,
            terminalEmbarque = terminalElegido,
            horarioSalida = horarioElegido,
            cantidadPersonas = cantidadPasajeros,
            listaPasajeros = listaManifiesto.toList(),
            tipoMovilidad = nombreMovilidad,
            categoriaHotel = nombreHotel,
            guiaTuristico = nombreGuia,
            costoTotal = total,
            estado = "Confirmada",
            usuarioNombre = nombreUsuario,
            usuarioEmail = emailUsuario
        )

        prefs.guardarReserva(nuevaReserva)

        NotificationHelper.notificarReservaConfirmada(this, nuevaReserva)

        // Resumen formateado para mostrar y compartir
        val resumenPasajeros = listaManifiesto.joinToString("\n") { "   • ${it.nombresApellidos} (DNI: ${it.numeroDocumento}, ${it.edad} años)" }

        MaterialAlertDialogBuilder(this)
            .setTitle("¡Boleto y Voucher Emitido!")
            .setMessage(
                "${empresa.razonSocial}\n" +
                "RUC: ${empresa.ruc} • Reg. ${empresa.registroMincetur}\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "• Código: $codigoGenerado\n" +
                "• Destino: ${dest.nombre}\n" +
                "• Fecha: $fechaSeleccionada\n" +
                "• Horario de Salida: $horarioElegido\n" +
                "• Terminal de Embarque: $terminalElegido\n" +
                "• Movilidad: $nombreMovilidad\n" +
                "• Alojamiento: $nombreHotel\n" +
                "• Guía Oficial: $nombreGuia\n\n" +
                "👥 Manifiesto de Pasajeros:\n$resumenPasajeros\n\n" +
                "💰 Total Pagado: S/ ${String.format(Locale.US, "%.2f", total)} (Inc. IGV)\n\n" +
                "Toca 'Aceptar' o pulsa en 'Mis Reservas' para ver el ticket detallado."
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
        val listaStr = reserva.listaPasajeros.joinToString("\n") { "   - ${it.nombresApellidos} (DNI: ${it.numeroDocumento})" }
        val mensaje = "🎟️ VOUCHER OFICIAL DE EMBARQUE - $razonSocial\n" +
                "RUC: $ruc • Reg. Oficial MINCETUR\n\n" +
                "• Código de Reserva: ${reserva.codigoReserva}\n" +
                "• Tour: ${reserva.destinoNombre}\n" +
                "• Fecha de Salida: ${reserva.fechaSalida}\n" +
                "• Horario: ${reserva.horarioSalida}\n" +
                "• Terminal de Embarque: ${reserva.terminalEmbarque}\n" +
                "• Transporte: ${reserva.tipoMovilidad}\n" +
                "• Alojamiento: ${reserva.categoriaHotel}\n" +
                "• Guía Acreditado: ${reserva.guiaTuristico}\n\n" +
                "👥 Pasajeros Registrados:\n$listaStr\n\n" +
                "💰 Monto Total: S/ ${String.format(Locale.US, "%.2f", reserva.costoTotal)} (Inc. IGV)\n\n" +
                "Emitido por PerúViajero."

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, mensaje)
        }
        val chooser = Intent.createChooser(sendIntent, "Compartir Voucher de Embarque")
        startActivity(chooser)
    }
}
