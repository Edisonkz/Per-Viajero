package pe.edu.utp.perviajero.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.adapter.AvisoAdapter
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.ui.common.ChipStyleHelper
import pe.edu.utp.perviajero.util.AvisoPushSimulator
import pe.edu.utp.perviajero.util.GestorPermisos
import pe.edu.utp.perviajero.util.NotificationHelper
import pe.edu.utp.perviajero.util.WindowInsetsHelper

/**
 * Bandeja persistente de Avisos Turísticos con estado leído/no leído, filtros por tipo,
 * marcado masivo y simulador de payloads JSON (con AlarmManager para pruebas con app cerrada).
 */
class BandejaAvisosActivity : AppCompatActivity() {

    companion object {
        private val TIPOS_FILTRO = listOf("Todos", "Alerta", "Reserva", "Itinerario", "Aviso")

        private const val PLANTILLA_ALERTA =
            """{"tipo":"alerta","titulo":"Alerta Climática Cusco","cuerpo":"Lluvias moderadas en la tarde en el Valle Sagrado. Lleva poncho impermeable.","region":"Cusco","url":"https://www.gob.pe/senamhi"}"""
        private const val PLANTILLA_RESERVA =
            """{"tipo":"reserva","titulo":"Puerta de Embarque Asignada","cuerpo":"Tu transporte turístico a Machu Picchu abordará por la Puerta 2 del Terminal Wanchaq.","region":"Cusco"}"""
        private const val PLANTILLA_ITINERARIO =
            """{"tipo":"itinerario","titulo":"Próxima Actividad • 08:00 AM","cuerpo":"Recorrido por el Mirador de la Cruz del Cóndor en el Cañón del Colca.","region":"Arequipa"}"""
        private const val PLANTILLA_AVISO =
            """{"tipo":"aviso","titulo":"Guía Oficial Actualizada","cuerpo":"Nuevas recomendaciones de aclimatación y teléfonos IPERÚ disponibles sin conexión.","region":"Perú","url":"https://www.peru.travel/es"}"""
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvNoLeidos: TextView
    private lateinit var btnMarcarTodos: MaterialButton
    private lateinit var btnLimpiar: MaterialButton
    private lateinit var chipGroupTipos: ChipGroup
    private lateinit var rvAvisos: RecyclerView
    private lateinit var layoutVacio: View
    private lateinit var fabSimularPush: ExtendedFloatingActionButton

    private lateinit var adapter: AvisoAdapter
    private var tipoFiltro: String = "Todos"
    private var accionPendienteNotif: (() -> Unit)? = null
    private val gestorPermisos by lazy { GestorPermisos(this) }

    private val lanzadorNotif = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gestorPermisos.marcarPreguntado(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
        }
        if (resultado.values.any { it }) {
            accionPendienteNotif?.invoke()
        } else {
            Toast.makeText(
                this,
                "Sin permiso de notificaciones; los avisos igual se guardarán en la bandeja.",
                Toast.LENGTH_LONG
            ).show()
            accionPendienteNotif?.invoke()
        }
        accionPendienteNotif = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bandeja_avisos)

        NotificationHelper.crearCanalNotificaciones(this)

        toolbar = findViewById(R.id.toolbarBandejaAvisos)
        WindowInsetsHelper.applyToolbarActivityInsets(this, toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        tvNoLeidos = findViewById(R.id.tvAvisosNoLeidos)
        btnMarcarTodos = findViewById(R.id.btnMarcarTodosLeidos)
        btnLimpiar = findViewById(R.id.btnLimpiarAvisos)
        chipGroupTipos = findViewById(R.id.chipGroupTipoAvisos)
        rvAvisos = findViewById(R.id.rvBandejaAvisos)
        layoutVacio = findViewById(R.id.layoutAvisosVacio)
        fabSimularPush = findViewById(R.id.fabSimularPush)

        adapter = AvisoAdapter { aviso ->
            startActivity(Intent(this, DetalleAvisoActivity::class.java).apply {
                putExtra(NotificationHelper.EXTRA_AVISO_ID, aviso.id)
            })
        }
        rvAvisos.layoutManager = LinearLayoutManager(this)
        rvAvisos.adapter = adapter

        configurarChips()
        configurarEventos()
        refrescarBandeja()
    }

    override fun onResume() {
        super.onResume()
        refrescarBandeja()
    }

