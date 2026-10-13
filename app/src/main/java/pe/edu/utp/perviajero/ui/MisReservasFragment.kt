package pe.edu.utp.perviajero.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.adapter.ReservaAdapter
import pe.edu.utp.perviajero.data.local.PreferencesHelper

// Módulo Turista: Visualización y control de vouchers de tour emitidos
class MisReservasFragment : Fragment() {

    private lateinit var rvMisReservas: RecyclerView
    private lateinit var layoutSinReservas: LinearLayout
    private lateinit var tvSubtituloMisReservas: TextView
    private lateinit var adapter: ReservaAdapter

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

        rvMisReservas.layoutManager = LinearLayoutManager(requireContext())
        adapter = ReservaAdapter(emptyList(), esModoAdmin = false)
        rvMisReservas.adapter = adapter

        cargarReservas()
    }

    override fun onResume() {
        super.onResume()
        cargarReservas()
    }

    private fun cargarReservas() {
        val prefs = PreferencesHelper.getInstance(requireContext())
        val todasLasReservas = prefs.getReservas()
        val usuario = prefs.getUsuarioActual()

        // Filtrar reservas asociadas al usuario actual o mostrar todas si está en modo demo
        val reservasFiltradas = if (usuario != null) {
            val userReservas = todasLasReservas.filter { it.usuarioEmail.equals(usuario.email, ignoreCase = true) }
            if (userReservas.isEmpty()) todasLasReservas else userReservas
        } else {
            todasLasReservas
        }

        if (usuario != null) {
            tvSubtituloMisReservas.text = "Viajero: ${usuario.nombre} • ${reservasFiltradas.size} tours registrados"
        } else {
            tvSubtituloMisReservas.text = "Explorando en modo libre (${reservasFiltradas.size} tours disponibles)"
        }

        if (reservasFiltradas.isEmpty()) {
            layoutSinReservas.visibility = View.VISIBLE
            rvMisReservas.visibility = View.GONE
        } else {
            layoutSinReservas.visibility = View.GONE
            rvMisReservas.visibility = View.VISIBLE
            adapter.actualizarLista(reservasFiltradas)
        }
    }
}

