package pe.edu.utp.perviajero.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CalendarView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.adapter.ActividadItinerarioAdapter
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.ActividadItinerario
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Planificador interactivo de actividades turísticas semanales con sugerencias y calendario
class ItinerarioActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_LUGAR_INICIAL = "extra_lugar_inicial"
    }

    private lateinit var toolbarItinerario: MaterialToolbar
    private lateinit var tvProgresoItinerario: TextView
    private lateinit var pbProgresoItinerario: ProgressBar

    // Selector de Modo (Días vs Calendario)
    private lateinit var toggleGrupoVistas: MaterialButtonToggleGroup
    private lateinit var btnModoDias: MaterialButton
    private lateinit var btnModoCalendario: MaterialButton
    private lateinit var contenedorVistaDias: LinearLayout
    private lateinit var contenedorVistaCalendario: LinearLayout

    // Vista por Días
    private lateinit var chipGroupDias: ChipGroup
    private lateinit var tvAbrirSugerenciasSemanales: TextView

    // Vista Calendario
    private lateinit var calendarViewItinerario: CalendarView
    private lateinit var tvFechaSeleccionadaCalendario: TextView
    private lateinit var btnAgregarAFechaCalendario: MaterialButton

    // Lista y Estado Vacío
    private lateinit var rvItinerario: RecyclerView
    private lateinit var layoutSinActividades: LinearLayout
    private lateinit var btnCompartirItinerario: MaterialButton
    private lateinit var btnNuevaActividad: MaterialButton
    private lateinit var adapter: ActividadItinerarioAdapter

    private var diaFiltroSeleccionado: String = "Todos"
    private var esModoCalendario: Boolean = false
    private var fechaCalendarioSeleccionada: String = ""

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_itinerario)

        // Fecha inicial por defecto (hoy)
        fechaCalendarioSeleccionada = sdf.format(Calendar.getInstance().time)

        inicializarVistas()
        configurarChipsSugerenciasRapidas()
        configurarEventos()
        cargarItinerario()

        // Si se abrió con un destino sugerido desde DetalleDestinoActivity
        val lugarInicial = intent.getStringExtra(EXTRA_LUGAR_INICIAL)
        if (!lugarInicial.isNullOrEmpty()) {
            mostrarDialogoNuevaActividad(lugarInicial = lugarInicial)
        }
    }

    private fun inicializarVistas() {
        toolbarItinerario = findViewById(R.id.toolbarItinerario)
        tvProgresoItinerario = findViewById(R.id.tvProgresoItinerario)
        pbProgresoItinerario = findViewById(R.id.pbProgresoItinerario)

        toggleGrupoVistas = findViewById(R.id.toggleGrupoVistas)
        btnModoDias = findViewById(R.id.btnModoDias)
        btnModoCalendario = findViewById(R.id.btnModoCalendario)
        contenedorVistaDias = findViewById(R.id.contenedorVistaDias)
        contenedorVistaCalendario = findViewById(R.id.contenedorVistaCalendario)

        chipGroupDias = findViewById(R.id.chipGroupDias)
        tvAbrirSugerenciasSemanales = findViewById(R.id.tvAbrirSugerenciasSemanales)

        calendarViewItinerario = findViewById(R.id.calendarViewItinerario)
        tvFechaSeleccionadaCalendario = findViewById(R.id.tvFechaSeleccionadaCalendario)
        btnAgregarAFechaCalendario = findViewById(R.id.btnAgregarAFechaCalendario)

        rvItinerario = findViewById(R.id.rvItinerario)
        layoutSinActividades = findViewById(R.id.layoutSinActividades)
        btnCompartirItinerario = findViewById(R.id.btnCompartirItinerario)
        btnNuevaActividad = findViewById(R.id.btnNuevaActividad)

        setSupportActionBar(toolbarItinerario)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbarItinerario.setNavigationOnClickListener {
            finish()
        }

        tvFechaSeleccionadaCalendario.text = "📅 $fechaCalendarioSeleccionada (Hoy)"

        rvItinerario.layoutManager = LinearLayoutManager(this)
        adapter = ActividadItinerarioAdapter(
            emptyList(),
            onToggleCompletada = { item ->
                val prefs = PreferencesHelper.getInstance(this)
                prefs.toggleActividadCompletada(item.id)
                cargarItinerario()
            },
            onEliminar = { item ->
                MaterialAlertDialogBuilder(this)
                    .setTitle("Eliminar Actividad")
                    .setMessage("¿Deseas quitar \"${item.lugar}\" de tu itinerario?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        PreferencesHelper.getInstance(this).eliminarActividad(item.id)
                        Toast.makeText(this, "Actividad eliminada", Toast.LENGTH_SHORT).show()
                        cargarItinerario()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        rvItinerario.adapter = adapter
    }

    private fun configurarChipsSugerenciasRapidas() {
        val sugerencias = mapOf(
            R.id.chipSugMachuPicchu to "Santuario Histórico de Machu Picchu",
            R.id.chipSugVinicunca to "Montaña de 7 Colores (Vinicunca)",
            R.id.chipSugValleSagrado to "Valle Sagrado de los Incas",
            R.id.chipSugHuacachina to "Oasis de Huacachina",
            R.id.chipSugColca to "Cañón del Colca",
            R.id.chipSugTiticaca to "Lago Titicaca e Islas flotantes"
        )

        for ((chipId, lugar) in sugerencias) {
            findViewById<Chip?>(chipId)?.setOnClickListener {
                mostrarDialogoNuevaActividad(lugarInicial = lugar)
            }
        }
    }

    private fun configurarEventos() {
        // Alternancia entre Modo Días y Modo Calendario
        toggleGrupoVistas.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                if (checkedId == R.id.btnModoCalendario) {
                    esModoCalendario = true
                    contenedorVistaDias.visibility = View.GONE
                    contenedorVistaCalendario.visibility = View.VISIBLE
                } else {
                    esModoCalendario = false
                    contenedorVistaDias.visibility = View.VISIBLE
                    contenedorVistaCalendario.visibility = View.GONE
                }
                cargarItinerario()
            }
        }

        // Calendario Interactivo: Selección de fecha
        calendarViewItinerario.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            fechaCalendarioSeleccionada = sdf.format(cal.time)
            tvFechaSeleccionadaCalendario.text = "📅 $fechaCalendarioSeleccionada"
            cargarItinerario()
        }

        btnAgregarAFechaCalendario.setOnClickListener {
            mostrarDialogoNuevaActividad(fechaInicial = fechaCalendarioSeleccionada)
        }

        // Filtro por Día
        chipGroupDias.setOnCheckedChangeListener { _, checkedId ->
            diaFiltroSeleccionado = when (checkedId) {
                R.id.chipDia1 -> "Día 1"
                R.id.chipDia2 -> "Día 2"
                R.id.chipDia3 -> "Día 3"
                R.id.chipDia4 -> "Día 4"
                R.id.chipDia5 -> "Día 5"
                R.id.chipDia6 -> "Día 6"
                R.id.chipDia7 -> "Día 7"
                else -> "Todos"
            }
            cargarItinerario()
        }

        // Diálogo con Sugerencias Semanales a lo largo de la semana
        tvAbrirSugerenciasSemanales.setOnClickListener {
            mostrarDialogoSugerenciasSemanales()
        }

        btnNuevaActividad.setOnClickListener {
            mostrarDialogoNuevaActividad(
                fechaInicial = if (esModoCalendario) fechaCalendarioSeleccionada else ""
            )
        }

        btnCompartirItinerario.setOnClickListener {
            compartirItinerarioCompleto()
        }
    }

    private fun cargarItinerario() {
        val prefs = PreferencesHelper.getInstance(this)
        val todasLasActividades = prefs.getItinerario()

        // Calcular Progreso Global
        val total = todasLasActividades.size
        val completadas = todasLasActividades.count { it.completada }
        val porcentaje = if (total > 0) (completadas * 100) / total else 0

        tvProgresoItinerario.text = "$completadas de $total ($porcentaje%)"
        pbProgresoItinerario.progress = porcentaje

        // Filtrar según modo activo
        val filtradas = if (esModoCalendario) {
            todasLasActividades.filter { it.fecha == fechaCalendarioSeleccionada }
        } else {
            if (diaFiltroSeleccionado == "Todos") {
                todasLasActividades
            } else {
                todasLasActividades.filter { it.dia.equals(diaFiltroSeleccionado, ignoreCase = true) }
            }
        }

        if (filtradas.isEmpty()) {
            layoutSinActividades.visibility = View.VISIBLE
            rvItinerario.visibility = View.GONE
        } else {
            layoutSinActividades.visibility = View.GONE
            rvItinerario.visibility = View.VISIBLE
            adapter.actualizarLista(filtradas)
        }
    }

    // Modal con catálogo de sugerencias día por día a lo largo de la semana
    private fun mostrarDialogoSugerenciasSemanales() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_sugerencias_semanales, null)
        val chipGroupDiasSugerencias: ChipGroup = dialogView.findViewById(R.id.chipGroupDiasSugerencias)
        val layoutContenido: LinearLayout = dialogView.findViewById(R.id.layoutContenidoSugerenciasDia)
        val btnCerrar: MaterialButton = dialogView.findViewById(R.id.btnCerrarSugerencias)

        var dialog: AlertDialog? = null

        fun renderizarSugerenciasParaDia(diaKey: String) {
            layoutContenido.removeAllViews()

            val recomendaciones = when (diaKey) {
                "Día 1 (Lunes - Lima)" -> listOf(
                    Triple("Centro Histórico & Catacumbas de San Francisco", "Mañana (09:00 AM)", "Recorrido por la Plaza Mayor, Palacio de Gobierno e iglesias virreinales."),
                    Triple("Malecón de Miraflores & Larcomar", "Tarde (04:30 PM)", "Paseo con vista al Océano Pacífico y degustación de ceviche tradicional.")
                )
                "Día 2 (Martes - Ica)" -> listOf(
                    Triple("Islas Ballestas & Reserva de Paracas", "Mañana (08:00 AM)", "Navegación marina para ver lobos de mar, pingüinos y el Candelabro."),
                    Triple("Oasis de la Huacachina & Buggies", "Tarde (03:30 PM)", "Tubulares a alta velocidad en las dunas y sesión de sandboarding al atardecer.")
                )
                "Día 3 (Miér - Arequipa)" -> listOf(
                    Triple("Monasterio de Santa Catalina", "Mañana (09:30 AM)", "Ciudadela religiosa de arquitectura barroca construida en sillar blanco."),
                    Triple("Mirador de Yanahuara & Gastronomía", "Tarde (03:00 PM)", "Vista panorámica al volcán Misti y comida típica en picantería tradicional.")
                )
                "Día 4 (Juev - Cusco)" -> listOf(
                    Triple("Plaza de Armas & Templo de Qorikancha", "Mañana (09:00 AM)", "Cimientos incas del templo del Sol y arquitectura virreinal cusqueña."),
                    Triple("Complejo Arqueológico de Sacsayhuamán", "Tarde (02:30 PM)", "Imponentes murallas megalíticas de piedra tallada y vista panorámica.")
                )
                "Día 5 (Vier - Valle Sagrado)" -> listOf(
                    Triple("Parque Arqueológico de Písac", "Mañana (08:30 AM)", "Terrazas agrícolas andinas, observatorio solar y mercado artesanal."),
                    Triple("Ollantaytambo & Salineras de Maras", "Tarde (02:00 PM)", "Fortaleza inca viva y pozas milenarias de sal rosada natural.")
                )
                "Día 6 (Sáb - Machu Picchu)" -> listOf(
                    Triple("Santuario Histórico de Machu Picchu", "Mañana (07:00 AM)", "Visita guiada al circuito clásico de la ciudadela sagrada de los incas."),
                    Triple("Machupicchu Pueblo & Aguas Termales", "Noche (06:30 PM)", "Relajo en los baños termales naturales y cena típica andina.")
                )
                else -> listOf(
                    Triple("Montaña de 7 Colores (Vinicunca)", "Madrugada (05:00 AM)", "Trekking andino de alta montaña a más de 5,000 msnm con vistas espectaculares."),
                    Triple("Lago Titicaca & Islas de los Uros (Puno)", "Mañana (08:30 AM)", "Navegación lacustre en balsas de totora y convivencia comunitaria.")
                )
            }

            for ((lugar, horario, desc) in recomendaciones) {
                val card = MaterialCardView(this@ItinerarioActivity).apply {
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 0, 16)
                    }
                    layoutParams = params
                    radius = 12f
                    cardElevation = 2f
                    setCardBackgroundColor(android.graphics.Color.TRANSPARENT)
                    strokeWidth = 1
                    strokeColor = android.graphics.Color.parseColor("#30006837")
                }

                val row = LinearLayout(this@ItinerarioActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(16, 16, 16, 16)
                }

                val tvTitulo = TextView(this@ItinerarioActivity).apply {
                    text = lugar
                    textSize = 14f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setTextColor(android.graphics.Color.parseColor("#006837"))
                }

                val tvSub = TextView(this@ItinerarioActivity).apply {
                    text = "⏰ $horario\n$desc"
                    textSize = 12f
                    setPadding(0, 4, 0, 8)
                }

                val btnAgregar = MaterialButton(this@ItinerarioActivity).apply {
                    text = "+ Añadir a mi Itinerario"
                    textSize = 12f
                    isAllCaps = false
                    cornerRadius = 10
                    setOnClickListener {
                        val diaSimple = when {
                            diaKey.startsWith("Día 1") -> "Día 1"
                            diaKey.startsWith("Día 2") -> "Día 2"
                            diaKey.startsWith("Día 3") -> "Día 3"
                            diaKey.startsWith("Día 4") -> "Día 4"
                            diaKey.startsWith("Día 5") -> "Día 5"
                            diaKey.startsWith("Día 6") -> "Día 6"
                            else -> "Día 7"
                        }
                        val nuevaActividad = ActividadItinerario(
                            id = "sug_${System.currentTimeMillis()}",
                            dia = diaSimple,
                            fecha = fechaCalendarioSeleccionada,
                            horario = horario,
                            lugar = lugar,
                            descripcion = desc,
                            completada = false
                        )
                        PreferencesHelper.getInstance(this@ItinerarioActivity).guardarActividad(nuevaActividad)
                        Toast.makeText(this@ItinerarioActivity, "¡Agregado a tu $diaSimple!", Toast.LENGTH_SHORT).show()
                        cargarItinerario()
                        dialog?.dismiss()
                    }
                }

                row.addView(tvTitulo)
                row.addView(tvSub)
                row.addView(btnAgregar)
                card.addView(row)
                layoutContenido.addView(card)
            }
        }

        chipGroupDiasSugerencias.setOnCheckedChangeListener { _, checkedId ->
            val diaKey = when (checkedId) {
                R.id.chipSugMartes -> "Día 2 (Martes - Ica)"
                R.id.chipSugMiercoles -> "Día 3 (Miér - Arequipa)"
                R.id.chipSugJueves -> "Día 4 (Juev - Cusco)"
                R.id.chipSugViernes -> "Día 5 (Vier - Valle Sagrado)"
                R.id.chipSugSabado -> "Día 6 (Sáb - Machu Picchu)"
                R.id.chipSugDomingo -> "Día 7 (Dom - Vinicunca/Puno)"
                else -> "Día 1 (Lunes - Lima)"
            }
            renderizarSugerenciasParaDia(diaKey)
        }

        renderizarSugerenciasParaDia("Día 1 (Lunes - Lima)")

        dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        btnCerrar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun mostrarDialogoNuevaActividad(lugarInicial: String = "", fechaInicial: String = "") {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 10)
        }

        val tvLabelDia = TextView(this).apply { text = "Día del Viaje:" }
        val spDia = Spinner(this).apply {
            val dias = arrayOf("Día 1", "Día 2", "Día 3", "Día 4", "Día 5", "Día 6", "Día 7")
            adapter = ArrayAdapter(this@ItinerarioActivity, android.R.layout.simple_spinner_dropdown_item, dias)
        }

        val tvLabelFecha = TextView(this).apply {
            text = "Fecha en Calendario:"
            setPadding(0, 16, 0, 4)
        }
        var fechaSeleccionada = if (fechaInicial.isNotEmpty()) fechaInicial else fechaCalendarioSeleccionada
        val btnElegirFecha = MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
            text = "📅 $fechaSeleccionada"
            isAllCaps = false
            setOnClickListener {
                val cal = Calendar.getInstance()
                DatePickerDialog(
                    this@ItinerarioActivity,
                    { _, y, m, d ->
                        cal.set(y, m, d)
                        fechaSeleccionada = sdf.format(cal.time)
                        text = "📅 $fechaSeleccionada"
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        val tvLabelHorario = TextView(this).apply {
            text = "Horario / Momento del Día:"
            setPadding(0, 16, 0, 0)
        }
        val spHorario = Spinner(this).apply {
            val turnos = arrayOf(
                "Madrugada (05:00 AM)",
                "Mañana (08:30 AM)",
                "Mediodía (12:30 PM)",
                "Tarde (03:30 PM)",
                "Noche (07:30 PM)"
            )
            adapter = ArrayAdapter(this@ItinerarioActivity, android.R.layout.simple_spinner_dropdown_item, turnos)
        }

        val etLugar = EditText(this).apply {
            hint = "Lugar o Atractivo Turístico"
            if (lugarInicial.isNotEmpty()) setText(lugarInicial)
        }

        val etNotas = EditText(this).apply {
            hint = "Notas personales (ej: llevar agua, repelente, tips)"
        }

        layout.addView(tvLabelDia)
        layout.addView(spDia)
        layout.addView(tvLabelFecha)
        layout.addView(btnElegirFecha)
        layout.addView(tvLabelHorario)
        layout.addView(spHorario)
        layout.addView(etLugar)
        layout.addView(etNotas)

        MaterialAlertDialogBuilder(this)
            .setTitle("Agregar Actividad al Plan")
            .setView(layout)
            .setPositiveButton("Guardar") { dialog, _ ->
                val dia = spDia.selectedItem.toString()
                val horario = spHorario.selectedItem.toString()
                val lugar = etLugar.text.toString().trim()
                val notas = etNotas.text.toString().trim()

                if (lugar.isNotEmpty()) {
                    val nuevaActividad = ActividadItinerario(
                        id = "act_${System.currentTimeMillis()}",
                        dia = dia,
                        fecha = fechaSeleccionada,
                        horario = horario,
                        lugar = lugar,
                        descripcion = if (notas.isNotEmpty()) notas else "Visita turística planificada.",
                        completada = false
                    )
                    PreferencesHelper.getInstance(this).guardarActividad(nuevaActividad)
                    Toast.makeText(this, "Actividad guardada para $dia ($fechaSeleccionada)", Toast.LENGTH_SHORT).show()
                    cargarItinerario()
                } else {
                    Toast.makeText(this, "Por favor escribe el nombre del lugar", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun compartirItinerarioCompleto() {
        val prefs = PreferencesHelper.getInstance(this)
        val actividades = prefs.getItinerario()

        if (actividades.isEmpty()) {
            Toast.makeText(this, "No tienes actividades en tu itinerario aún", Toast.LENGTH_SHORT).show()
            return
        }

        val sb = StringBuilder("🗺️ MI PLAN DE VIAJE EN PERÚ 🇵🇪\n")
        sb.append("Planificado con la aplicación móvil PerúViajero\n\n")

        val agrupadasPorDia = actividades.groupBy { it.dia }
        for ((dia, lista) in agrupadasPorDia) {
            sb.append("📌 $dia:\n")
            for (act in lista) {
                val estado = if (act.completada) "✅" else "⏳"
                val fechaStr = if (act.fecha.isNotEmpty()) "[${act.fecha}] " else ""
                sb.append("  $estado $fechaStr${act.horario} - ${act.lugar}\n")
                if (act.descripcion.isNotEmpty()) {
                    sb.append("     Nota: ${act.descripcion}\n")
                }
            }
            sb.append("\n")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        val chooser = Intent.createChooser(sendIntent, "Compartir Itinerario de Viaje")
        startActivity(chooser)
    }
}
