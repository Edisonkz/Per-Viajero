package pe.edu.utp.perviajero.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pe.edu.utp.perviajero.MainActivity
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.adapter.ReservaAdapter
import pe.edu.utp.perviajero.data.model.Reserva
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.ui.common.VoucherDialogHelper
import pe.edu.utp.perviajero.util.AdminFilterHelper
import pe.edu.utp.perviajero.util.NotificationHelper
import java.util.Locale

/**
 * Panel Ejecutivo de Agencia (Rol ADMIN):
 * - Datos fiscales MINCETUR y 4 KPIs (Reservas, Ingresos, Viajeros, Tour Top).
 * - Gestión de reservas filtrables por Destino, Estado y buscador.
 * - Accesos rápidos a CRUD de Lugares Turísticos y Emisor de Alertas Push a viajeros.
 */
class AdminDashboardFragment : Fragment() {

    companion object {
        private val ESTADOS_ADMIN = listOf("Todas", "Confirmada", "En Ruta", "Completada", "Cancelada")
    }

    private lateinit var tvAdminRazonSocial: TextView
    private lateinit var tvAdminRuc: TextView
    private lateinit var tvAdminDireccion: TextView
    private lateinit var btnEditarEmpresa: MaterialButton

    private lateinit var tvKpiTotalReservas: TextView
    private lateinit var tvKpiIngresos: TextView
    private lateinit var tvKpiPasajeros: TextView
    private lateinit var tvKpiTopDestino: TextView
    private lateinit var btnAdminIrCatalogo: MaterialButton
    private lateinit var btnAdminEmitirPush: MaterialButton

    private lateinit var etBuscarReserva: EditText
    private lateinit var cardFiltroDestino: View
    private lateinit var tvFiltroDestino: TextView
    private lateinit var chipGroupEstado: ChipGroup
    private lateinit var rvAdminReservas: RecyclerView
    private lateinit var adapter: ReservaAdapter

    private var todasReservas: List<Reserva> = emptyList()
    private var destinoFiltro: String = "Todos"
    private var estadoFiltro: String = "Todas"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_admin_dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvAdminRazonSocial = view.findViewById(R.id.tvAdminRazonSocial)
        tvAdminRuc = view.findViewById(R.id.tvAdminRuc)
        tvAdminDireccion = view.findViewById(R.id.tvAdminDireccion)
        btnEditarEmpresa = view.findViewById(R.id.btnEditarEmpresa)

        tvKpiTotalReservas = view.findViewById(R.id.tvKpiTotalReservas)
        tvKpiIngresos = view.findViewById(R.id.tvKpiIngresos)
        tvKpiPasajeros = view.findViewById(R.id.tvKpiPasajeros)
        tvKpiTopDestino = view.findViewById(R.id.tvKpiTopDestino)
        btnAdminIrCatalogo = view.findViewById(R.id.btnAdminIrCatalogo)
        btnAdminEmitirPush = view.findViewById(R.id.btnAdminEmitirPush)

        etBuscarReserva = view.findViewById(R.id.etBuscarReservaAdmin)
        cardFiltroDestino = view.findViewById(R.id.cardFiltroDestinoAdmin)
        tvFiltroDestino = view.findViewById(R.id.tvFiltroDestinoAdmin)
        chipGroupEstado = view.findViewById(R.id.chipGroupEstadoAdmin)
        rvAdminReservas = view.findViewById(R.id.rvAdminReservas)

        rvAdminReservas.layoutManager = LinearLayoutManager(requireContext())
        adapter = ReservaAdapter(
            esModoAdmin = true,
            onCambiarEstadoClicked = { reserva -> mostrarDialogoCambiarEstado(reserva) },
            onItemClicked = { reserva -> VoucherDialogHelper.showVoucherDialog(requireContext(), reserva) },
            onRutaGpsClicked = { reserva -> VoucherDialogHelper.openMapaParaReserva(requireContext(), reserva) },
            onBrujulaClicked = { reserva -> VoucherDialogHelper.openBrujulaParaReserva(requireContext(), reserva) }
        )
        rvAdminReservas.adapter = adapter

        btnEditarEmpresa.setOnClickListener { mostrarDialogoEditarEmpresa() }
        btnAdminIrCatalogo.setOnClickListener {
            (activity as? MainActivity)?.seleccionarTab(MainActivity.MainTab.ITINERARIO)
        }
        btnAdminEmitirPush.setOnClickListener { mostrarDialogoEmitirAlertaAdmin() }

