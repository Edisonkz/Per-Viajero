package pe.edu.utp.perviajero.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.adapter.ReservaAdapter
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.Reserva
import java.util.Locale

// Módulo Turista: Visualización de vouchers de tour y detalle interactivo de boleto de embarque
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
        adapter = ReservaAdapter(
            emptyList(),
            esModoAdmin = false,
            onItemClicked = { reserva ->
                mostrarDetalleVoucher(reserva)
            }
        )
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

    private fun mostrarDetalleVoucher(reserva: Reserva) {
        val prefs = PreferencesHelper.getInstance(requireContext())
        val empresa = prefs.getEmpresaInfo()

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_detalle_voucher, null)

        val tvTicketEmpresa: TextView = dialogView.findViewById(R.id.tvTicketEmpresa)
        val tvTicketRuc: TextView = dialogView.findViewById(R.id.tvTicketRuc)
        val tvTicketCodigo: TextView = dialogView.findViewById(R.id.tvTicketCodigo)
        val tvTicketEstado: TextView = dialogView.findViewById(R.id.tvTicketEstado)
        val ivTicketFoto: ImageView = dialogView.findViewById(R.id.ivTicketFoto)
        val tvTicketDestino: TextView = dialogView.findViewById(R.id.tvTicketDestino)
        val tvTicketFechaHora: TextView = dialogView.findViewById(R.id.tvTicketFechaHora)
        val tvTicketTerminal: TextView = dialogView.findViewById(R.id.tvTicketTerminal)
        val tvTicketTransporte: TextView = dialogView.findViewById(R.id.tvTicketTransporte)
        val tvTicketGuia: TextView = dialogView.findViewById(R.id.tvTicketGuia)
        val tvTicketPasajerosLista: TextView = dialogView.findViewById(R.id.tvTicketPasajerosLista)
        val tvTicketTotal: TextView = dialogView.findViewById(R.id.tvTicketTotal)
        val tvTicketMetodoPago: TextView = dialogView.findViewById(R.id.tvTicketMetodoPago)
        val btnTicketMaps: MaterialButton = dialogView.findViewById(R.id.btnTicketMaps)
        val btnTicketCompartir: MaterialButton = dialogView.findViewById(R.id.btnTicketCompartir)

        tvTicketEmpresa.text = empresa.razonSocial
        tvTicketRuc.text = "RUC: ${empresa.ruc} • Reg: ${empresa.registroMincetur}"
        tvTicketCodigo.text = reserva.codigoReserva
        tvTicketEstado.text = reserva.estado.uppercase(Locale.US)
        tvTicketDestino.text = reserva.destinoNombre
        tvTicketFechaHora.text = "📅 Salida: ${reserva.fechaSalida} • ${reserva.horarioSalida}"
        tvTicketTerminal.text = "📍 Embarque: ${reserva.terminalEmbarque}"
        tvTicketTransporte.text = "🚐 Transporte: ${reserva.tipoMovilidad} • ${reserva.categoriaHotel}"
        tvTicketGuia.text = "👤 Guía Oficial: ${reserva.guiaTuristico}"
        tvTicketTotal.text = String.format(Locale.US, "S/ %.2f", reserva.costoTotal)
        tvTicketMetodoPago.text = "${reserva.metodoPago} • ${reserva.numeroOperacion}"

        Glide.with(requireContext())
            .load(reserva.destinoFoto)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(ivTicketFoto)

        // Renderizar lista de pasajeros del manifiesto
        if (reserva.listaPasajeros.isNotEmpty()) {
            val listaText = reserva.listaPasajeros.joinToString("\n") { p ->
                "• ${p.nombresApellidos} (${p.tipoDocumento}: ${p.numeroDocumento}, ${p.edad} años)"
            }
            tvTicketPasajerosLista.text = listaText
        } else {
            tvTicketPasajerosLista.text = "• Titular: ${reserva.usuarioNombre} (${reserva.usuarioEmail})"
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Cerrar", null)
            .create()

        // Botón: Ver ubicación del terminal en Google Maps (Intent Implícito)
        btnTicketMaps.setOnClickListener {
            val uri = Uri.parse("geo:0,0?q=${Uri.encode(reserva.terminalEmbarque)}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        // Botón: Compartir boleto por WhatsApp o mensajería (Intent Implícito)
        btnTicketCompartir.setOnClickListener {
            compartirBoleto(reserva, empresa.razonSocial, empresa.ruc)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun compartirBoleto(reserva: Reserva, razonSocial: String, ruc: String) {
        val listaStr = if (reserva.listaPasajeros.isNotEmpty()) {
            reserva.listaPasajeros.joinToString("\n") { "   - ${it.nombresApellidos} (${it.tipoDocumento}: ${it.numeroDocumento})" }
        } else {
            "   - ${reserva.usuarioNombre} (${reserva.usuarioEmail})"
        }

        val mensaje = "🎟️ BOLETO DE EMBARQUE OFICIAL - $razonSocial\n" +
                "RUC: $ruc • Reg. MINCETUR\n\n" +
                "• Código Voucher: ${reserva.codigoReserva}\n" +
                "• Destino: ${reserva.destinoNombre}\n" +
                "• Salida: ${reserva.fechaSalida} a las ${reserva.horarioSalida}\n" +
                "• Terminal: ${reserva.terminalEmbarque}\n" +
                "• Transporte: ${reserva.tipoMovilidad}\n" +
                "• Hospedaje: ${reserva.categoriaHotel}\n" +
                "• Guía Turístico: ${reserva.guiaTuristico}\n\n" +
                "👥 Manifiesto de Pasajeros:\n$listaStr\n\n" +
                "💰 Total Pagado: S/ ${String.format(Locale.US, "%.2f", reserva.costoTotal)} (Inc. IGV)\n\n" +
                "Emitido por PerúViajero."

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, mensaje)
        }
        val chooser = Intent.createChooser(sendIntent, "Compartir Boleto de Embarque")
        startActivity(chooser)
    }
}
