package pe.edu.utp.perviajero.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.model.ActividadItinerario

class ActividadItinerarioAdapter(
    private var actividades: List<ActividadItinerario>,
    private val onToggleCompletada: (ActividadItinerario) -> Unit,
    private val onEliminar: (ActividadItinerario) -> Unit
) : RecyclerView.Adapter<ActividadItinerarioAdapter.ActividadViewHolder>() {

    fun actualizarLista(nuevaLista: List<ActividadItinerario>) {
        actividades = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActividadViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_actividad_itinerario, parent, false)
        return ActividadViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActividadViewHolder, position: Int) {
        val item = actividades[position]
        holder.bind(item, onToggleCompletada, onEliminar)
    }

    override fun getItemCount(): Int = actividades.size

    class ActividadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDia: TextView = itemView.findViewById(R.id.tvItemActividadDia)
        private val tvHorario: TextView = itemView.findViewById(R.id.tvItemActividadHorario)
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
            tvLugar.text = item.lugar
            tvNotas.text = item.descripcion

            cbCompletada.setOnCheckedChangeListener(null)
            cbCompletada.isChecked = item.completada

            // Efecto tachado si está completada
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
}

