package pe.edu.utp.perviajero.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.adapter.ActividadItinerarioAdapter
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.ActividadItinerario

// Planificador interactivo de actividades turísticas día a día con sugerencias y progreso
class ItinerarioActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_LUGAR_INICIAL = "extra_lugar_inicial"
    }

    private lateinit var toolbarItinerario: MaterialToolbar
    private lateinit var tvProgresoItinerario: TextView
    private lateinit var pbProgresoItinerario: ProgressBar
    private lateinit var chipGroupDias: ChipGroup
    private lateinit var rvItinerario: RecyclerView
    private lateinit var layoutSinActividades: LinearLayout
    private lateinit var btnCompartirItinerario: MaterialButton
    private lateinit var btnNuevaActividad: MaterialButton
    private lateinit var adapter: ActividadItinerarioAdapter

    private var diaFiltroSeleccionado: String = "Todos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_itinerario)

        inicializarVistas()
        configurarChipsSugerencias()
        configurarEventos()
        cargarItinerario()

        // Si se abrió con un destino sugerido desde DetalleDestinoActivity
        val lugarInicial = intent.getStringExtra(EXTRA_LUGAR_INICIAL)
        if (!lugarInicial.isNullOrEmpty()) {
            mostrarDialogoNuevaActividad(lugarInicial)
        }
    }

    private fun inicializarVistas() {
        toolbarItinerario = findViewById(R.id.toolbarItinerario)
        tvProgresoItinerario = findViewById(R.id.tvProgresoItinerario)
        pbProgresoItinerario = findViewById(R.id.pbProgresoItinerario)
        chipGroupDias = findViewById(R.id.chipGroupDias)
        rvItinerario = findViewById(R.id.rvItinerario)
        layoutSinActividades = findViewById(R.id.layoutSinActividades)
        btnCompartirItinerario = findViewById(R.id.btnCompartirItinerario)
        btnNuevaActividad = findViewById(R.id.btnNuevaActividad)

        setSupportActionBar(toolbarItinerario)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbarItinerario.setNavigationOnClickListener {
            finish()
        }

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

    private fun configurarChipsSugerencias() {
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
                mostrarDialogoNuevaActividad(lugar)
            }
        }
    }

    private fun configurarEventos() {
        // Filtrar actividades por día
        chipGroupDias.setOnCheckedChangeListener { _, checkedId ->
            diaFiltroSeleccionado = when (checkedId) {
                R.id.chipDia1 -> "Día 1"
                R.id.chipDia2 -> "Día 2"
                R.id.chipDia3 -> "Día 3"
                R.id.chipDia4 -> "Día 4"
                R.id.chipDia5 -> "Día 5"
                else -> "Todos"
            }
            cargarItinerario()
        }

        btnNuevaActividad.setOnClickListener {
            mostrarDialogoNuevaActividad()
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

        // Aplicar Filtro de Día
        val filtradas = if (diaFiltroSeleccionado == "Todos") {
            todasLasActividades
        } else {
            todasLasActividades.filter { it.dia.equals(diaFiltroSeleccionado, ignoreCase = true) }
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

    private fun mostrarDialogoNuevaActividad(lugarInicial: String = "") {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 10)
        }

        val tvLabelDia = TextView(this).apply { text = "Día del Viaje:" }
        val spDia = Spinner(this).apply {
            val dias = arrayOf("Día 1", "Día 2", "Día 3", "Día 4", "Día 5")
            adapter = ArrayAdapter(this@ItinerarioActivity, android.R.layout.simple_spinner_dropdown_item, dias)
        }

        val tvLabelHorario = TextView(this).apply {
            text = "Horario / Momento del Día:"
            setPadding(0, 20, 0, 0)
        }
        val spHorario = Spinner(this).apply {
            val turnos = arrayOf(
                "Mañana (08:00 AM)",
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
            hint = "Notas manuales (ej: llevar agua, comprar boletos, tips)"
        }

        layout.addView(tvLabelDia)
        layout.addView(spDia)
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
                        horario = horario,
                        lugar = lugar,
                        descripcion = if (notas.isNotEmpty()) notas else "Visita turística planificada.",
                        completada = false
                    )
                    PreferencesHelper.getInstance(this).guardarActividad(nuevaActividad)
                    Toast.makeText(this, "Actividad agregada a tu $dia", Toast.LENGTH_SHORT).show()
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

        val sb = StringBuilder("🗺️ MI ITINERARIO DE VIAJE EN PERÚ 🇵🇪\n")
        sb.append("Planificado con la aplicación móvil PerúViajero\n\n")

        val agrupadasPorDia = actividades.groupBy { it.dia }
        for ((dia, lista) in agrupadasPorDia) {
            sb.append("📌 $dia:\n")
            for (act in lista) {
                val estado = if (act.completada) "✅" else "⏳"
                sb.append("  $estado ${act.horario} - ${act.lugar}\n")
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

