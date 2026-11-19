package pe.edu.utp.perviajero.ui

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.data.model.Pasajero
import pe.edu.utp.perviajero.data.model.Reserva
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.util.ContactoAgenda
import pe.edu.utp.perviajero.util.ContactoImportHelper
import pe.edu.utp.perviajero.util.CotizadorCalculator
import pe.edu.utp.perviajero.util.GestorPermisos
import pe.edu.utp.perviajero.util.NotificationHelper
import pe.edu.utp.perviajero.util.ReminderScheduler
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
    private lateinit var btnImportarContactos: MaterialButton
    private lateinit var btnRegistrarPasajeros: MaterialButton
    private lateinit var tvEstadoPermisoContactos: TextView

    private val permisosContactos = arrayOf(Manifest.permission.READ_CONTACTS)
    private val gestorPermisos by lazy { GestorPermisos(this) }

    private val lanzadorContactos = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        gestorPermisos.marcarPreguntado(permisosContactos)
        if (resultado.values.any { it }) {
            importarAcompanantesDesdeContactos()
        } else {
            Toast.makeText(
                this,
                "Sin acceso a contactos. Puedes registrar los pasajeros manualmente.",
                Toast.LENGTH_LONG
            ).show()
        }
        actualizarEstadoPermisoContactos()
    }

    private val lanzadorNotificaciones = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gestorPermisos.marcarPreguntado(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
        }
        if (resultado.values.any { it }) {
            Toast.makeText(this, "Alertas de embarque activadas", Toast.LENGTH_SHORT).show()
        }
    }

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
        actualizarEstadoPermisoContactos()
        actualizarCalculoCosto()
    }

    override fun onResume() {
        super.onResume()
        actualizarEstadoPermisoContactos()
    }

    private fun inicializarVistas() {
        toolbarReserva = findViewById(R.id.toolbarReserva)
        pe.edu.utp.perviajero.util.WindowInsetsHelper.applyToolbarActivityInsets(this, toolbarReserva)
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
        btnImportarContactos = findViewById(R.id.btnImportarContactos)
        btnRegistrarPasajeros = findViewById(R.id.btnRegistrarPasajeros)
        tvEstadoPermisoContactos = findViewById(R.id.tvEstadoPermisoContactos)

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
            .placeholder(R.drawable.bg_destino_placeholder)
            .error(R.drawable.bg_destino_placeholder)
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
        val sessionRepo = ServiceLocator.getSessionRepository(this)
        val usuario = sessionRepo.getUsuarioActual() ?: sessionRepo.getQuickAccessUser(pe.edu.utp.perviajero.data.model.RolUsuario.CLIENTE)
        val nombreTitular = usuario.nombre

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

        btnImportarContactos.setOnClickListener {
            gestorPermisos.ejecutarConPermiso(
                permisos = permisosContactos,
                lanzador = lanzadorContactos,
                motivo = "Leemos tu agenda solo para ayudarte a completar rápido los nombres de tus acompañantes. Si prefieres, puedes registrarlos manualmente."
            ) {
                importarAcompanantesDesdeContactos()
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
            solicitarPermisoNotificacionEnContexto()
            iniciarPasarelaDePago()
        }
    }

    private fun solicitarPermisoNotificacionEnContexto() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permisosNotif = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
            if (gestorPermisos.estadoDe(permisosNotif) == GestorPermisos.Estado.PENDIENTE) {
                lanzadorNotificaciones.launch(permisosNotif)
            }
        }
    }

    private fun actualizarEstadoPermisoContactos() {
        val estado = gestorPermisos.estadoDe(permisosContactos)
        tvEstadoPermisoContactos.text = when (estado) {
            GestorPermisos.Estado.CONCEDIDO -> "✅ Contactos activos: puedes importar acompañantes o escribirlos a mano."
            GestorPermisos.Estado.PENDIENTE -> "ℹ️ Importa acompañantes de tu agenda o regístralos manualmente."
            GestorPermisos.Estado.DENEGADO -> "⚠️ Sin acceso a contactos. El registro manual de DNI sigue disponible."
            GestorPermisos.Estado.BLOQUEADO -> "🚫 Contactos bloqueados (Ajustes). Modo manual activo sin restricciones."
        }
        btnRegistrarPasajeros.text = "Registrar DNI (${listaManifiesto.size}/$cantidadPasajeros)"
    }

    private fun importarAcompanantesDesdeContactos() {
        val colNombre = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        val colNumero = ContactsContract.CommonDataKinds.Phone.NUMBER
        val encontrados = mutableListOf<ContactoAgenda>()

        try {
            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(colNombre, colNumero),
                null,
                null,
                "$colNombre ASC"
            )?.use { cursor ->
                val idxNombre = cursor.getColumnIndexOrThrow(colNombre)
                val idxNumero = cursor.getColumnIndexOrThrow(colNumero)
                while (cursor.moveToNext()) {
                    encontrados.add(
                        ContactoAgenda(
                            nombre = cursor.getString(idxNombre) ?: "",
                            telefono = cursor.getString(idxNumero) ?: ""
                        )
                    )
                }
            }
        } catch (_: SecurityException) {
            Toast.makeText(this, "No hay permiso para leer contactos", Toast.LENGTH_SHORT).show()
            actualizarEstadoPermisoContactos()
            return
        }

        val contactos = ContactoImportHelper.deduplicar(encontrados)
        if (contactos.isEmpty()) {
            Toast.makeText(this, "No se encontraron contactos con teléfono; regístralos a mano.", Toast.LENGTH_LONG).show()
            return
        }

        val etiquetas = contactos.map { "${it.nombre} (${it.telefono})" }.toTypedArray()
        val marcados = BooleanArray(contactos.size)

        MaterialAlertDialogBuilder(this)
            .setTitle("Importar acompañantes (máx. 10)")
            .setMultiChoiceItems(etiquetas, marcados) { _, which, isChecked ->
                marcados[which] = isChecked
            }
            .setPositiveButton("Agregar al manifiesto") { _, _ ->
                val elegidos = contactos.filterIndexed { idx, _ -> marcados[idx] }
                if (elegidos.isNotEmpty()) {
                    val nuevaLista = ContactoImportHelper.agregarAlManifiesto(listaManifiesto, elegidos, 10)
                    listaManifiesto.clear()
                    listaManifiesto.addAll(nuevaLista)
                    cantidadPasajeros = listaManifiesto.size.coerceIn(1, 10)
                    tvCantidadPasajeros.text = cantidadPasajeros.toString()
                    actualizarCalculoCosto()
                    actualizarEstadoPermisoContactos()
                    Toast.makeText(this, "${elegidos.size} acompañante(s) agregado(s) al manifiesto", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
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
        return CotizadorCalculator.calcularTotal(
            cantidadPasajeros = cantidadPasajeros,
            extraMovilidadPorPersona = extraMovilidad,
            extraHotelPorPersona = extraHotel,
            extraGuiaGrupo = extraGuia
        )
    }

    private fun actualizarCalculoCosto() {
        val total = calcularCostoTotal()
        tvCostoTotal.text = String.format(Locale.US, "S/ %.2f", total)
    }

    private fun iniciarPasarelaDePago() {
        val dest = destino ?: return
        if (fechaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Por favor selecciona una fecha de salida", Toast.LENGTH_SHORT).show()
            return
        }

        val total = calcularCostoTotal()
        val subtotal = total / 1.18
        val igv = total - subtotal

        val dialogView = layoutInflater.inflate(R.layout.dialog_pasarela_pago, null)
        val tvPasarelaDestinoResumen: TextView = dialogView.findViewById(R.id.tvPasarelaDestinoResumen)
        val tvPasarelaDesgloseIgv: TextView = dialogView.findViewById(R.id.tvPasarelaDesgloseIgv)
        val tvPasarelaTotal: TextView = dialogView.findViewById(R.id.tvPasarelaTotal)

        val rgMetodosPago: RadioGroup = dialogView.findViewById(R.id.rgMetodosPago)
        val layoutFormularioTarjeta: LinearLayout = dialogView.findViewById(R.id.layoutFormularioTarjeta)
        val layoutFormularioYape: LinearLayout = dialogView.findViewById(R.id.layoutFormularioYape)
        val layoutFormularioTransferencia: LinearLayout = dialogView.findViewById(R.id.layoutFormularioTransferencia)

        val chipDemoTarjeta: com.google.android.material.chip.Chip = dialogView.findViewById(R.id.chipDemoTarjeta)
        val etNumeroTarjeta: EditText = dialogView.findViewById(R.id.etNumeroTarjeta)
        val etVencimientoTarjeta: EditText = dialogView.findViewById(R.id.etVencimientoTarjeta)
        val etCvvTarjeta: EditText = dialogView.findViewById(R.id.etCvvTarjeta)
        val etTitularTarjeta: EditText = dialogView.findViewById(R.id.etTitularTarjeta)

        val chipDemoYape: com.google.android.material.chip.Chip = dialogView.findViewById(R.id.chipDemoYape)
        val etCodigoOperacionYape: EditText = dialogView.findViewById(R.id.etCodigoOperacionYape)
        val tvCodigoCip: TextView = dialogView.findViewById(R.id.tvCodigoCip)

        val layoutProcesandoPago: LinearLayout = dialogView.findViewById(R.id.layoutProcesandoPago)
        val tvEstadoProcesando: TextView = dialogView.findViewById(R.id.tvEstadoProcesando)

        val btnEjecutarPago: MaterialButton = dialogView.findViewById(R.id.btnEjecutarPago)
        val btnCancelarPago: MaterialButton = dialogView.findViewById(R.id.btnCancelarPago)

        tvPasarelaDestinoResumen.text = "${dest.nombre} • $cantidadPasajeros pasajero(s)"
        tvPasarelaDesgloseIgv.text = String.format(Locale.US, "Subtotal: S/ %.2f • IGV (18%%): S/ %.2f", subtotal, igv)
        tvPasarelaTotal.text = String.format(Locale.US, "S/ %.2f", total)
        btnEjecutarPago.text = String.format(Locale.US, "Pagar S/ %.2f con Cifrado SSL", total)

        val usuario = ServiceLocator.getSessionRepository(this).getUsuarioActual()
        if (usuario != null && usuario.nombre.isNotEmpty()) {
            etTitularTarjeta.setText(usuario.nombre.uppercase(Locale.US))
        }

        val cipAleatorio = "CIP: " + (8000000..8999999).random() + " (Vence en 24h)"
        tvCodigoCip.text = cipAleatorio

        rgMetodosPago.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbMetodoTarjeta -> {
                    layoutFormularioTarjeta.visibility = View.VISIBLE
                    layoutFormularioYape.visibility = View.GONE
                    layoutFormularioTransferencia.visibility = View.GONE
                }
                R.id.rbMetodoYape -> {
                    layoutFormularioTarjeta.visibility = View.GONE
                    layoutFormularioYape.visibility = View.VISIBLE
                    layoutFormularioTransferencia.visibility = View.GONE
                }
                R.id.rbMetodoTransferencia -> {
                    layoutFormularioTarjeta.visibility = View.GONE
                    layoutFormularioYape.visibility = View.GONE
                    layoutFormularioTransferencia.visibility = View.VISIBLE
                }
            }
        }

        chipDemoTarjeta.setOnClickListener {
            etNumeroTarjeta.setText("4557 8912 3456 7890")
            etVencimientoTarjeta.setText("12/28")
            etCvvTarjeta.setText("742")
            Toast.makeText(this, "Datos de tarjeta de prueba cargados", Toast.LENGTH_SHORT).show()
        }

        chipDemoYape.setOnClickListener {
            val op = "" + (100000..999999).random()
            etCodigoOperacionYape.setText(op)
            Toast.makeText(this, "Código de aprobación Yape simulado: $op", Toast.LENGTH_SHORT).show()
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancelarPago.setOnClickListener {
            dialog.dismiss()
        }

        btnEjecutarPago.setOnClickListener {
            val metodoSeleccionado = when (rgMetodosPago.checkedRadioButtonId) {
                R.id.rbMetodoYape -> "📱 Yape Móvil"
                R.id.rbMetodoTransferencia -> "🏦 Transferencia / CIP"
                else -> "💳 Tarjeta Débito / Crédito"
            }

            val numOperacion = when (rgMetodosPago.checkedRadioButtonId) {
                R.id.rbMetodoYape -> "YAPE-" + etCodigoOperacionYape.text.toString().trim().ifEmpty { (100000..999999).random().toString() }
                R.id.rbMetodoTransferencia -> "CIP-" + (8000000..8999999).random()
                else -> "VISA-OP-" + (10000..99999).random()
            }

            // Simular validación bancaria con estados dinámicos
            layoutFormularioTarjeta.visibility = View.GONE
            layoutFormularioYape.visibility = View.GONE
            layoutFormularioTransferencia.visibility = View.GONE
            rgMetodosPago.visibility = View.GONE
            btnEjecutarPago.isEnabled = false
            btnCancelarPago.isEnabled = false
            layoutProcesandoPago.visibility = View.VISIBLE

            val handler = android.os.Handler(android.os.Looper.getMainLooper())

            tvEstadoProcesando.text = "1/3 Conectando con la pasarela bancaria..."

            handler.postDelayed({
                tvEstadoProcesando.text = "2/3 Validando fondos y token de seguridad..."
            }, 800)

            handler.postDelayed({
                tvEstadoProcesando.text = "3/3 ¡Pago aprobado con éxito! Generando voucher..."
            }, 1600)

            handler.postDelayed({
                dialog.dismiss()
                guardarReservaExitosa(metodoSeleccionado, numOperacion)
            }, 2300)
        }

        dialog.show()
    }

    private fun guardarReservaExitosa(metodoPago: String, numeroOperacion: String) {
        val dest = destino ?: return
        val sessionRepo = ServiceLocator.getSessionRepository(this)
        val reservaRepo = ServiceLocator.getReservaRepository(this)
        val usuario = sessionRepo.getUsuarioActual() ?: sessionRepo.getQuickAccessUser(pe.edu.utp.perviajero.data.model.RolUsuario.CLIENTE)
        val empresa = sessionRepo.getEmpresaInfo()
        val nombreUsuario = usuario.nombre
        val emailUsuario = usuario.email

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
            metodoPago = metodoPago,
            numeroOperacion = numeroOperacion,
            estado = "Confirmada",
            usuarioNombre = nombreUsuario,
            usuarioEmail = emailUsuario
        )

        reservaRepo.guardarReserva(nuevaReserva)
        NotificationHelper.notificarReservaConfirmada(this, nuevaReserva)
        ReminderScheduler.programarRecordatorioViaje(
            this,
            "Recordatorio de Embarque • $codigoGenerado",
            "Tu tour a ${dest.nombre} sale el $fechaSeleccionada ($horarioElegido) desde $terminalElegido.",
            delaySeconds = 20L
        )

        val resumenPasajeros = listaManifiesto.joinToString("\n") { "   • ${it.nombresApellidos} (DNI: ${it.numeroDocumento}, ${it.edad} años)" }

        MaterialAlertDialogBuilder(this)
            .setTitle("¡Pago Aprobado y Voucher Emitido! 💳✅")
            .setMessage(
                "${empresa.razonSocial}\n" +
                "RUC: ${empresa.ruc} • Reg. ${empresa.registroMincetur}\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "• Código: $codigoGenerado\n" +
                "• Destino: ${dest.nombre}\n" +
                "• Fecha de Salida: $fechaSeleccionada\n" +
                "• Horario: $horarioElegido\n" +
                "• Terminal: $terminalElegido\n" +
                "• Transporte: $nombreMovilidad\n" +
                "• Alojamiento: $nombreHotel\n" +
                "• Guía Oficial: $nombreGuia\n\n" +
                "💳 Medio de Pago: $metodoPago\n" +
                "🔢 N° Operación: $numeroOperacion\n\n" +
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
