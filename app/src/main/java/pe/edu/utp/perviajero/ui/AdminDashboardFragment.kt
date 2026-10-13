package pe.edu.utp.perviajero.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.adapter.ReservaAdapter
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.EmpresaInfo
import pe.edu.utp.perviajero.data.model.Reserva
import pe.edu.utp.perviajero.util.NotificationHelper
import java.util.Locale

// Módulo Administrador: Tablero de control de agencia, edición de datos fiscales, KPIs y despacho de tours
class AdminDashboardFragment : Fragment() {

    private lateinit var tvAdminRazonSocial: TextView
    private lateinit var tvAdminRuc: TextView
    private lateinit var tvAdminDireccion: TextView
    private lateinit var btnEditarEmpresa: MaterialButton

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

        tvAdminRazonSocial = view.findViewById(R.id.tvAdminRazonSocial)
        tvAdminRuc = view.findViewById(R.id.tvAdminRuc)
        tvAdminDireccion = view.findViewById(R.id.tvAdminDireccion)
        btnEditarEmpresa = view.findViewById(R.id.btnEditarEmpresa)

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

        btnEditarEmpresa.setOnClickListener {
            mostrarDialogoEditarEmpresa()
        }

        cargarDatosDashboard()
    }

    override fun onResume() {
        super.onResume()
        cargarDatosDashboard()
    }

    private fun cargarDatosDashboard() {
        val prefs = PreferencesHelper.getInstance(requireContext())
        val reservas = prefs.getReservas()
        val empresa = prefs.getEmpresaInfo()

        // Mostrar datos de empresa
        tvAdminRazonSocial.text = empresa.razonSocial
        tvAdminRuc.text = "RUC: ${empresa.ruc} • Reg: ${empresa.registroMincetur}"
        tvAdminDireccion.text = "${empresa.direccion} • Tel: ${empresa.telefono}"

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

    private fun mostrarDialogoEditarEmpresa() {
        val prefs = PreferencesHelper.getInstance(requireContext())
        val empresaActual = prefs.getEmpresaInfo()

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 10)
        }

        val etRazonSocial = EditText(requireContext()).apply {
            hint = "Razón Social"
            setText(empresaActual.razonSocial)
        }
        val etRuc = EditText(requireContext()).apply {
            hint = "Número de RUC (11 dígitos)"
            setText(empresaActual.ruc)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val etDireccion = EditText(requireContext()).apply {
            hint = "Dirección Fiscal"
            setText(empresaActual.direccion)
        }
        val etTelefono = EditText(requireContext()).apply {
            hint = "Teléfono de Contacto"
            setText(empresaActual.telefono)
        }

        layout.addView(etRazonSocial)
        layout.addView(etRuc)
        layout.addView(etDireccion)
        layout.addView(etTelefono)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Editar Datos de la Agencia")
            .setView(layout)
            .setPositiveButton("Guardar") { dialog, _ ->
                val razonSocial = etRazonSocial.text.toString().trim()
                val ruc = etRuc.text.toString().trim()
                val direccion = etDireccion.text.toString().trim()
                val telefono = etTelefono.text.toString().trim()

                if (razonSocial.isNotEmpty() && ruc.isNotEmpty()) {
                    val nuevaEmpresa = empresaActual.copy(
                        razonSocial = razonSocial,
                        ruc = ruc,
                        direccion = if (direccion.isNotEmpty()) direccion else empresaActual.direccion,
                        telefono = if (telefono.isNotEmpty()) telefono else empresaActual.telefono
                    )
                    prefs.guardarEmpresaInfo(nuevaEmpresa)
                    Toast.makeText(requireContext(), "Datos de la empresa actualizados", Toast.LENGTH_SHORT).show()
                    cargarDatosDashboard()
                } else {
                    Toast.makeText(requireContext(), "Razón Social y RUC son requeridos", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
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
                    // Lanzar notificación nativa en Android notificando al usuario/cliente
                    NotificationHelper.notificarActualizacionEstado(
                        requireContext(),
                        reserva.codigoReserva,
                        reserva.destinoNombre,
                        nuevoEstado
                    )

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
