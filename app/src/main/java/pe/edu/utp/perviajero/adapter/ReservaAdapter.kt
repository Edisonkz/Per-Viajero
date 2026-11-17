package pe.edu.utp.perviajero.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.model.Reserva
import pe.edu.utp.perviajero.ui.view.BoardingPassBgView
import java.util.Locale

class ReservaAdapter(
    listaInicial: List<Reserva> = emptyList(),
    private val esModoAdmin: Boolean = false,
    private val onCambiarEstadoClicked: ((Reserva) -> Unit)? = null,
    private val onItemClicked: ((Reserva) -> Unit)? = null,
    private val onRutaGpsClicked: ((Reserva) -> Unit)? = null,
    private val onBrujulaClicked: ((Reserva) -> Unit)? = null
) : ListAdapter<Reserva, ReservaAdapter.ReservaViewHolder>(ReservaDiffCallback) {

    init {
        if (listaInicial.isNotEmpty()) {
            submitList(listaInicial.map { it.copy() })
        }
    }

    fun actualizarLista(nuevaLista: List<Reserva>) {
        submitList(nuevaLista.map { it.copy() })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reserva, parent, false)
        return ReservaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReservaViewHolder, position: Int) {
        holder.bind(
            getItem(position),
            esModoAdmin,
            onCambiarEstadoClicked,
            onItemClicked,
            onRutaGpsClicked,
            onBrujulaClicked
        )
    }

    class ReservaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val boardingPassBg: BoardingPassBgView = itemView.findViewById(R.id.boardingPassBg)
        private val tvDestino: TextView = itemView.findViewById(R.id.tvItemReservaDestino)
        private val tvCodigo: TextView = itemView.findViewById(R.id.tvItemReservaCodigo)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvItemReservaFecha)
        private val tvUsuario: TextView = itemView.findViewById(R.id.tvItemReservaUsuario)
        private val tvTotal: TextView = itemView.findViewById(R.id.tvItemReservaTotal)
        private val tvEstado: TextView = itemView.findViewById(R.id.tvItemReservaEstado)
        private val btnVerBoleto: MaterialButton = itemView.findViewById(R.id.btnReservaVerBoleto)
        private val btnRutaGps: MaterialButton = itemView.findViewById(R.id.btnReservaRutaGps)
        private val btnBrujula: MaterialButton = itemView.findViewById(R.id.btnReservaBrujula)
        private val btnCambiarEstado: MaterialButton = itemView.findViewById(R.id.btnItemCambiarEstado)

        fun bind(
            reserva: Reserva,
            esModoAdmin: Boolean,
            onCambiarEstado: ((Reserva) -> Unit)?,
            onItemClicked: ((Reserva) -> Unit)?,
            onRutaGps: ((Reserva) -> Unit)?,
            onBrujula: ((Reserva) -> Unit)?
        ) {
            tvCodigo.text = reserva.codigoReserva
            tvDestino.text = reserva.destinoNombre
            tvFecha.text = "${reserva.fechaSalida} • ${reserva.horarioSalida} • ${reserva.cantidadPersonas} pasajeros"
            tvTotal.text = String.format(Locale.US, "S/ %.2f", reserva.costoTotal)
            tvEstado.text = reserva.estado.uppercase(Locale.US)

            val badgeBg = GradientDrawable().apply {
                cornerRadius = 100f
                val colorRes = when (reserva.estado) {
                    "En Ruta" -> R.color.colorAccent
                    "Completada", "Finalizada" -> R.color.colorPrimaryDark
                    else -> R.color.colorPrimary
                }
                setColor(ContextCompat.getColor(itemView.context, colorRes))
            }
            tvEstado.background = badgeBg

            if (esModoAdmin) {
                boardingPassBg.setTearLineBottomOffsetRatio(0.41f)
                tvUsuario.visibility = View.VISIBLE
                tvUsuario.text = "Cliente: ${reserva.usuarioNombre} (${reserva.usuarioEmail})"
                btnCambiarEstado.visibility = View.VISIBLE
                btnCambiarEstado.setOnClickListener { onCambiarEstado?.invoke(reserva) }
            } else {
                boardingPassBg.setTearLineBottomOffsetRatio(0.28f)
                tvUsuario.visibility = View.GONE
                btnCambiarEstado.visibility = View.GONE
            }

            itemView.setOnClickListener { onItemClicked?.invoke(reserva) }
            btnVerBoleto.setOnClickListener { onItemClicked?.invoke(reserva) }
            btnRutaGps.setOnClickListener { onRutaGps?.invoke(reserva) }
            btnBrujula.setOnClickListener { onBrujula?.invoke(reserva) }
        }
    }

    private object ReservaDiffCallback : DiffUtil.ItemCallback<Reserva>() {
        override fun areItemsTheSame(oldItem: Reserva, newItem: Reserva): Boolean =
            oldItem.codigoReserva == newItem.codigoReserva

        override fun areContentsTheSame(oldItem: Reserva, newItem: Reserva): Boolean =
            oldItem == newItem
    }
}
