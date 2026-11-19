package pe.edu.utp.perviajero.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.model.ComentarioPublicacion
import pe.edu.utp.perviajero.data.model.PublicacionViajero
import pe.edu.utp.perviajero.data.model.RolUsuario
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.ui.common.AuthGuardHelper
import pe.edu.utp.perviajero.util.WindowInsetsHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla completa de detalle de una publicación de la Comunidad:
 * - Carrusel deslizable multi-foto con contador y puntos indicadores.
 * - Datos del autor, destino clickeable, relato completo y botón Útil.
 * - Lista completa de comentarios de otros viajeros + caja inferior para publicar comentarios.
 */
class DetallePublicacionActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PUBLICACION_ID = "extra_publicacion_id"
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var scrollView: NestedScrollView
    private lateinit var vpFotos: ViewPager2
    private lateinit var tvFotoCounter: TextView
    private lateinit var layoutDots: LinearLayout
    private lateinit var ivAvatar: ImageView
    private lateinit var tvAutor: TextView
    private lateinit var tvMeta: TextView
    private lateinit var tvRating: TextView
    private lateinit var btnDestino: MaterialButton
    private lateinit var tvTexto: TextView
    private lateinit var btnLike: MaterialButton
    private lateinit var tvCountComentarios: TextView
    private lateinit var btnModerar: MaterialButton
    private lateinit var tvSinComentarios: TextView
    private lateinit var rvComentarios: RecyclerView
    private lateinit var etNuevoComentario: EditText
    private lateinit var btnEnviar: MaterialButton

    private lateinit var comentarioAdapter: ComentarioAdapter
    private var publicacionId: String = ""
    private var publicacionActual: PublicacionViajero? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_publicacion)

        publicacionId = intent.getStringExtra(EXTRA_PUBLICACION_ID).orEmpty()
        if (publicacionId.isBlank()) {
            finish()
            return
        }

        inicializarVistas()
        cargarPublicacion()
        configurarEventos()
    }

    override fun onResume() {
        super.onResume()
        cargarPublicacion()
    }

    private fun inicializarVistas() {
        toolbar = findViewById(R.id.toolbarDetallePub)
        WindowInsetsHelper.applyToolbarActivityInsets(this, toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        scrollView = findViewById(R.id.scrollDetallePub)
        vpFotos = findViewById(R.id.vpDetallePubFotos)
        tvFotoCounter = findViewById(R.id.tvDetallePubFotoCounter)
        layoutDots = findViewById(R.id.layoutDetallePubDots)
        ivAvatar = findViewById(R.id.ivDetallePubAvatar)
        tvAutor = findViewById(R.id.tvDetallePubAutor)
        tvMeta = findViewById(R.id.tvDetallePubMeta)
        tvRating = findViewById(R.id.tvDetallePubRating)
        btnDestino = findViewById(R.id.btnDetallePubDestino)
        tvTexto = findViewById(R.id.tvDetallePubTexto)
        btnLike = findViewById(R.id.btnDetallePubLike)
        tvCountComentarios = findViewById(R.id.tvDetallePubCountComentarios)
        btnModerar = findViewById(R.id.btnDetallePubModerar)
        tvSinComentarios = findViewById(R.id.tvDetallePubSinComentarios)
        rvComentarios = findViewById(R.id.rvDetallePubComentarios)
        etNuevoComentario = findViewById(R.id.etDetallePubComentario)
        btnEnviar = findViewById(R.id.btnDetallePubEnviar)

        comentarioAdapter = ComentarioAdapter(
            isAdminProvider = { esAdmin() },
            onDeleteClick = { comentario -> confirmarEliminarComentario(comentario) }
        )
        rvComentarios.layoutManager = LinearLayoutManager(this)
        rvComentarios.adapter = comentarioAdapter
    }

    private fun esAdmin(): Boolean {
        val repo = ServiceLocator.getSessionRepository(this)
        return repo.isLoggedIn() && repo.getRolActual() == RolUsuario.ADMIN
    }

    private fun cargarPublicacion() {
        val pub = ServiceLocator.getComunidadRepository(this).obtenerPublicacionPorId(publicacionId)
        if (pub == null) {
            Toast.makeText(this, "La publicación ya no está disponible", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        publicacionActual = pub
        renderPublicacion(pub)
    }

    private fun renderPublicacion(pub: PublicacionViajero) {
        toolbar.title = pub.destinoNombre
        toolbar.setBackgroundColor(
            ContextCompat.getColor(
                this,
                if (esAdmin()) R.color.colorAdminPrimary else R.color.colorPrimary
            )
        )

        tvAutor.text = pub.autorNombre
        tvMeta.text = "${pub.region} • ${pub.fechaPublicacion}"
        tvRating.text = String.format(Locale.US, "%.1f ★", pub.calificacion)
        btnDestino.text = pub.destinoNombre
        tvTexto.text = pub.comentario

        if (pub.autorFotoUri.isNotEmpty()) {
            ivAvatar.setPadding(0, 0, 0, 0)
            Glide.with(this)
                .load(Uri.parse(pub.autorFotoUri))
                .circleCrop()
                .placeholder(R.drawable.ic_person)
                .into(ivAvatar)
        } else {
            ivAvatar.setPadding(12, 12, 12, 12)
            ivAvatar.setImageResource(R.drawable.ic_person)
        }

        val fotos = pub.obtenerFotos().ifEmpty { listOf("") }
        if (vpFotos.adapter == null || (vpFotos.adapter as? FotoCarouselAdapter)?.fotos != fotos) {
            vpFotos.adapter = FotoCarouselAdapter(fotos)
            configurarIndicadorCarrusel(fotos.size)
        }

        btnLike.text = "${pub.likes} Útil"
        val likeColor = ContextCompat.getColor(
            this,
            if (pub.dioLike) R.color.colorPrimary else R.color.colorTextSecondary
        )
        btnLike.setTextColor(likeColor)
        btnLike.setIconTintResource(if (pub.dioLike) R.color.colorPrimary else R.color.colorTextSecondary)

        val totalCom = pub.comentarios.size
        tvCountComentarios.text = if (totalCom == 1) "1 comentario" else "$totalCom comentarios"

        if (pub.comentarios.isEmpty()) {
            tvSinComentarios.visibility = View.VISIBLE
            rvComentarios.visibility = View.GONE
        } else {
            tvSinComentarios.visibility = View.GONE
            rvComentarios.visibility = View.VISIBLE
            comentarioAdapter.actualizar(pub.comentarios)
        }

        btnModerar.visibility = if (esAdmin()) View.VISIBLE else View.GONE
    }

    private fun configurarIndicadorCarrusel(total: Int) {
        layoutDots.removeAllViews()
        if (total <= 1) {
            tvFotoCounter.visibility = View.GONE
            layoutDots.visibility = View.GONE
            return
        }

        tvFotoCounter.visibility = View.VISIBLE
        layoutDots.visibility = View.VISIBLE
        tvFotoCounter.text = "1 / $total"

        val density = resources.displayMetrics.density
        val dots = mutableListOf<View>()
        repeat(total) { index ->
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ((if (index == 0) 20 else 8) * density).toInt(),
                    (8 * density).toInt()
                ).apply {
                    setMargins((4 * density).toInt(), 0, (4 * density).toInt(), 0)
                }
                setBackgroundResource(
                    if (index == 0) R.drawable.bg_onboarding_dot_active
                    else R.drawable.bg_onboarding_dot_inactive
                )
            }
            dots.add(dot)
            layoutDots.addView(dot)
        }

        vpFotos.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tvFotoCounter.text = "${position + 1} / $total"
                dots.forEachIndexed { idx, v ->
                    v.setBackgroundResource(
                        if (idx == position) R.drawable.bg_onboarding_dot_active
                        else R.drawable.bg_onboarding_dot_inactive
                    )
                    v.layoutParams = v.layoutParams.apply {
                        width = ((if (idx == position) 20 else 8) * density).toInt()
                    }
                }
            }
        })
    }

    private fun configurarEventos() {
        btnLike.setOnClickListener {
            val pub = publicacionActual ?: return@setOnClickListener
            AuthGuardHelper.requireAuth(
                this,
                "Inicia sesión para reaccionar",
                "Como visitante puedes ver todas las fotos y comentarios. Inicia sesión para marcar experiencias como útiles."
            ) {
                ServiceLocator.getComunidadRepository(this).toggleLike(pub.id)
                cargarPublicacion()
            }
        }

        btnDestino.setOnClickListener {
            val pub = publicacionActual ?: return@setOnClickListener
            val destino = ServiceLocator.getDestinoRepository(this)
                .obtenerDestinosEnCache()
                .find { it.id == pub.destinoId }
            if (destino != null) {
                startActivity(Intent(this, DetalleDestinoActivity::class.java).apply {
                    putExtra(DetalleDestinoActivity.EXTRA_DESTINO, destino)
                })
            } else {
                Toast.makeText(this, "${pub.destinoNombre} (${pub.region})", Toast.LENGTH_SHORT).show()
            }
        }

        btnModerar.setOnClickListener {
            val pub = publicacionActual ?: return@setOnClickListener
            MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar publicación (Admin)")
                .setMessage("¿Deseas eliminar la publicación de ${pub.autorNombre} y sus comentarios?")
                .setPositiveButton("Eliminar") { _, _ ->
                    ServiceLocator.getComunidadRepository(this).eliminarPublicacion(pub.id)
                    Toast.makeText(this, "Publicación eliminada por moderación", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        btnEnviar.setOnClickListener {
            val pub = publicacionActual ?: return@setOnClickListener
            AuthGuardHelper.requireAuth(
                this,
                "Inicia sesión para comentar",
                "Puedes leer todos los comentarios libremente. Inicia sesión como Turista o Administrador para responder en esta experiencia."
            ) {
                val texto = etNuevoComentario.text.toString().trim()
                if (texto.isEmpty()) {
                    Toast.makeText(this, "Escribe un comentario primero", Toast.LENGTH_SHORT).show()
                    return@requireAuth
                }
                val sessionRepo = ServiceLocator.getSessionRepository(this)
                val usuario = sessionRepo.getUsuarioActual()
                val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                val nuevo = ComentarioPublicacion(
                    id = "com_${System.currentTimeMillis()}",
                    autorNombre = usuario?.nombre ?: "Viajero PerúViajero",
                    texto = texto,
                    fecha = fecha,
                    autorRol = if (usuario?.rol == RolUsuario.ADMIN) "ADMIN" else "TURISTA"
                )
                ServiceLocator.getComunidadRepository(this).agregarComentario(pub.id, nuevo)
                etNuevoComentario.setText("")
                cargarPublicacion()
                scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
                Toast.makeText(this, "Comentario publicado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmarEliminarComentario(comentario: ComentarioPublicacion) {
        val pub = publicacionActual ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle("Moderar comentario")
            .setMessage("¿Eliminar el comentario de ${comentario.autorNombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                ServiceLocator.getComunidadRepository(this).eliminarComentario(pub.id, comentario.id)
                cargarPublicacion()
                Toast.makeText(this, "Comentario eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private class FotoCarouselAdapter(
        val fotos: List<String>
    ) : RecyclerView.Adapter<FotoCarouselAdapter.VH>() {

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val ivFoto: ImageView = v.findViewById(R.id.ivSlidePubFoto)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_pub_foto_slide, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val uri = fotos[position]
            val model: Any = when {
                uri.isBlank() -> R.drawable.bg_destino_placeholder
                uri.startsWith("http") -> uri
                else -> Uri.parse(uri)
            }
            Glide.with(holder.itemView.context)
                .load(model)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .placeholder(R.drawable.bg_destino_placeholder)
                .error(R.drawable.bg_destino_placeholder)
                .into(holder.ivFoto)
        }

        override fun getItemCount(): Int = fotos.size
    }

    private class ComentarioAdapter(
        private val isAdminProvider: () -> Boolean,
        private val onDeleteClick: (ComentarioPublicacion) -> Unit
    ) : RecyclerView.Adapter<ComentarioAdapter.VH>() {

        private var items: List<ComentarioPublicacion> = emptyList()

        fun actualizar(nuevaLista: List<ComentarioPublicacion>) {
            items = nuevaLista
            notifyDataSetChanged()
        }

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvInicial: TextView = v.findViewById(R.id.tvComentarioInicial)
            val tvAutor: TextView = v.findViewById(R.id.tvComentarioAutor)
            val tvRolBadge: TextView = v.findViewById(R.id.tvComentarioRolBadge)
            val tvFecha: TextView = v.findViewById(R.id.tvComentarioFecha)
            val tvTexto: TextView = v.findViewById(R.id.tvComentarioTexto)
            val btnEliminar: TextView = v.findViewById(R.id.btnEliminarComentarioAdmin)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_comentario_publicacion, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val c = items[position]
            holder.tvInicial.text = c.autorNombre.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "V"
            holder.tvAutor.text = c.autorNombre
            holder.tvFecha.text = c.fecha
            holder.tvTexto.text = c.texto
            holder.tvRolBadge.visibility =
                if (c.rolNormalizado().equals("ADMIN", ignoreCase = true)) View.VISIBLE else View.GONE

            if (isAdminProvider()) {
                holder.btnEliminar.visibility = View.VISIBLE
                holder.btnEliminar.setOnClickListener { onDeleteClick(c) }
            } else {
                holder.btnEliminar.visibility = View.GONE
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
