package pe.edu.utp.perviajero.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.adapter.ReservaAdapter
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.Reserva
import java.util.Locale

// Módulo Administrador: Tablero de control de agencia, KPIs y gestión de estados de tour
class AdminDashboardFragment : Fragment() {

    private lateinit var tvKpiTotalReservas: TextView
    private lateinit var tvKpiIngresos: TextView
    private lateinit var tvKpiTopDestino: TextView
    private lateinit var rvAdminReservas: RecyclerView
    private lateinit var adapter: ReservaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvKpiTotalReservas = view.findViewById(R.id.tvKpiTotalReservas)
        tvKpiIngresos = view.findViewById(R.id.tvKpiIngresos)
        tvKpiTopDestino = view.findViewById(R.id.tvKpiTopDestino)
        rvAdminReservas = view.findViewById(R.id.rvAdminReservas)

        rvAdminReservas.layoutManager = LinearLayoutManager(requireContext())
        adapter = ReservaAdapter(
            emptyList(),
            esModoAdmin = true,
            onCambiarEstadoClicked = { reserva ->
                mostrarDialogoCambiarEstado(reserva)
            }
        )
        rvAdminReservas.adapter = adapter

        cargarDatosDashboard()
    }

    override fun onResume() {
        super.onResume()
        cargarDatosDashboard()
    }

    private fun cargarDatosDashboard() {
        val prefs = PreferencesHelper.getInstance(requireContext())
        val reservas = prefs.getReservas()

        // Calcular Métricas / KPIs
        val totalReservas = reservas.size
        val totalIngresos = reservas.sumOf { it.costoTotal }
        val topDestino = reservas
            .groupBy { it.destinoNombre }
            .maxByOrNull { it.value.size }
            ?.key ?: "Ninguno"

        tvKpiTotalReservas.text = totalReservas.toString()
        tvKpiIngresos.text = String.format(Locale.US, "S/ %.2f", totalIngresos)
        tvKpiTopDestino.text = topDestino

        adapter.actualizarLista(reservas)
    }

    private fun mostrarDialogoCambiarEstado(reserva: Reserva) {
        val opciones = arrayOf("Confirmada", "En Ruta", "Completada")
        var seleccionIndex = opciones.indexOf(reserva.estado)
        if (seleccionIndex == -1) seleccionIndex = 0

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Actualizar Estado • ${reserva.codigoReserva}")
            .setSingleChoiceItems(opciones, seleccionIndex) { dialog, which ->
                val nuevoEstado = opciones[which]
                val prefs = PreferencesHelper.getInstance(requireContext())
                val actualizado = prefs.actualizarEstadoReserva(reserva.codigoReserva, nuevoEstado)
                if (actualizado) {
                    Toast.makeText(
                        requireContext(),
                        "Estado cambiado a: $nuevoEstado",
                        Toast.LENGTH_SHORT
                    ).show()
                    cargarDatosDashboard()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}

