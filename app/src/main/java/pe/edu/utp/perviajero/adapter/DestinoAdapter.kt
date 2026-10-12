package pe.edu.utp.perviajero.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.Destino

// REQ-06: Adaptador para RecyclerView con patrón ViewHolder
class DestinoAdapter(
    private var listaOriginal: List<Destino>,
    private val onItemClick: (Destino) -> Unit,
    private val onFavoritoClick: ((Destino, Boolean) -> Unit)? = null
) : RecyclerView.Adapter<DestinoAdapter.DestinoViewHolder>() {

    // Lista mutable visible en pantalla (para filtrado reactivo)
    private var listaFiltrada: MutableList<Destino> = listaOriginal.toMutableList()

    inner class DestinoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivDestino: ImageView = itemView.findViewById(R.id.ivDestino)
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvDepartamento: TextView = itemView.findViewById(R.id.tvDepartamento)
        val tvCategoria: TextView = itemView.findViewById(R.id.tvCategoria)
        val tvCalificacion: TextView = itemView.findViewById(R.id.tvCalificacion)
        val tvDescripcionCorta: TextView = itemView.findViewById(R.id.tvDescripcionCorta)
        val btnFavorito: ImageButton = itemView.findViewById(R.id.btnFavorito)
        val cardDestino: View = itemView.findViewById(R.id.cardDestino)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_destino, parent, false)
        return DestinoViewHolder(view)
    }

    override fun onBindViewHolder(holder: DestinoViewHolder, position: Int) {
        val destino = listaFiltrada[position]
        val context = holder.itemView.context
        val prefs = PreferencesHelper.getInstance(context)

        holder.tvNombre.text = destino.nombre
        holder.tvDepartamento.text = "${destino.departamento}, Perú"
        holder.tvCategoria.text = destino.categoria
        holder.tvCalificacion.text = destino.calificacion.toString()
        holder.tvDescripcionCorta.text = destino.descripcion

        // Carga asíncrona de imágenes mediante Glide
        Glide.with(context)
            .load(destino.imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_dialog_alert)
            .into(holder.ivDestino)

        // Estado del botón favorito desde SharedPreferences
        val esFavorito = prefs.isFavorito(destino.id)
        actualizarIconoFavorito(holder.btnFavorito, esFavorito)

        // Listener de clic en favorito
        holder.btnFavorito.setOnClickListener {
            val nuevoEstado = prefs.toggleFavorito(destino.id)
            actualizarIconoFavorito(holder.btnFavorito, nuevoEstado)
            onFavoritoClick?.invoke(destino, nuevoEstado)
        }

        // Navegación hacia detalle
        holder.cardDestino.setOnClickListener {
            onItemClick(destino)
        }
    }

    private fun actualizarIconoFavorito(btn: ImageButton, esFavorito: Boolean) {
        if (esFavorito) {
            btn.setImageResource(R.drawable.ic_favoritos)
            btn.setColorFilter(ContextCompat.getColor(btn.context, R.color.colorAccent))
        } else {
            btn.setImageResource(R.drawable.ic_favoritos_border)
            btn.setColorFilter(ContextCompat.getColor(btn.context, android.R.color.white))
        }
    }

    override fun getItemCount(): Int = listaFiltrada.size

    // Actualizar datos base
    fun updateData(nuevaLista: List<Destino>) {
        listaOriginal = nuevaLista
        listaFiltrada = nuevaLista.toMutableList()
        notifyDataSetChanged()
    }

    // REQ-08: Filtrado dinámico en tiempo real sin recrear la Activity
    fun filter(query: String): Int {
        val texto = query.trim().lowercase()
        listaFiltrada = if (texto.isEmpty()) {
            listaOriginal.toMutableList()
        } else {
            listaOriginal.filter { destino ->
                destino.nombre.lowercase().contains(texto) ||
                destino.departamento.lowercase().contains(texto) ||
                destino.categoria.lowercase().contains(texto)
            }.toMutableList()
        }
        notifyDataSetChanged()
        return listaFiltrada.size
    }
}

