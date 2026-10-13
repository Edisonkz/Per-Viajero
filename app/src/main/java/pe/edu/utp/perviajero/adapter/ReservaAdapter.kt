package pe.edu.utp.perviajero.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.model.Reserva
import java.util.Locale

class ReservaAdapter(
    private var listaReservas: List<Reserva>,
    private val esModoAdmin: Boolean = false,
    private val onCambiarEstadoClicked: ((Reserva) -> Unit)? = null
) : RecyclerView.Adapter<ReservaAdapter.ReservaViewHolder>() {

    fun actualizarLista(nuevaLista: List<Reserva>) {
        listaReservas = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reserva, parent, false)
        return ReservaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReservaViewHolder, position: Int) {
        val reserva = listaReservas[position]
        holder.bind(reserva, esModoAdmin, onCambiarEstadoClicked)
    }

    override fun getItemCount(): Int = listaReservas.size

    class ReservaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivFoto: ImageView = itemView.findViewById(R.id.ivItemReservaFoto)
        private val tvDestino: TextView = itemView.findViewById(R.id.tvItemReservaDestino)
        private val tvCodigo: TextView = itemView.findViewById(R.id.tvItemReservaCodigo)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvItemReservaFecha)
        private val tvDetalles: TextView = itemView.findViewById(R.id.tvItemReservaDetalles)
        private val tvUsuario: TextView = itemView.findViewById(R.id.tvItemReservaUsuario)
        private val tvTotal: TextView = itemView.findViewById(R.id.tvItemReservaTotal)
        private val tvEstado: TextView = itemView.findViewById(R.id.tvItemReservaEstado)
        private val btnCambiarEstado: MaterialButton = itemView.findViewById(R.id.btnItemCambiarEstado)

        fun bind(
            reserva: Reserva,
            esModoAdmin: Boolean,
            onCambiarEstado: ((Reserva) -> Unit)?
        ) {
            tvDestino.text = reserva.destinoNombre
            tvCodigo.text = reserva.codigoReserva
            tvFecha.text = "Salida: ${reserva.fechaSalida}"
            tvDetalles.text = "${reserva.cantidadPersonas} pers • ${reserva.tipoMovilidad} • ${reserva.categoriaHotel} • ${reserva.guiaTuristico}"
            tvTotal.text = String.format(Locale.US, "S/ %.2f", reserva.costoTotal)
            tvEstado.text = reserva.estado

            // Color del badge según el estado de la reserva
            val badgeBg = GradientDrawable().apply {
                cornerRadius = 16f
                val colorHex = when (reserva.estado) {
                    "En Ruta" -> "#F57C00"
                    "Completada", "Finalizada" -> "#2E7D32"
                    else -> "#1976D2"
                }
                setColor(Color.parseColor(colorHex))
            }
            tvEstado.background = badgeBg

            Glide.with(itemView.context)
                .load(reserva.destinoFoto)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_dialog_alert)
                .into(ivFoto)

            if (esModoAdmin) {
                tvUsuario.visibility = View.VISIBLE
                tvUsuario.text = "Cliente: ${reserva.usuarioNombre} (${reserva.usuarioEmail})"
                btnCambiarEstado.visibility = View.VISIBLE
                btnCambiarEstado.setOnClickListener {
                    onCambiarEstado?.invoke(reserva)
                }
            } else {
                tvUsuario.visibility = View.GONE
                btnCambiarEstado.visibility = View.GONE
            }
        }
    }
}

