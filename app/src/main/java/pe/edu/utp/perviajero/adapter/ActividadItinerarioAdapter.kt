package pe.edu.utp.perviajero.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.model.ActividadItinerario

class ActividadItinerarioAdapter(
    actividadesIniciales: List<ActividadItinerario> = emptyList(),
    private val onToggleCompletada: (ActividadItinerario) -> Unit,
    private val onEliminar: (ActividadItinerario) -> Unit
) : ListAdapter<ActividadItinerario, ActividadItinerarioAdapter.ActividadViewHolder>(ActividadDiffCallback) {

    init {
        if (actividadesIniciales.isNotEmpty()) {
            submitList(actividadesIniciales.map { it.copy() })
        }
    }

    fun actualizarLista(nuevaLista: List<ActividadItinerario>) {
        submitList(nuevaLista.map { it.copy() })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActividadViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_actividad_itinerario, parent, false)
        return ActividadViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActividadViewHolder, position: Int) {
        holder.bind(getItem(position), onToggleCompletada, onEliminar)
    }

    class ActividadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDia: TextView = itemView.findViewById(R.id.tvItemActividadDia)
        private val tvHorario: TextView = itemView.findViewById(R.id.tvItemActividadHorario)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvItemActividadFecha)
        private val cbCompletada: CheckBox = itemView.findViewById(R.id.cbItemActividadCompletada)
        private val tvLugar: TextView = itemView.findViewById(R.id.tvItemActividadLugar)
        private val tvNotas: TextView = itemView.findViewById(R.id.tvItemActividadNotas)
        private val btnEliminar: ImageView = itemView.findViewById(R.id.btnItemActividadEliminar)

        fun bind(
            item: ActividadItinerario,
            onToggle: (ActividadItinerario) -> Unit,
            onEliminar: (ActividadItinerario) -> Unit
        ) {
            tvDia.text = item.dia
            tvHorario.text = item.horario
            if (item.fecha.isNotEmpty()) {
                tvFecha.visibility = View.VISIBLE
                tvFecha.text = item.fecha
            } else {
                tvFecha.visibility = View.GONE
            }
            tvLugar.text = item.lugar
            tvNotas.text = item.descripcion

            cbCompletada.setOnCheckedChangeListener(null)
            cbCompletada.isChecked = item.completada

            if (item.completada) {
                tvLugar.paintFlags = tvLugar.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvLugar.alpha = 0.6f
            } else {
                tvLugar.paintFlags = tvLugar.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvLugar.alpha = 1.0f
            }

            cbCompletada.setOnCheckedChangeListener { _, _ ->
                onToggle(item)
            }

            btnEliminar.setOnClickListener {
                onEliminar(item)
            }
        }
    }

    private object ActividadDiffCallback : DiffUtil.ItemCallback<ActividadItinerario>() {
        override fun areItemsTheSame(oldItem: ActividadItinerario, newItem: ActividadItinerario): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ActividadItinerario, newItem: ActividadItinerario): Boolean =
            oldItem == newItem
    }
}