        configurarFiltros()
        cargarDatosDashboard()
    }

    override fun onResume() {
        super.onResume()
        cargarDatosDashboard()
    }

    private fun configurarFiltros() {
        chipGroupEstado.removeAllViews()
        for (estado in ESTADOS_ADMIN) {
            val chip = Chip(requireContext()).apply {
                id = View.generateViewId()
                text = estado
                isCheckable = true
                isChecked = estado == estadoFiltro
                checkedIcon = null
                shapeAppearanceModel = shapeAppearanceModel.toBuilder().setAllCornerSizes(100f).build()
                aplicarEstiloChipAdmin(this, isChecked)
            }
            chipGroupEstado.addView(chip)
        }

        chipGroupEstado.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            for (i in 0 until group.childCount) {
                val c = group.getChildAt(i) as? Chip ?: continue
                aplicarEstiloChipAdmin(c, c.id == checkedId)
            }
            val selected = group.findViewById<Chip>(checkedId) ?: return@setOnCheckedStateChangeListener
            estadoFiltro = selected.text.toString()
            aplicarFiltroReservas()
        }

        cardFiltroDestino.setOnClickListener {
            val destinos = listOf("Todos") + todasReservas.map { it.destinoNombre }.distinct().sorted()
            val checked = destinos.indexOfFirst { it.equals(destinoFiltro, ignoreCase = true) }.coerceAtLeast(0)
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Filtrar Reservas por Destino")
                .setSingleChoiceItems(destinos.toTypedArray(), checked) { dialog, which ->
                    destinoFiltro = destinos[which]
                    tvFiltroDestino.text = if (destinoFiltro == "Todos") "Destino: Todos ▾" else "$destinoFiltro ▾"
                    aplicarFiltroReservas()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        etBuscarReserva.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                aplicarFiltroReservas()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun aplicarEstiloChipAdmin(chip: Chip, isSelected: Boolean) {
        val adminColor = ContextCompat.getColor(requireContext(), R.color.colorAdminPrimary)
        val cardBg = ContextCompat.getColor(requireContext(), R.color.colorCardBackground)
        val textPrimary = ContextCompat.getColor(requireContext(), R.color.colorTextPrimary)
        if (isSelected) {
            chip.chipBackgroundColor = ColorStateList.valueOf(adminColor)
            chip.setTextColor(Color.WHITE)
            chip.chipStrokeWidth = 0f
        } else {
            chip.chipBackgroundColor = ColorStateList.valueOf(cardBg)
            chip.setTextColor(textPrimary)
            chip.chipStrokeColor = ColorStateList.valueOf(Color.argb(40, 0, 0, 0))
            chip.chipStrokeWidth = 2f
        }
    }

    private fun cargarDatosDashboard() {
        todasReservas = ServiceLocator.getReservaRepository(requireContext()).getReservas()
        val empresa = ServiceLocator.getSessionRepository(requireContext()).getEmpresaInfo()

        tvAdminRazonSocial.text = empresa.razonSocial
        tvAdminRuc.text = "RUC: ${empresa.ruc} • Reg: ${empresa.registroMincetur}"
        tvAdminDireccion.text = "${empresa.direccion} • Tel: ${empresa.telefono}"

        val totalReservas = todasReservas.size
        val totalIngresos = todasReservas.sumOf { it.costoTotal }
        val totalPasajeros = todasReservas.sumOf { it.cantidadPersonas }
        val topDestino = todasReservas
            .groupBy { it.destinoNombre }
            .maxByOrNull { it.value.size }
            ?.key ?: "Ninguno"

        tvKpiTotalReservas.text = totalReservas.toString()
        tvKpiIngresos.text = String.format(Locale.US, "S/ %.0f", totalIngresos)
        tvKpiPasajeros.text = totalPasajeros.toString()
        tvKpiTopDestino.text = topDestino

        aplicarFiltroReservas()
    }

    private fun aplicarFiltroReservas() {
        val filtradas = AdminFilterHelper.filtrarReservas(
            reservas = todasReservas,
            destinoFiltro = destinoFiltro,
            estadoFiltro = estadoFiltro,
            query = etBuscarReserva.text?.toString().orEmpty()
        )
        adapter.actualizarLista(filtradas)
    }

    private fun mostrarDialogoEmitirAlertaAdmin() {
        val ctx = requireContext()
        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(44, 24, 44, 12)
        }
        val tipos = listOf("alerta", "reserva", "itinerario", "aviso")
        val spTipo = Spinner(ctx).apply {
            adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, tipos)
        }
        val etTitulo = EditText(ctx).apply { hint = "Título del aviso *" }
        val etCuerpo = EditText(ctx).apply {
            hint = "Mensaje para los viajeros *"
            minLines = 3
        }
        val etRegion = EditText(ctx).apply {
            hint = "Región / Destino (ej. Cusco)"
            setText("Cusco")
        }
        val etUrl = EditText(ctx).apply { hint = "URL oficial opcional (https://...)" }

        listOf(spTipo, etTitulo, etCuerpo, etRegion, etUrl).forEach { layout.addView(it) }

        MaterialAlertDialogBuilder(ctx)
            .setTitle("Emitir Alerta Push a Viajeros")
            .setView(layout)
            .setPositiveButton("Enviar ahora") { _, _ ->
                val titulo = etTitulo.text.toString().trim()
                val cuerpo = etCuerpo.text.toString().trim()
                if (titulo.isEmpty() || cuerpo.isEmpty()) {
                    Toast.makeText(ctx, "Completa el título y mensaje del aviso", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                NotificationHelper.mostrarNotificacionPush(
                    context = ctx,
                    titulo = titulo,
                    cuerpo = cuerpo,
                    tipo = spTipo.selectedItem.toString(),
                    region = etRegion.text.toString().trim().ifEmpty { "Perú" },
                    url = etUrl.text.toString().trim()
                )
                Toast.makeText(ctx, "Alerta enviada y guardada en la Bandeja de Avisos", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEditarEmpresa() {
        val sessionRepo = ServiceLocator.getSessionRepository(requireContext())
        val empresaActual = sessionRepo.getEmpresaInfo()

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 12)
        }

        val etRazon = EditText(requireContext()).apply {
            hint = "Razón Social"
            setText(empresaActual.razonSocial)
        }
        val etRuc = EditText(requireContext()).apply {
            hint = "RUC (11 dígitos)"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(empresaActual.ruc)
        }
        val etMincetur = EditText(requireContext()).apply {
            hint = "Registro MINCETUR"
            setText(empresaActual.registroMincetur)
        }
        val etDir = EditText(requireContext()).apply {
            hint = "Dirección Fiscal"
            setText(empresaActual.direccion)
        }
        val etTel = EditText(requireContext()).apply {
            hint = "Teléfono de Contacto"
            setText(empresaActual.telefono)
        }

        listOf(etRazon, etRuc, etMincetur, etDir, etTel).forEach { layout.addView(it) }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Editar Datos Fiscales de la Agencia")
            .setView(layout)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevaEmpresa = empresaActual.copy(
                    razonSocial = etRazon.text.toString().trim().ifEmpty { empresaActual.razonSocial },
                    ruc = etRuc.text.toString().trim().ifEmpty { empresaActual.ruc },
                    registroMincetur = etMincetur.text.toString().trim().ifEmpty { empresaActual.registroMincetur },
                    direccion = etDir.text.toString().trim().ifEmpty { empresaActual.direccion },
                    telefono = etTel.text.toString().trim().ifEmpty { empresaActual.telefono }
                )
                sessionRepo.guardarEmpresaInfo(nuevaEmpresa)
                cargarDatosDashboard()
                Toast.makeText(requireContext(), "Datos corporativos actualizados", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoCambiarEstado(reserva: Reserva) {
        val estadosDisponibles = arrayOf("Confirmada", "En Ruta", "Completada", "Cancelada")
        val indiceActual = estadosDisponibles.indexOf(reserva.estado).coerceAtLeast(0)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Despacho de Tour [${reserva.codigoReserva}]")
            .setSingleChoiceItems(estadosDisponibles, indiceActual) { dialog, which ->
                val nuevoEstado = estadosDisponibles[which]
                ServiceLocator.getReservaRepository(requireContext())
                    .actualizarEstadoReserva(reserva.codigoReserva, nuevoEstado)
                NotificationHelper.notificarActualizacionEstado(
                    requireContext(),
                    reserva.codigoReserva,
                    reserva.destinoNombre,
                    nuevoEstado
                )
                cargarDatosDashboard()
                Toast.makeText(requireContext(), "Reserva actualizada a: $nuevoEstado", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
