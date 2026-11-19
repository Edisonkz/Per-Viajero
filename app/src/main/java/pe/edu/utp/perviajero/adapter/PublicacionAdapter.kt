package pe.edu.utp.perviajero.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.button.MaterialButton
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.model.PublicacionViajero
import java.util.Locale

class PublicacionAdapter(
    private val esModoAdmin: () -> Boolean = { false },
    private val onLikeClick: (PublicacionViajero) -> Unit,
    private val onCommentClick: (PublicacionViajero) -> Unit,
    private val onVerDestinoClick: (PublicacionViajero) -> Unit,
    private val onModerarClick: ((PublicacionViajero) -> Unit)? = null
) : ListAdapter<PublicacionViajero, PublicacionAdapter.PublicacionViewHolder>(PublicacionDiffCallback) {

    fun actualizarLista(lista: List<PublicacionViajero>) {
        submitList(lista.map { it.copy() })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicacionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_publicacion, parent, false)
        return PublicacionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PublicacionViewHolder, position: Int) {
        holder.bind(getItem(position), esModoAdmin(), onLikeClick, onCommentClick, onVerDestinoClick, onModerarClick)
    }

    class PublicacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAvatar: ImageView = itemView.findViewById(R.id.ivPubAvatar)
        private val tvAutor: TextView = itemView.findViewById(R.id.tvPubAutor)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvPubFecha)
        private val tvRegionBadge: TextView = itemView.findViewById(R.id.tvPubRegion)
        private val tvDestino: TextView = itemView.findViewById(R.id.tvPubDestino)
        private val tvRating: TextView = itemView.findViewById(R.id.tvPubRating)
        private val ivFoto: ImageView = itemView.findViewById(R.id.ivPubFoto)
        private val tvFotosCount: TextView = itemView.findViewById(R.id.tvPubFotosCount)
        private val tvComentario: TextView = itemView.findViewById(R.id.tvPubComentario)
        private val btnLike: MaterialButton = itemView.findViewById(R.id.btnPubLike)
        private val btnComentar: MaterialButton = itemView.findViewById(R.id.btnPubComentar)
        private val btnModerar: MaterialButton = itemView.findViewById(R.id.btnPubModerar)
        private val btnVerDestino: MaterialButton = itemView.findViewById(R.id.btnPubVerDestino)

        fun bind(
            pub: PublicacionViajero,
            isAdmin: Boolean,
            onLikeClick: (PublicacionViajero) -> Unit,
            onCommentClick: (PublicacionViajero) -> Unit,
            onVerDestinoClick: (PublicacionViajero) -> Unit,
            onModerarClick: ((PublicacionViajero) -> Unit)?
        ) {
            val ctx = itemView.context
            tvAutor.text = pub.autorNombre
            tvFecha.text = pub.fechaPublicacion
            tvRegionBadge.text = pub.region
            tvDestino.text = pub.destinoNombre
            tvRating.text = String.format(Locale.US, "%.1f ★", pub.calificacion)
            tvComentario.text = pub.comentario

            if (pub.autorFotoUri.isNotEmpty()) {
                ivAvatar.setPadding(0, 0, 0, 0)
                Glide.with(ctx)
                    .load(Uri.parse(pub.autorFotoUri))
                    .circleCrop()
                    .placeholder(R.drawable.ic_person)
                    .into(ivAvatar)
            } else {
                ivAvatar.setPadding(12, 12, 12, 12)
                ivAvatar.setImageResource(R.drawable.ic_person)
            }

            val fotos = pub.obtenerFotos()
            val principal = pub.fotoPrincipal()
            val imageModel: Any = when {
                principal.isBlank() -> R.drawable.bg_destino_placeholder
                principal.startsWith("http") -> principal
                else -> Uri.parse(principal)
            }
            Glide.with(ctx)
                .load(imageModel)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .placeholder(R.drawable.bg_destino_placeholder)
                .error(R.drawable.bg_destino_placeholder)
                .into(ivFoto)

            if (fotos.size > 1) {
                tvFotosCount.visibility = View.VISIBLE
                tvFotosCount.text = "📷 ${fotos.size} fotos"
            } else {
                tvFotosCount.visibility = View.GONE
            }

            btnLike.text = "${pub.likes} Útil"
            if (pub.dioLike) {
                btnLike.setIconTintResource(R.color.colorPrimary)
                btnLike.setTextColor(ContextCompat.getColor(ctx, R.color.colorPrimary))
            } else {
                btnLike.setIconTintResource(R.color.colorTextSecondary)
                btnLike.setTextColor(ContextCompat.getColor(ctx, R.color.colorTextSecondary))
            }

            btnComentar.text = "${pub.comentarios.size} Comentarios"

            itemView.setOnClickListener { onCommentClick(pub) }
            btnLike.setOnClickListener { onLikeClick(pub) }
            btnComentar.setOnClickListener { onCommentClick(pub) }
            ivFoto.setOnClickListener { onCommentClick(pub) }
            btnVerDestino.setOnClickListener { onVerDestinoClick(pub) }

            if (isAdmin && onModerarClick != null) {
                btnModerar.visibility = View.VISIBLE
                btnModerar.setOnClickListener { onModerarClick(pub) }
            } else {
                btnModerar.visibility = View.GONE
            }
        }
    }

    private object PublicacionDiffCallback : DiffUtil.ItemCallback<PublicacionViajero>() {
        override fun areItemsTheSame(oldItem: PublicacionViajero, newItem: PublicacionViajero): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PublicacionViajero, newItem: PublicacionViajero): Boolean =
            oldItem == newItem
    }
}
