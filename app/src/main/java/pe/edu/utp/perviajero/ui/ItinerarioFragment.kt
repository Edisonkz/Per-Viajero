package pe.edu.utp.perviajero.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.adapter.ActividadItinerarioAdapter
import pe.edu.utp.perviajero.data.model.ActividadItinerario
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.util.ReminderScheduler
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Planificador interactivo de actividades turísticas (Tab 4 y contenedor reutilizable en ItinerarioActivity).
 * Todas las actividades iniciales y sugerencias provienen de assets/services/ vía ItinerarioRepository.
 */
class ItinerarioFragment : Fragment() {

    companion object {
        const val ARG_LUGAR_INICIAL = "arg_lugar_inicial"
        private val DIAS_CHIPS = listOf("Todos", "Día 1", "Día 2", "Día 3", "Día 4", "Día 5", "Día 6", "Día 7")

        fun newInstance(lugarInicial: String = ""): ItinerarioFragment {
            return ItinerarioFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_LUGAR_INICIAL, lugarInicial)
                }
            }
        }
    }

    private lateinit var tvProgreso: TextView
    private lateinit var pbProgreso: ProgressBar
    private lateinit var btnSugerencias: ImageButton
    private lateinit var btnCompartir: ImageButton

    private lateinit var btnModoDias: TextView
    private lateinit var btnModoCalendario: TextView
    private lateinit var contenedorVistaDias: LinearLayout
    private lateinit var contenedorVistaCalendario: LinearLayout
    private lateinit var chipGroupDias: ChipGroup
    private lateinit var calendarView: CalendarView
    private lateinit var tvFechaCalendario: TextView

    private lateinit var rvItinerario: RecyclerView
    private lateinit var layoutSinActividades: View
    private lateinit var fabNuevaActividad: ExtendedFloatingActionButton
    private lateinit var adapter: ActividadItinerarioAdapter

    private var diaFiltroSeleccionado: String = "Todos"
    private var esModoCalendario: Boolean = false
    private var fechaCalendarioSeleccionada: String = ""
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_itinerario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fechaCalendarioSeleccionada = sdf.format(Calendar.getInstance().time)

        tvProgreso = view.findViewById(R.id.tvProgresoItinerario)
        pbProgreso = view.findViewById(R.id.pbProgresoItinerario)
        btnSugerencias = view.findViewById(R.id.btnItinerarioSugerencias)
        btnCompartir = view.findViewById(R.id.btnCompartirItinerario)

        btnModoDias = view.findViewById(R.id.btnModoDias)
        btnModoCalendario = view.findViewById(R.id.btnModoCalendario)
        contenedorVistaDias = view.findViewById(R.id.contenedorVistaDias)
        contenedorVistaCalendario = view.findViewById(R.id.contenedorVistaCalendario)
        chipGroupDias = view.findViewById(R.id.chipGroupDias)
        calendarView = view.findViewById(R.id.calendarViewItinerario)
        tvFechaCalendario = view.findViewById(R.id.tvFechaSeleccionadaCalendario)

        rvItinerario = view.findViewById(R.id.rvItinerario)
        layoutSinActividades = view.findViewById(R.id.layoutSinActividades)
        fabNuevaActividad = view.findViewById(R.id.fabNuevaActividad)

        tvFechaCalendario.text = "Fecha seleccionada: $fechaCalendarioSeleccionada"

        rvItinerario.layoutManager = LinearLayoutManager(requireContext())
        adapter = ActividadItinerarioAdapter(
            onToggleCompletada = { item ->
                pe.edu.utp.perviajero.ui.common.AuthGuardHelper.requireAuth(
                    requireContext(),
                    "Inicia sesión para editar tu itinerario",
                    "Como visitante puedes revisar el plan de viaje. Inicia sesión para marcar actividades completadas y guardar tu progreso."
                ) {
                    ServiceLocator.getItinerarioRepository(requireContext()).toggleActividadCompletada(item.id)
                    cargarItinerario()
                }
            },
            onEliminar = { item ->
                pe.edu.utp.perviajero.ui.common.AuthGuardHelper.requireAuth(
                    requireContext(),
                    "Inicia sesión para gestionar tu itinerario",
                    "Inicia sesión para modificar o eliminar actividades de tu itinerario personal."
                ) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Eliminar Actividad")
                        .setMessage("¿Deseas quitar \"${item.lugar}\" de tu itinerario?")
                        .setPositiveButton("Eliminar") { _, _ ->
                            ServiceLocator.getItinerarioRepository(requireContext()).eliminarActividad(item.id)
                            Toast.makeText(requireContext(), "Actividad eliminada", Toast.LENGTH_SHORT).show()
                            cargarItinerario()
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            }
        )
        rvItinerario.adapter = adapter

        configurarChipsDias()
        configurarEventos()
        cargarItinerario()

        val lugarInicial = arguments?.getString(ARG_LUGAR_INICIAL).orEmpty()
        if (lugarInicial.isNotEmpty()) {
            mostrarDialogoNuevaActividad(lugarInicial = lugarInicial)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarItinerario()
    }

    private fun configurarChipsDias() {
        chipGroupDias.removeAllViews()
        for (dia in DIAS_CHIPS) {
            val chip = Chip(requireContext()).apply {
                id = View.generateViewId()
                text = dia
                isCheckable = true
                isChecked = dia == diaFiltroSeleccionado
                checkedIcon = null
                shapeAppearanceModel = shapeAppearanceModel.toBuilder().setAllCornerSizes(100f).build()
                aplicarEstiloChip(this, isChecked)
            }
            chipGroupDias.addView(chip)
        }

        chipGroupDias.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            for (i in 0 until group.childCount) {
                val c = group.getChildAt(i) as? Chip ?: continue
                aplicarEstiloChip(c, c.id == checkedId)
            }
            val chip = group.findViewById<Chip>(checkedId) ?: return@setOnCheckedStateChangeListener
            diaFiltroSeleccionado = chip.text.toString()
            cargarItinerario()
        }
    }

    private fun aplicarEstiloChip(chip: Chip, isSelected: Boolean) {
        val primary = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        val cardBg = ContextCompat.getColor(requireContext(), R.color.colorCardBackground)
        val textPrimary = ContextCompat.getColor(requireContext(), R.color.colorTextPrimary)
        if (isSelected) {
            chip.chipBackgroundColor = ColorStateList.valueOf(primary)
            chip.setTextColor(Color.WHITE)
            chip.chipStrokeWidth = 0f
        } else {
            chip.chipBackgroundColor = ColorStateList.valueOf(cardBg)
            chip.setTextColor(textPrimary)
            chip.chipStrokeColor = ColorStateList.valueOf(Color.argb(40, 0, 0, 0))
            chip.chipStrokeWidth = 2f
        }
    }

    private fun configurarEventos() {
        btnModoDias.setOnClickListener {
            esModoCalendario = false
            actualizarToggleVisual()
            contenedorVistaDias.visibility = View.VISIBLE
            contenedorVistaCalendario.visibility = View.GONE
            cargarItinerario()
        }

        btnModoCalendario.setOnClickListener {
            esModoCalendario = true
            actualizarToggleVisual()
            contenedorVistaDias.visibility = View.GONE
            contenedorVistaCalendario.visibility = View.VISIBLE
            cargarItinerario()
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
            fechaCalendarioSeleccionada = sdf.format(cal.time)
            tvFechaCalendario.text = "Fecha seleccionada: $fechaCalendarioSeleccionada"
            cargarItinerario()
        }

        fabNuevaActividad.setOnClickListener {
            pe.edu.utp.perviajero.ui.common.AuthGuardHelper.requireAuth(
                requireContext(),
                "Crea tu itinerario personal",
                "Inicia sesión para agregar actividades personalizadas y programar recordatorios de viaje."
            ) {
                mostrarDialogoNuevaActividad(
                    fechaInicial = if (esModoCalendario) fechaCalendarioSeleccionada else ""
                )
            }
        }

        btnSugerencias.setOnClickListener {
            pe.edu.utp.perviajero.ui.common.AuthGuardHelper.requireAuth(
                requireContext(),
                "Importar rutas sugeridas",
                "Inicia sesión para cargar planes semanales recomendados en tu itinerario personal."
            ) {
                mostrarDialogoSugerenciasSemanales()
            }
        }

        btnCompartir.setOnClickListener {
            compartirItinerarioCompleto()
        }
    }

    private fun actualizarToggleVisual() {
        val textPrimary = ContextCompat.getColor(requireContext(), R.color.colorTextPrimary)
        if (!esModoCalendario) {
            btnModoDias.setBackgroundResource(R.drawable.bg_badge_categoria)
            btnModoDias.setTextColor(Color.WHITE)
            btnModoCalendario.background = null
            btnModoCalendario.setTextColor(textPrimary)
        } else {
            btnModoCalendario.setBackgroundResource(R.drawable.bg_badge_categoria)
            btnModoCalendario.setTextColor(Color.WHITE)
            btnModoDias.background = null
            btnModoDias.setTextColor(textPrimary)
        }
    }

    private fun cargarItinerario() {
        val todas = ServiceLocator.getItinerarioRepository(requireContext()).getItinerario()
        val total = todas.size
        val completadas = todas.count { it.completada }
        val porcentaje = if (total > 0) (completadas * 100) / total else 0

        tvProgreso.text = "Progreso del viaje: $completadas de $total actividades ($porcentaje%)"
        pbProgreso.progress = porcentaje

        val filtradas = if (esModoCalendario) {
            todas.filter { it.fecha == fechaCalendarioSeleccionada }
        } else if (diaFiltroSeleccionado == "Todos") {
            todas
        } else {
            todas.filter { it.dia.equals(diaFiltroSeleccionado, ignoreCase = true) }
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

    private fun mostrarDialogoSugerenciasSemanales() {
        val ctx = requireContext()
        val sugerenciasPorDia = ServiceLocator.getItinerarioRepository(ctx).getSugerenciasSemanales()
        val dialogView = layoutInflater.inflate(R.layout.dialog_sugerencias_semanales, null)
        val chipGroupDiasSugerencias: ChipGroup = dialogView.findViewById(R.id.chipGroupDiasSugerencias)
        val layoutContenido: LinearLayout = dialogView.findViewById(R.id.layoutContenidoSugerenciasDia)
        val btnCerrar: MaterialButton = dialogView.findViewById(R.id.btnCerrarSugerencias)

        var dialog: AlertDialog? = null

        fun renderizarSugerenciasParaDia(diaKey: String) {
            layoutContenido.removeAllViews()
            val recomendaciones = sugerenciasPorDia[diaKey].orEmpty()

            for (rec in recomendaciones) {
                val card = MaterialCardView(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(0, 0, 0, 16) }
                    radius = 12f
                    cardElevation = 2f
                    setCardBackgroundColor(Color.TRANSPARENT)
                    strokeWidth = 1
                    strokeColor = Color.parseColor("#30006837")
                }

                val row = LinearLayout(ctx).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(16, 16, 16, 16)
                }

                val tvTitulo = TextView(ctx).apply {
                    text = rec.lugar
                    textSize = 14f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(ctx, R.color.colorPrimary))
                }

                val tvSub = TextView(ctx).apply {
                    text = "${rec.horario}\n${rec.descripcion}"
                    textSize = 12f
                    setPadding(0, 4, 0, 8)
                }

                val btnAgregar = MaterialButton(ctx).apply {
                    text = "+ Añadir a mi Itinerario"
                    textSize = 12f
                    isAllCaps = false
                    cornerRadius = 10
                    setOnClickListener {
                        val diaSimple = diaKey.substringBefore(" (").trim().ifEmpty { "Día 1" }
                        val nuevaActividad = ActividadItinerario(
                            id = "sug_${System.currentTimeMillis()}",
                            dia = diaSimple,
                            fecha = fechaCalendarioSeleccionada,
                            horario = rec.horario,
                            lugar = rec.lugar,
                            descripcion = rec.descripcion,
                            completada = false
                        )
                        ServiceLocator.getItinerarioRepository(ctx).guardarActividad(nuevaActividad)
                        ReminderScheduler.programarRecordatorioViaje(
                            ctx,
                            "Itinerario: ${rec.lugar}",
                            "Actividad planificada para $diaSimple (${rec.horario}).",
                            delaySeconds = 30L
                        )
                        Toast.makeText(ctx, "¡Agregado a tu $diaSimple!", Toast.LENGTH_SHORT).show()
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

        chipGroupDiasSugerencias.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: View.NO_ID
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

        dialog = MaterialAlertDialogBuilder(ctx)
            .setView(dialogView)
            .create()

        btnCerrar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun mostrarDialogoNuevaActividad(lugarInicial: String = "", fechaInicial: String = "") {
        val ctx = requireContext()
        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 10)
        }

        val tvLabelDia = TextView(ctx).apply { text = "Día del Viaje:" }
        val spDia = Spinner(ctx).apply {
            val dias = arrayOf("Día 1", "Día 2", "Día 3", "Día 4", "Día 5", "Día 6", "Día 7")
            adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, dias)
        }

        val tvLabelFecha = TextView(ctx).apply {
            text = "Fecha en Calendario:"
            setPadding(0, 16, 0, 4)
        }
        var fechaSeleccionada = if (fechaInicial.isNotEmpty()) fechaInicial else fechaCalendarioSeleccionada
        val btnElegirFecha = MaterialButton(ctx, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
            text = fechaSeleccionada
            isAllCaps = false
            setOnClickListener {
                val cal = Calendar.getInstance()
                DatePickerDialog(
                    ctx,
                    { _, y, m, d ->
                        cal.set(y, m, d)
                        fechaSeleccionada = sdf.format(cal.time)
                        text = fechaSeleccionada
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        val tvLabelHorario = TextView(ctx).apply {
            text = "Horario:"
            setPadding(0, 16, 0, 0)
        }
        val spHorario = Spinner(ctx).apply {
            val turnos = arrayOf("05:00 AM", "08:30 AM", "12:30 PM", "03:30 PM", "07:30 PM")
            adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, turnos)
        }

        val etLugar = EditText(ctx).apply {
            hint = "Lugar o Atractivo Turístico"
            if (lugarInicial.isNotEmpty()) setText(lugarInicial)
        }

        val etNotas = EditText(ctx).apply {
            hint = "Notas personales (ej: llevar agua, repelente)"
        }

        layout.addView(tvLabelDia)
        layout.addView(spDia)
        layout.addView(tvLabelFecha)
        layout.addView(btnElegirFecha)
        layout.addView(tvLabelHorario)
        layout.addView(spHorario)
        layout.addView(etLugar)
        layout.addView(etNotas)

        MaterialAlertDialogBuilder(ctx)
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
                    ServiceLocator.getItinerarioRepository(ctx).guardarActividad(nuevaActividad)
                    ReminderScheduler.programarRecordatorioViaje(
                        ctx,
                        "Itinerario: $lugar",
                        "Recuerda tu actividad planificada para el $fechaSeleccionada ($horario).",
                        delaySeconds = 30L
                    )
                    Toast.makeText(ctx, "Actividad guardada para $dia", Toast.LENGTH_SHORT).show()
                    cargarItinerario()
                } else {
                    Toast.makeText(ctx, "Escribe el nombre del lugar", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun compartirItinerarioCompleto() {
        val actividades = ServiceLocator.getItinerarioRepository(requireContext()).getItinerario()
        if (actividades.isEmpty()) {
            Toast.makeText(requireContext(), "No tienes actividades en tu itinerario aún", Toast.LENGTH_SHORT).show()
            return
        }

        val sb = StringBuilder("MI PLAN DE VIAJE EN PERÚ\nPlanificado con PerúViajero\n\n")
        val agrupadasPorDia = actividades.groupBy { it.dia }
        for ((dia, lista) in agrupadasPorDia) {
            sb.append("• $dia:\n")
            for (act in lista) {
                val estado = if (act.completada) "[Hecho]" else "[Pendiente]"
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
        startActivity(Intent.createChooser(sendIntent, "Compartir Itinerario de Viaje"))
    }
}
