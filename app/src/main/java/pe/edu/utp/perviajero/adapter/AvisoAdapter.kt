package pe.edu.utp.perviajero.adapter

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.model.AvisoTuristico
import pe.edu.utp.perviajero.util.NotificationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AvisoAdapter(
    private val onAvisoClick: (AvisoTuristico) -> Unit
) : ListAdapter<AvisoTuristico, AvisoAdapter.AvisoViewHolder>(AvisoDiff) {

    private val timeFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())

    inner class AvisoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardAviso: View = itemView.findViewById(R.id.cardAviso)
        val dotNoLeido: View = itemView.findViewById(R.id.dotNoLeido)
        val tvTipoBadge: TextView = itemView.findViewById(R.id.tvAvisoTipoBadge)
        val tvRegion: TextView = itemView.findViewById(R.id.tvAvisoRegion)
        val tvHora: TextView = itemView.findViewById(R.id.tvAvisoHora)
        val tvTitulo: TextView = itemView.findViewById(R.id.tvAvisoTitulo)
        val tvCuerpo: TextView = itemView.findViewById(R.id.tvAvisoCuerpo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvisoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_aviso, parent, false)
        return AvisoViewHolder(view)
    }

    override fun onBindViewHolder(holder: AvisoViewHolder, position: Int) {
        val aviso = getItem(position)
        val ctx = holder.itemView.context

        holder.dotNoLeido.visibility = if (aviso.leido) View.GONE else View.VISIBLE
        holder.tvTipoBadge.text = aviso.tipo.uppercase(Locale.US)
        holder.tvTipoBadge.background = GradientDrawable().apply {
            cornerRadius = 100f
            setColor(NotificationHelper.colorDe(ctx, aviso.tipo))
        }
        holder.tvRegion.text = aviso.region.ifBlank { "PerúViajero" }
        holder.tvHora.text = timeFormat.format(Date(aviso.hora))
        holder.tvTitulo.text = aviso.titulo
        holder.tvTitulo.setTypeface(null, if (aviso.leido) Typeface.NORMAL else Typeface.BOLD)
        holder.tvCuerpo.text = aviso.cuerpo
        holder.cardAviso.alpha = if (aviso.leido) 0.85f else 1f
        holder.cardAviso.setOnClickListener { onAvisoClick(aviso) }
    }

    private object AvisoDiff : DiffUtil.ItemCallback<AvisoTuristico>() {
        override fun areItemsTheSame(oldItem: AvisoTuristico, newItem: AvisoTuristico): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: AvisoTuristico, newItem: AvisoTuristico): Boolean = oldItem == newItem
    }
}
