package pe.edu.utp.perviajero.ui.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.data.model.EmpresaInfo
import pe.edu.utp.perviajero.data.model.Reserva
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.ui.BrujulaActivity
import pe.edu.utp.perviajero.ui.MapaRutaActivity
import java.util.Locale

/**
 * Componente compartido para renderizar y compartir el voucher oficial de embarque.
 * Elimina duplicación entre MisReservasFragment y AdminDashboardFragment.
 */
object VoucherDialogHelper {

    fun showVoucherDialog(context: Context, reserva: Reserva) {
        val empresa: EmpresaInfo = ServiceLocator.getSessionRepository(context).getEmpresaInfo()
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_detalle_voucher, null)

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
        tvTicketFechaHora.text = "Salida: ${reserva.fechaSalida} • ${reserva.horarioSalida}"
        tvTicketTerminal.text = "Embarque: ${reserva.terminalEmbarque}"
        tvTicketTransporte.text = "Transporte: ${reserva.tipoMovilidad} • ${reserva.categoriaHotel}"
        tvTicketGuia.text = "Guía Oficial: ${reserva.guiaTuristico}"
        tvTicketTotal.text = String.format(Locale.US, "S/ %.2f", reserva.costoTotal)
        tvTicketMetodoPago.text = "${reserva.metodoPago} • ${reserva.numeroOperacion}"

        Glide.with(context)
            .load(reserva.destinoFoto)
            .centerCrop()
            .placeholder(R.drawable.bg_destino_placeholder)
            .error(R.drawable.bg_destino_placeholder)
            .into(ivTicketFoto)

        tvTicketPasajerosLista.text = if (reserva.listaPasajeros.isNotEmpty()) {
            reserva.listaPasajeros.joinToString("\n") { p ->
                "• ${p.nombresApellidos} (${p.tipoDocumento}: ${p.numeroDocumento}, ${p.edad} años)"
            }
        } else {
            "• Titular: ${reserva.usuarioNombre} (${reserva.usuarioEmail})"
        }

        val dialog = MaterialAlertDialogBuilder(context)
            .setView(dialogView)
            .setPositiveButton("Cerrar", null)
            .create()

        btnTicketMaps.setOnClickListener {
            val uri = Uri.parse("geo:0,0?q=${Uri.encode(reserva.terminalEmbarque)}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            try {
                context.startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(context, "No se encontró una app de mapas instalada", Toast.LENGTH_SHORT).show()
            }
        }

        btnTicketCompartir.setOnClickListener {
            shareVoucher(context, reserva, empresa.razonSocial, empresa.ruc)
            dialog.dismiss()
        }

        dialog.show()
    }

    fun shareVoucher(context: Context, reserva: Reserva, razonSocial: String, ruc: String) {
        val listaStr = if (reserva.listaPasajeros.isNotEmpty()) {
            reserva.listaPasajeros.joinToString("\n") {
                "   - ${it.nombresApellidos} (${it.tipoDocumento}: ${it.numeroDocumento})"
            }
        } else {
            "   - ${reserva.usuarioNombre} (${reserva.usuarioEmail})"
        }

        val mensaje = "BOLETO DE EMBARQUE OFICIAL - $razonSocial\n" +
            "RUC: $ruc • Reg. MINCETUR\n\n" +
            "• Código Voucher: ${reserva.codigoReserva}\n" +
            "• Destino: ${reserva.destinoNombre}\n" +
            "• Salida: ${reserva.fechaSalida} a las ${reserva.horarioSalida}\n" +
            "• Terminal: ${reserva.terminalEmbarque}\n" +
            "• Transporte: ${reserva.tipoMovilidad}\n" +
            "• Hospedaje: ${reserva.categoriaHotel}\n" +
            "• Guía Turístico: ${reserva.guiaTuristico}\n\n" +
            "Manifiesto de Pasajeros:\n$listaStr\n\n" +
            "Total Pagado: S/ ${String.format(Locale.US, "%.2f", reserva.costoTotal)} (Inc. IGV)\n\n" +
            "Emitido por PerúViajero."

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, mensaje)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Compartir Boleto de Embarque"))
    }

    fun openMapaParaReserva(context: Context, reserva: Reserva) {
        val destino = resolverDestinoDeReserva(context, reserva)
        context.startActivity(Intent(context, MapaRutaActivity::class.java).apply {
            putExtra(MapaRutaActivity.EXTRA_DESTINO, destino)
        })
    }

    fun openBrujulaParaReserva(context: Context, reserva: Reserva) {
        val destino = resolverDestinoDeReserva(context, reserva)
        context.startActivity(Intent(context, BrujulaActivity::class.java).apply {
            putExtra(BrujulaActivity.EXTRA_DESTINO, destino)
        })
    }

    private fun resolverDestinoDeReserva(context: Context, reserva: Reserva): Destino {
        val cached = ServiceLocator.getDestinoRepository(context).obtenerDestinosEnCache()
        return cached.find { it.id == reserva.destinoId } ?: Destino(
            id = reserva.destinoId,
            nombre = reserva.destinoNombre,
            departamento = "Perú",
            categoria = "Tour",
            descripcion = "",
            calificacion = 4.8,
            imageUrl = reserva.destinoFoto,
            latitud = -13.1631,
            longitud = -72.5450
        )
    }
}
