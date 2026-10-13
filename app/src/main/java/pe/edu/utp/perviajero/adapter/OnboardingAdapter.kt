package pe.edu.utp.perviajero.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.model.OnboardingItem

class OnboardingAdapter(
    private val items: List<OnboardingItem>
) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding_slide, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcono: ImageView = itemView.findViewById(R.id.ivOnboardingIcono)
        private val tvBadge: TextView = itemView.findViewById(R.id.tvOnboardingBadge)
        private val tvTitulo: TextView = itemView.findViewById(R.id.tvOnboardingTitulo)
        private val tvSubtitulo: TextView = itemView.findViewById(R.id.tvOnboardingSubtitulo)
        private val tvDescripcion: TextView = itemView.findViewById(R.id.tvOnboardingDescripcion)

        fun bind(item: OnboardingItem) {
            ivIcono.setImageResource(item.iconoResId)
            tvBadge.text = item.badge
            tvTitulo.text = item.titulo
            tvSubtitulo.text = item.subtitulo
            tvDescripcion.text = item.descripcion
        }
    }
}
