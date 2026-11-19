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
import pe.edu.utp.perviajero.data.model.RolUsuario
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.ui.BrujulaActivity
import pe.edu.utp.perviajero.ui.LoginActivity
import pe.edu.utp.perviajero.ui.MapaRutaActivity
import pe.edu.utp.perviajero.ui.ReservaTourActivity
import pe.edu.utp.perviajero.ui.view.BoardingPassBgView
import pe.edu.utp.perviajero.ui.view.QrVisualView
import java.util.Locale

/**
 * Componente compartido para renderizar el Boleto Oficial de Embarque (con QR y perforación),
 * compartir el voucher y centralizar los accesos rápidos desde reservas y favoritos.
 */
object VoucherDialogHelper {

    fun showVoucherDialog(
        context: Context,
        reserva: Reserva,
        onDismiss: (() -> Unit)? = null
    ) {
        val empresa = ServiceLocator.getSessionRepository(context).getEmpresaInfo()
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_detalle_voucher, null)

        val boardingBg: BoardingPassBgView = dialogView.findViewById(R.id.boardingPassVoucherBg)
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
        val tvTicketCantidadPasajeros: TextView = dialogView.findViewById(R.id.tvTicketCantidadPasajeros)
        val tvTicketPasajerosLista: TextView = dialogView.findViewById(R.id.tvTicketPasajerosLista)
        val qrVoucher: QrVisualView = dialogView.findViewById(R.id.qrTicketVoucher)
        val tvTicketTotal: TextView = dialogView.findViewById(R.id.tvTicketTotal)
        val tvTicketMetodoPago: TextView = dialogView.findViewById(R.id.tvTicketMetodoPago)
        val btnTicketMaps: MaterialButton = dialogView.findViewById(R.id.btnTicketMaps)
        val btnTicketCompartir: MaterialButton = dialogView.findViewById(R.id.btnTicketCompartir)

        boardingBg.setTearLineBottomOffsetRatio(0.24f)
        qrVoucher.setCode("${reserva.codigoReserva}|${reserva.destinoId}|${reserva.costoTotal}")

        tvTicketEmpresa.text = empresa.razonSocial
        tvTicketRuc.text = "RUC: ${empresa.ruc} • Reg. ${empresa.registroMincetur}"
        tvTicketCodigo.text = reserva.codigoReserva
        tvTicketEstado.text = reserva.estado.uppercase(Locale.US)
        tvTicketDestino.text = reserva.destinoNombre
        tvTicketFechaHora.text = "${reserva.fechaSalida} • ${reserva.horarioSalida}"
        tvTicketTerminal.text = reserva.terminalEmbarque
        tvTicketTransporte.text = reserva.tipoMovilidad
        tvTicketGuia.text = reserva.guiaTuristico
        val sufijoPax = if (reserva.cantidadPersonas == 1) "Pasajero" else "Pasajeros"
        tvTicketCantidadPasajeros.text = "${reserva.cantidadPersonas} $sufijoPax"
        tvTicketTotal.text = String.format(Locale.US, "S/ %.2f", reserva.costoTotal)
        tvTicketMetodoPago.text = "Inc. IGV • ${reserva.metodoPago}"

        Glide.with(context)
            .load(reserva.destinoFoto)
            .centerCrop()
            .placeholder(R.drawable.bg_destino_placeholder)
            .error(R.drawable.bg_destino_placeholder)
            .into(ivTicketFoto)

        tvTicketPasajerosLista.text = if (reserva.listaPasajeros.isNotEmpty()) {
            reserva.listaPasajeros.joinToString("\n") { p ->
                "• ${p.nombresApellidos} (${p.tipoDocumento}: ${p.numeroDocumento})"
            }
        } else {
            "• Titular: ${reserva.usuarioNombre} (${reserva.usuarioEmail})"
        }

        val dialog = MaterialAlertDialogBuilder(context)
            .setView(dialogView)
            .setPositiveButton("Cerrar") { _, _ -> onDismiss?.invoke() }
            .setOnCancelListener { onDismiss?.invoke() }
            .create()

        btnTicketMaps.setOnClickListener {
            val uri = Uri.parse("geo:0,0?q=${Uri.encode(reserva.terminalEmbarque)}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            try {
                context.startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(context, "No se encontró una aplicación de mapas instalada", Toast.LENGTH_SHORT).show()
            }
        }

        btnTicketCompartir.setOnClickListener {
            shareVoucherText(context, reserva, empresa)
        }

        dialog.show()
    }

    fun shareVoucherText(context: Context, reserva: Reserva, empresa: EmpresaInfo) {
        val listaStr = if (reserva.listaPasajeros.isNotEmpty()) {
            reserva.listaPasajeros.joinToString("\n") { "   - ${it.nombresApellidos} (${it.tipoDocumento}: ${it.numeroDocumento})" }
        } else {
            "   - ${reserva.usuarioNombre}"
        }

        val mensaje = "🎟️ BOLETO OFICIAL DE EMBARQUE - ${empresa.razonSocial}\n" +
            "RUC: ${empresa.ruc} • Reg. MINCETUR: ${empresa.registroMincetur}\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
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

    fun openCotizadorConValidacion(context: Context, destino: Destino) {
        AuthGuardHelper.requireAuth(
            context = context,
            titulo = "Inicia sesión para reservar",
            motivo = "Como visitante puedes explorar libremente los atractivos. Para cotizar y confirmar tu tour en PerúViajero necesitas iniciar sesión."
        ) {
            lanzarReservaTour(context, destino)
        }
    }

    private fun lanzarReservaTour(context: Context, destino: Destino) {
        context.startActivity(Intent(context, ReservaTourActivity::class.java).apply {
            putExtra(ReservaTourActivity.EXTRA_DESTINO, destino)
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
