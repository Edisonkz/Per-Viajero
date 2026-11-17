package pe.edu.utp.perviajero.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.adapter.ReservaAdapter
import pe.edu.utp.perviajero.data.model.Reserva
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.ui.common.VoucherDialogHelper

// Módulo Turista: Vouchers estilo Boarding Pass con filtro por estado y accesos rápidos a Boleto, Ruta GPS y Brújula
class MisReservasFragment : Fragment() {

    companion object {
        private val ESTADOS_FILTRO = listOf("Todas", "Confirmada", "En Ruta", "Completada")
    }

    private lateinit var rvMisReservas: RecyclerView
    private lateinit var layoutSinReservas: LinearLayout
    private lateinit var tvSubtituloMisReservas: TextView
    private lateinit var chipGroupEstadoReservas: ChipGroup
    private lateinit var adapter: ReservaAdapter

    private var estadoFiltro: String = "Todas"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mis_reservas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvMisReservas = view.findViewById(R.id.rvMisReservas)
        layoutSinReservas = view.findViewById(R.id.layoutSinReservas)
        tvSubtituloMisReservas = view.findViewById(R.id.tvSubtituloMisReservas)
        chipGroupEstadoReservas = view.findViewById(R.id.chipGroupEstadoReservas)

        rvMisReservas.layoutManager = LinearLayoutManager(requireContext())
        adapter = ReservaAdapter(
            esModoAdmin = false,
            onItemClicked = { reserva -> VoucherDialogHelper.showVoucherDialog(requireContext(), reserva) },
            onRutaGpsClicked = { reserva -> VoucherDialogHelper.openMapaParaReserva(requireContext(), reserva) },
            onBrujulaClicked = { reserva -> VoucherDialogHelper.openBrujulaParaReserva(requireContext(), reserva) }
        )
        rvMisReservas.adapter = adapter

        configurarChipsFiltro()
        cargarReservas()
    }

    override fun onResume() {
        super.onResume()
        cargarReservas()
    }

    private fun configurarChipsFiltro() {
        chipGroupEstadoReservas.removeAllViews()
        for (estado in ESTADOS_FILTRO) {
            val label = when (estado) {
                "Confirmada" -> "Confirmadas"
                "Completada" -> "Completadas"
                else -> estado
            }
            val chip = Chip(requireContext()).apply {
                id = View.generateViewId()
                tag = estado
                text = label
                isCheckable = true
                isChecked = estado == estadoFiltro
                checkedIcon = null
                shapeAppearanceModel = shapeAppearanceModel.toBuilder().setAllCornerSizes(100f).build()
                aplicarEstiloChip(this, isChecked)
            }
            chipGroupEstadoReservas.addView(chip)
        }

        chipGroupEstadoReservas.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            for (i in 0 until group.childCount) {
                val c = group.getChildAt(i) as? Chip ?: continue
                aplicarEstiloChip(c, c.id == checkedId)
            }
            val chip = group.findViewById<Chip>(checkedId) ?: return@setOnCheckedStateChangeListener
            estadoFiltro = chip.tag?.toString() ?: "Todas"
            cargarReservas()
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

    private fun cargarReservas() {
        val todasLasReservas = ServiceLocator.getReservaRepository(requireContext()).getReservas()
        val usuario = ServiceLocator.getSessionRepository(requireContext()).getUsuarioActual()

        val reservasUsuario = if (usuario != null) {
            val userReservas = todasLasReservas.filter { it.usuarioEmail.equals(usuario.email, ignoreCase = true) }
            if (userReservas.isEmpty()) todasLasReservas else userReservas
        } else {
            todasLasReservas
        }

        val filtradas = if (estadoFiltro == "Todas") {
            reservasUsuario
        } else {
            reservasUsuario.filter { it.estado.equals(estadoFiltro, ignoreCase = true) }
        }

        tvSubtituloMisReservas.text = if (usuario != null) {
            "Viajero: ${usuario.nombre} • ${reservasUsuario.size} tours activos"
        } else {
            "Modo libre • ${reservasUsuario.size} tours activos"
        }

        if (filtradas.isEmpty()) {
            layoutSinReservas.visibility = View.VISIBLE
            rvMisReservas.visibility = View.GONE
        } else {
            layoutSinReservas.visibility = View.GONE
            rvMisReservas.visibility = View.VISIBLE
            adapter.actualizarLista(filtradas)
        }
    }
}