    private fun configurarChips() {
        chipGroupTipos.removeAllViews()
        for (tipo in TIPOS_FILTRO) {
            val chip = ChipStyleHelper.createFilterChip(
                context = this,
                label = tipo,
                isCheckedInitial = tipo == tipoFiltro,
                id = View.generateViewId()
            )
            chipGroupTipos.addView(chip)
        }

        chipGroupTipos.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            for (i in 0 until group.childCount) {
                val c = group.getChildAt(i) as? Chip ?: continue
                ChipStyleHelper.applyStyle(this, c, c.id == checkedId)
            }
            val selected = group.findViewById<Chip>(checkedId) ?: return@setOnCheckedStateChangeListener
            tipoFiltro = selected.text.toString()
            refrescarBandeja()
        }
    }

    private fun configurarEventos() {
        val repo = ServiceLocator.getAvisoRepository(this)

        btnMarcarTodos.setOnClickListener {
            repo.marcarTodosLeidos()
            NotificationHelper.cancelarTodas(this)
            refrescarBandeja()
            Toast.makeText(this, "Todos los avisos marcados como leídos", Toast.LENGTH_SHORT).show()
        }

        btnLimpiar.setOnClickListener {
            repo.limpiarHistorial()
            NotificationHelper.cancelarTodas(this)
            refrescarBandeja()
            Toast.makeText(this, "Historial de avisos limpiado", Toast.LENGTH_SHORT).show()
        }

        fabSimularPush.setOnClickListener { mostrarDialogoSimuladorPush() }
    }

    private fun refrescarBandeja() {
        val repo = ServiceLocator.getAvisoRepository(this)
        val todos = repo.listarAvisos()
        val filtrados = repo.filtrarPorTipo(tipoFiltro)
        val noLeidos = todos.count { !it.leido }

        tvNoLeidos.text = "No leídos: $noLeidos de ${todos.size}"
        adapter.submitList(filtrados)

        if (filtrados.isEmpty()) {
            layoutVacio.visibility = View.VISIBLE
            rvAvisos.visibility = View.GONE
        } else {
            layoutVacio.visibility = View.GONE
            rvAvisos.visibility = View.VISIBLE
        }
    }

    private fun mostrarDialogoSimuladorPush() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(44, 24, 44, 12)
        }

        val filaPlantillas = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val etPayload = EditText(this).apply {
            setText(PLANTILLA_ALERTA)
            minLines = 4
            textSize = 12f
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }

        fun addTemplateBtn(label: String, json: String) {
            val btn = MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
                text = label
                isAllCaps = false
                textSize = 11f
                setPadding(8, 0, 8, 0)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginEnd = 6
                }
                setOnClickListener { etPayload.setText(json) }
            }
            filaPlantillas.addView(btn)
        }

        addTemplateBtn("Alerta", PLANTILLA_ALERTA)
        addTemplateBtn("Reserva", PLANTILLA_RESERVA)
        addTemplateBtn("Itiner.", PLANTILLA_ITINERARIO)
        addTemplateBtn("Aviso", PLANTILLA_AVISO)

        val etSegundos = EditText(this).apply {
            hint = "Segundos de espera (3 a 600)"
            setText("5")
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        container.addView(filaPlantillas)
        container.addView(etPayload)
        container.addView(etSegundos)

        MaterialAlertDialogBuilder(this)
            .setTitle("Simulador Push JSON (Servidor)")
            .setMessage("Puedes editar el JSON (o probar un JSON inválido para verificar tolerancia a fallos).")
            .setView(container)
            .setPositiveButton("Programar Push") { _, _ ->
                val seg = etSegundos.text.toString().toIntOrNull()?.coerceIn(3, 600) ?: 5
                val rawPayload = etPayload.text.toString()
                ejecutarConPermisoNotif {
                    AvisoPushSimulator.programarPushSimulado(this, rawPayload, seg)
                    Toast.makeText(
                        this,
                        "Push JSON programado: llegará en $seg s (incluso con la app cerrada)",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun ejecutarConPermisoNotif(accion: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            accion()
            return
        }
        val perms = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        accionPendienteNotif = accion
        gestorPermisos.ejecutarConPermiso(
            permisos = perms,
            lanzador = lanzadorNotif,
            motivo = "Activa las notificaciones para recibir alertas climáticas, confirmaciones de reserva y recordatorios de viaje."
        ) {
            accionPendienteNotif = null
            accion()
        }
    }
}
