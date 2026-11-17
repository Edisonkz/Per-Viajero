package pe.edu.utp.perviajero.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.adapter.PublicacionAdapter
import pe.edu.utp.perviajero.data.model.ComentarioPublicacion
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.data.model.PublicacionViajero
import pe.edu.utp.perviajero.di.ServiceLocator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Red social Comunidad de Viajeros: experiencias reales con fotografía filtrables por Región y Destino.
 */
class ComunidadActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FILTER_REGION = "extra_filter_region"
        const val EXTRA_FILTER_DESTINO_ID = "extra_filter_destino_id"
        private val REGIONES_PERU = listOf(
            "Todas", "Cusco", "Arequipa", "Ica", "Puno", "Áncash", "Amazonas", "Piura", "Loreto", "La Libertad", "Lima"
        )
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var chipGroupRegiones: ChipGroup
    private lateinit var spFiltroDestino: Spinner
    private lateinit var rvComunidad: RecyclerView
    private lateinit var layoutVacio: View
    private lateinit var fabPublicar: ExtendedFloatingActionButton
    private lateinit var adapter: PublicacionAdapter

    private var selectedRegion: String = "Todas"
    private var selectedDestinoId: String = "ALL"
    private var destinosDisponibles: List<Destino> = emptyList()
    private var destinosSpinnerIds: List<String> = emptyList()
    private var ignoreSpinnerSelection = false

    private val publicarLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            recargarPublicaciones()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comunidad)

        selectedRegion = intent.getStringExtra(EXTRA_FILTER_REGION) ?: "Todas"
        selectedDestinoId = intent.getStringExtra(EXTRA_FILTER_DESTINO_ID) ?: "ALL"

        inicializarVistas()
        cargarDestinosParaFiltros()
        configurarChipsRegiones()
        configurarEventos()
        recargarPublicaciones()
    }

    override fun onResume() {
        super.onResume()
        recargarPublicaciones()
    }

    private fun inicializarVistas() {
        toolbar = findViewById(R.id.toolbarComunidad)
        chipGroupRegiones = findViewById(R.id.chipGroupRegiones)
        spFiltroDestino = findViewById(R.id.spFiltroDestinoComunidad)
        rvComunidad = findViewById(R.id.rvComunidad)
        layoutVacio = findViewById(R.id.layoutComunidadVacio)
        fabPublicar = findViewById(R.id.fabPublicarExperiencia)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        rvComunidad.layoutManager = LinearLayoutManager(this)
        adapter = PublicacionAdapter(
            onLikeClick = { pub ->
                ServiceLocator.getComunidadRepository(this).toggleLike(pub.id)
                recargarPublicaciones()
            },
            onCommentClick = { pub ->
                mostrarDetallePublicacion(pub)
            },
            onVerDestinoClick = { pub ->
                abrirDestinoDesdePublicacion(pub)
            }
        )
        rvComunidad.adapter = adapter
    }

    private fun cargarDestinosParaFiltros() {
        destinosDisponibles = ServiceLocator.getDestinoRepository(this).obtenerDestinosEnCache()
        actualizarSpinnerDestinos()
    }

    private fun configurarChipsRegiones() {
        chipGroupRegiones.removeAllViews()
        for (region in REGIONES_PERU) {
            val chip = Chip(this).apply {
                id = View.generateViewId()
                text = region
                isCheckable = true
                isChecked = region.equals(selectedRegion, ignoreCase = true)
            }
            chipGroupRegiones.addView(chip)
        }

        chipGroupRegiones.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val chip = group.findViewById<Chip>(checkedId) ?: return@setOnCheckedStateChangeListener
            selectedRegion = chip.text.toString()
            selectedDestinoId = "ALL"
            actualizarSpinnerDestinos()
            recargarPublicaciones()
        }
    }

    private fun actualizarSpinnerDestinos() {
        val filtrados = if (selectedRegion == "Todas") {
            destinosDisponibles
        } else {
            destinosDisponibles.filter { it.departamento.equals(selectedRegion, ignoreCase = true) }
        }

        val labels = mutableListOf("Todos los destinos")
        val ids = mutableListOf("ALL")
        for (d in filtrados) {
            labels.add(d.nombre)
            ids.add(d.id)
        }
        destinosSpinnerIds = ids

        ignoreSpinnerSelection = true
        spFiltroDestino.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)
        val preIndex = ids.indexOf(selectedDestinoId).takeIf { it >= 0 } ?: 0
        spFiltroDestino.setSelection(preIndex)
        spFiltroDestino.post { ignoreSpinnerSelection = false }

        spFiltroDestino.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (ignoreSpinnerSelection) return
                selectedDestinoId = destinosSpinnerIds.getOrElse(position) { "ALL" }
                recargarPublicaciones()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun configurarEventos() {
        fabPublicar.setOnClickListener {
            val intent = Intent(this, PublicarExperienciaActivity::class.java).apply {
                if (selectedRegion != "Todas") {
                    putExtra(PublicarExperienciaActivity.EXTRA_PRESELECTED_REGION, selectedRegion)
                }
                if (selectedDestinoId != "ALL") {
                    putExtra(PublicarExperienciaActivity.EXTRA_PRESELECTED_DESTINO_ID, selectedDestinoId)
                }
            }
            publicarLauncher.launch(intent)
        }
    }

    private fun recargarPublicaciones() {
        val repo = ServiceLocator.getComunidadRepository(this)
        val lista = repo.filtrarPublicaciones(selectedRegion, selectedDestinoId)
        if (lista.isEmpty()) {
            layoutVacio.visibility = View.VISIBLE
            rvComunidad.visibility = View.GONE
        } else {
            layoutVacio.visibility = View.GONE
            rvComunidad.visibility = View.VISIBLE
            adapter.actualizarLista(lista)
        }
    }

    private fun mostrarDetallePublicacion(pub: PublicacionViajero) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_detalle_publicacion, null)
        val ivFoto: ImageView = dialogView.findViewById(R.id.ivDetallePubFoto)
        val tvTitulo: TextView = dialogView.findViewById(R.id.tvDetallePubTitulo)
        val tvTexto: TextView = dialogView.findViewById(R.id.tvDetallePubTexto)
        val tvComentarios: TextView = dialogView.findViewById(R.id.tvDetallePubComentarios)
        val etNuevoComentario: EditText = dialogView.findViewById(R.id.etNuevoComentarioPub)
        val btnEnviar: MaterialButton = dialogView.findViewById(R.id.btnEnviarComentarioPub)

        val imageModel: Any = if (pub.fotoUri.startsWith("http")) pub.fotoUri else Uri.parse(pub.fotoUri)
        Glide.with(this)
            .load(imageModel)
            .centerCrop()
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(ivFoto)

        tvTitulo.text = "${pub.autorNombre} en ${pub.destinoNombre} (${pub.region})"
        tvTexto.text = pub.comentario

        fun renderComments(lista: List<ComentarioPublicacion>) {
            tvComentarios.text = if (lista.isEmpty()) {
                "Aún no hay comentarios. ¡Sé el primero en responder!"
            } else {
                lista.joinToString("\n\n") { c -> "• ${c.autorNombre} (${c.fecha}):\n  ${c.texto}" }
            }
        }
        renderComments(pub.comentarios)

        btnEnviar.setOnClickListener {
            val texto = etNuevoComentario.text.toString().trim()
            if (texto.isEmpty()) {
                Toast.makeText(this, "Escribe un comentario primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val autor = ServiceLocator.getSessionRepository(this).getUsuarioActual()?.nombre ?: "Viajero Invitado"
            val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val nuevo = ComentarioPublicacion(
                id = "com_${System.currentTimeMillis()}",
                autorNombre = autor,
                texto = texto,
                fecha = fecha
            )
            val actualizada = ServiceLocator.getComunidadRepository(this).agregarComentario(pub.id, nuevo)
            etNuevoComentario.setText("")
            renderComments(actualizada?.comentarios ?: (pub.comentarios + nuevo))
            recargarPublicaciones()
            Toast.makeText(this, "Comentario publicado", Toast.LENGTH_SHORT).show()
        }

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun abrirDestinoDesdePublicacion(pub: PublicacionViajero) {
        val destino = destinosDisponibles.find { it.id == pub.destinoId }
        if (destino != null) {
            val intent = Intent(this, DetalleDestinoActivity::class.java).apply {
                putExtra(DetalleDestinoActivity.EXTRA_DESTINO, destino)
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Destino: ${pub.destinoNombre} (${pub.region})", Toast.LENGTH_SHORT).show()
        }
    }
}
