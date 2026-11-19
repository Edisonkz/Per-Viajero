package pe.edu.utp.perviajero.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.button.MaterialButton
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.util.GeoMath
import pe.edu.utp.perviajero.util.RegionPeruHelper

// Adaptador optimizado con ListAdapter + DiffUtil para el catálogo de destinos turísticos
class DestinoAdapter(
    listaOriginal: List<Destino> = emptyList(),
    private val showQuickActions: Boolean = false,
    private val onItemClick: (Destino) -> Unit,
    private val onFavoritoClick: ((Destino, Boolean) -> Unit)? = null,
    private val onRutaGpsClick: ((Destino) -> Unit)? = null,
    private val onReservarClick: ((Destino) -> Unit)? = null
) : ListAdapter<Destino, DestinoAdapter.DestinoViewHolder>(DestinoDiffCallback) {

    private var listaBase: List<Destino> = listaOriginal
    private var currentQuery: String = ""
    private var currentCategory: String = "Todos"
    private var currentDepartment: String = "Todos"
    private var currentProvince: String = "Todas"
    private var userOrigin: Pair<Double, Double>? = null

    init {
        if (listaOriginal.isNotEmpty()) {
            submitList(listaOriginal)
        }
    }

    inner class DestinoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivDestino: ImageView = itemView.findViewById(R.id.ivDestino)
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvDepartamento: TextView = itemView.findViewById(R.id.tvDepartamento)
        val tvCategoria: TextView = itemView.findViewById(R.id.tvCategoria)
        val tvCalificacion: TextView = itemView.findViewById(R.id.tvCalificacion)
        val btnFavorito: ImageButton = itemView.findViewById(R.id.btnFavorito)
        val cardDestino: View = itemView.findViewById(R.id.cardDestino)
        val layoutFavoritoAcciones: View = itemView.findViewById(R.id.layoutFavoritoAcciones)
        val btnDestinoRutaGps: MaterialButton = itemView.findViewById(R.id.btnDestinoRutaGps)
        val btnDestinoReservar: MaterialButton = itemView.findViewById(R.id.btnDestinoReservar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_destino, parent, false)
        return DestinoViewHolder(view)
    }

    override fun onBindViewHolder(holder: DestinoViewHolder, position: Int) {
        val destino = getItem(position)
        val context = holder.itemView.context
        val favRepo = ServiceLocator.getFavoritoRepository(context)

        holder.tvNombre.text = destino.nombre
        holder.tvCategoria.text = destino.categoria
        holder.tvCalificacion.text = destino.calificacion.toString()

        val provincia = RegionPeruHelper.resolveProvincia(destino)
        val ubicacion = listOfNotNull(
            destino.distrito?.takeIf { it.isNotBlank() },
            provincia.takeIf { it.isNotBlank() },
            destino.departamento.takeIf { it.isNotBlank() }
        ).distinct().joinToString(", ")
        val origin = userOrigin
        holder.tvDepartamento.text = if (origin != null && destino.latitud != null && destino.longitud != null) {
            val km = GeoMath.haversineDistanceKm(origin.first, origin.second, destino.latitud, destino.longitud)
            "$ubicacion • ${GeoMath.formatDistance(km)}"
        } else {
            ubicacion
        }

        val imageModel: Any = when {
            destino.imageUrl.isBlank() -> R.drawable.bg_destino_placeholder
            destino.imageUrl.startsWith("http") -> destino.imageUrl
            else -> Uri.parse(destino.imageUrl)
        }
        Glide.with(context)
            .load(imageModel)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .transition(DrawableTransitionOptions.withCrossFade())
            .placeholder(R.drawable.bg_destino_placeholder)
            .error(R.drawable.bg_destino_placeholder)
            .into(holder.ivDestino)

        val esFavorito = favRepo.isFavorito(destino.id)
        actualizarIconoFavorito(holder.btnFavorito, esFavorito)

        holder.btnFavorito.setOnClickListener {
            val nuevoEstado = favRepo.toggleFavorito(destino.id)
            actualizarIconoFavorito(holder.btnFavorito, nuevoEstado)
            onFavoritoClick?.invoke(destino, nuevoEstado)
        }

        holder.cardDestino.setOnClickListener {
            onItemClick(destino)
        }

        if (showQuickActions) {
            holder.layoutFavoritoAcciones.visibility = View.VISIBLE
            holder.btnDestinoRutaGps.setOnClickListener { onRutaGpsClick?.invoke(destino) }
            holder.btnDestinoReservar.setOnClickListener { onReservarClick?.invoke(destino) }
        } else {
            holder.layoutFavoritoAcciones.visibility = View.GONE
        }
    }

    private fun actualizarIconoFavorito(btn: ImageButton, esFavorito: Boolean) {
        if (esFavorito) {
            btn.setImageResource(R.drawable.ic_favoritos)
            btn.setColorFilter(ContextCompat.getColor(btn.context, R.color.colorAccent))
        } else {
            btn.setImageResource(R.drawable.ic_favoritos_border)
            btn.setColorFilter(ContextCompat.getColor(btn.context, R.color.colorTextSecondary))
        }
    }

    fun setUserOrigin(origin: Pair<Double, Double>?) {
        userOrigin = origin
        notifyDataSetChanged()
    }

    fun getListaBase(): List<Destino> = listaBase

    fun updateData(nuevaLista: List<Destino>) {
        listaBase = nuevaLista
        val filtered = filterList(listaBase, currentQuery, currentCategory, currentDepartment, currentProvince)
        submitList(filtered)
    }

    fun filter(
        query: String,
        category: String = currentCategory,
        department: String = currentDepartment,
        province: String = currentProvince
    ): Int {
        currentQuery = query
        currentCategory = category
        currentDepartment = department
        currentProvince = province
        val filtered = filterList(listaBase, query, category, department, province)
        submitList(filtered)
        return filtered.size
    }

    companion object {
        fun filterList(
            source: List<Destino>,
            query: String,
            category: String = "Todos",
            department: String = "Todos",
            province: String = "Todas"
        ): List<Destino> {
            val texto = query.trim().lowercase()
            return source.filter { destino ->
                val resolvedProv = RegionPeruHelper.resolveProvincia(destino)
                val matchesCategory = category.equals("Todos", ignoreCase = true) ||
                    destino.categoria.equals(category, ignoreCase = true)
                val matchesDepartment = department.equals("Todos", ignoreCase = true) ||
                    destino.departamento.equals(department, ignoreCase = true)
                val matchesProvince = province.equals("Todas", ignoreCase = true) ||
                    resolvedProv.equals(province, ignoreCase = true)
                val matchesQuery = texto.isEmpty() ||
                    destino.nombre.lowercase().contains(texto) ||
                    destino.departamento.lowercase().contains(texto) ||
                    resolvedProv.lowercase().contains(texto) ||
                    (destino.distrito?.lowercase()?.contains(texto) == true) ||
                    destino.categoria.lowercase().contains(texto)
                matchesCategory && matchesDepartment && matchesProvince && matchesQuery
            }
        }
    }

    private object DestinoDiffCallback : DiffUtil.ItemCallback<Destino>() {
        override fun areItemsTheSame(oldItem: Destino, newItem: Destino): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Destino, newItem: Destino): Boolean = oldItem == newItem
    }
}
