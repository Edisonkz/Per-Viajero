package pe.edu.utp.perviajero.adapter

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
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

    override fun onViewAttachedToWindow(holder: OnboardingViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.startAmbientMotion()
    }

    override fun onViewDetachedFromWindow(holder: OnboardingViewHolder) {
        holder.stopAmbientMotion()
        super.onViewDetachedFromWindow(holder)
    }

    override fun getItemCount(): Int = items.size

    class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardMedallion: View = itemView.findViewById(R.id.cardSlideMedallion)
        private val haloInner: View = itemView.findViewById(R.id.viewSlideHaloInner)
        private val haloOuter: View = itemView.findViewById(R.id.viewSlideHaloOuter)
        private val ivIcono: ImageView = itemView.findViewById(R.id.ivOnboardingIcono)
        private val tvBadge: TextView = itemView.findViewById(R.id.tvOnboardingBadge)
        private val tvTitulo: TextView = itemView.findViewById(R.id.tvOnboardingTitulo)
        private val tvSubtitulo: TextView = itemView.findViewById(R.id.tvOnboardingSubtitulo)
        private val tvDescripcion: TextView = itemView.findViewById(R.id.tvOnboardingDescripcion)
        private val pillViews: List<TextView> = listOf(
            itemView.findViewById(R.id.tvSlidePill1),
            itemView.findViewById(R.id.tvSlidePill2),
            itemView.findViewById(R.id.tvSlidePill3)
        )

        private var ambientAnimator: AnimatorSet? = null

        fun bind(item: OnboardingItem) {
            ivIcono.setImageResource(item.iconoResId)
            if (item.tintIconWhite) {
                ivIcono.setColorFilter(Color.WHITE)
            } else {
                ivIcono.clearColorFilter()
            }

            val accent = try {
                Color.parseColor(item.accentColorHex)
            } catch (_: Exception) {
                Color.parseColor("#FFB300")
            }

            tvBadge.text = item.badge
            tvBadge.setTextColor(accent)
            tvTitulo.text = item.titulo
            tvSubtitulo.text = item.subtitulo
            tvSubtitulo.setTextColor(accent)
            tvDescripcion.text = item.descripcion

            pillViews.forEachIndexed { index, tv ->
                val text = item.highlights.getOrNull(index)
                if (!text.isNullOrBlank()) {
                    tv.visibility = View.VISIBLE
                    tv.text = text
                } else {
                    tv.visibility = View.GONE
                }
            }
        }

        fun startAmbientMotion() {
            stopAmbientMotion()
            val floatCard = ObjectAnimator.ofFloat(cardMedallion, View.TRANSLATION_Y, -8f, 8f).apply {
                duration = 2200L
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                interpolator = AccelerateDecelerateInterpolator()
            }
            val pulseInnerX = ObjectAnimator.ofFloat(haloInner, View.SCALE_X, 0.96f, 1.07f).apply {
                duration = 2000L
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                interpolator = AccelerateDecelerateInterpolator()
            }
            val pulseInnerY = ObjectAnimator.ofFloat(haloInner, View.SCALE_Y, 0.96f, 1.07f).apply {
                duration = 2000L
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                interpolator = AccelerateDecelerateInterpolator()
            }
            val pulseOuterX = ObjectAnimator.ofFloat(haloOuter, View.SCALE_X, 0.94f, 1.11f).apply {
                duration = 2600L
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                interpolator = AccelerateDecelerateInterpolator()
            }
            val pulseOuterY = ObjectAnimator.ofFloat(haloOuter, View.SCALE_Y, 0.94f, 1.11f).apply {
                duration = 2600L
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                interpolator = AccelerateDecelerateInterpolator()
            }
            ambientAnimator = AnimatorSet().apply {
                playTogether(floatCard, pulseInnerX, pulseInnerY, pulseOuterX, pulseOuterY)
                start()
            }
        }

        fun stopAmbientMotion() {
            ambientAnimator?.cancel()
            ambientAnimator = null
            cardMedallion.translationY = 0f
            haloInner.scaleX = 1f
            haloInner.scaleY = 1f
            haloOuter.scaleX = 1f
            haloOuter.scaleY = 1f
        }
    }
}
