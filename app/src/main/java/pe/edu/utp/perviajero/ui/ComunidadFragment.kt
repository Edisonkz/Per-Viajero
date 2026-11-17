package pe.edu.utp.perviajero.ui

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
 * Fragmento reutilizable de la Red Social Comunidad de Viajeros.
 * Se muestra en el Tab 2 del menú principal y también dentro de ComunidadActivity al filtrar desde un destino.
 */
class ComunidadFragment : Fragment() {

    companion object {
        const val ARG_REGION = "arg_region"
        const val ARG_DESTINO_ID = "arg_destino_id"
        private val REGIONES_PERU = listOf(
            "Todas", "Cusco", "Arequipa", "Ica", "Puno", "Áncash", "Amazonas", "Piura", "Loreto", "La Libertad", "Lima"
        )

        fun newInstance(region: String = "Todas", destinoId: String = "ALL"): ComunidadFragment {
            return ComunidadFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_REGION, region)
                    putString(ARG_DESTINO_ID, destinoId)
                }
            }
        }
    }

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_comunidad, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedRegion = arguments?.getString(ARG_REGION) ?: "Todas"
        selectedDestinoId = arguments?.getString(ARG_DESTINO_ID) ?: "ALL"

        chipGroupRegiones = view.findViewById(R.id.chipGroupRegiones)
        spFiltroDestino = view.findViewById(R.id.spFiltroDestinoComunidad)
        rvComunidad = view.findViewById(R.id.rvComunidad)
        layoutVacio = view.findViewById(R.id.layoutComunidadVacio)
        fabPublicar = view.findViewById(R.id.fabPublicarExperiencia)

        rvComunidad.layoutManager = LinearLayoutManager(requireContext())
        adapter = PublicacionAdapter(
            onLikeClick = { pub ->
                ServiceLocator.getComunidadRepository(requireContext()).toggleLike(pub.id)
                recargarPublicaciones()
            },
            onCommentClick = { pub -> mostrarDetallePublicacion(pub) },
            onVerDestinoClick = { pub -> abrirDestinoDesdePublicacion(pub) }
        )
        rvComunidad.adapter = adapter

        cargarDestinosParaFiltros()
        configurarChipsRegiones()
        configurarEventos()
        recargarPublicaciones()
    }

    override fun onResume() {
        super.onResume()
        recargarPublicaciones()
    }

    private fun cargarDestinosParaFiltros() {
        destinosDisponibles = ServiceLocator.getDestinoRepository(requireContext()).obtenerDestinosEnCache()
        actualizarSpinnerDestinos()
    }

    private fun configurarChipsRegiones() {
        chipGroupRegiones.removeAllViews()
        for (region in REGIONES_PERU) {
            val chip = Chip(requireContext()).apply {
                id = View.generateViewId()
                text = region
                isCheckable = true
                isChecked = region.equals(selectedRegion, ignoreCase = true)
                checkedIcon = null
                shapeAppearanceModel = shapeAppearanceModel.toBuilder().setAllCornerSizes(100f).build()
                aplicarEstiloChip(this, isChecked)
            }
            chipGroupRegiones.addView(chip)
        }

        chipGroupRegiones.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            for (i in 0 until group.childCount) {
                val c = group.getChildAt(i) as? Chip ?: continue
                aplicarEstiloChip(c, c.id == checkedId)
            }
            val chip = group.findViewById<Chip>(checkedId) ?: return@setOnCheckedStateChangeListener
            selectedRegion = chip.text.toString()
            selectedDestinoId = "ALL"
            actualizarSpinnerDestinos()
            recargarPublicaciones()
        }
    }

    private fun aplicarEstiloChip(chip: Chip, isSelected: Boolean) {
        val primary = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        val cardBg = ContextCompat.getColor(requireContext(), R.color.colorCardBackground)
        val textPrimary = ContextCompat.getColor(requireContext(), R.color.colorTextPrimary)
        if (isSelected) {
            chip.chipBackgroundColor = ColorStateList.valueOf(primary)
            chip.setTextColor(Color.WHITE)
            chip.chipStrokeWidth = 0f
        } else {
            chip.chipBackgroundColor = ColorStateList.valueOf(cardBg)
            chip.setTextColor(textPrimary)
            chip.chipStrokeColor = ColorStateList.valueOf(Color.argb(40, 0, 0, 0))
            chip.chipStrokeWidth = 2f
        }
    }

    private fun actualizarSpinnerDestinos() {
        val filtrados = if (selectedRegion == "Todas") {
            destinosDisponibles
        } else {
            destinosDisponibles.filter { it.departamento.equals(selectedRegion, ignoreCase = true) }
        }

        val labels = mutableListOf("Todos")
        val ids = mutableListOf("ALL")
        for (d in filtrados) {
            labels.add(d.nombre)
            ids.add(d.id)
        }
        destinosSpinnerIds = ids

        ignoreSpinnerSelection = true
        spFiltroDestino.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, labels)
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
            val intent = Intent(requireContext(), PublicarExperienciaActivity::class.java).apply {
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
        val lista = ServiceLocator.getComunidadRepository(requireContext())
            .filtrarPublicaciones(selectedRegion, selectedDestinoId)
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
        val ctx = requireContext()
        val dialogView = LayoutInflater.from(ctx).inflate(R.layout.dialog_detalle_publicacion, null)
        val ivFoto: ImageView = dialogView.findViewById(R.id.ivDetallePubFoto)
        val tvTitulo: TextView = dialogView.findViewById(R.id.tvDetallePubTitulo)
        val tvTexto: TextView = dialogView.findViewById(R.id.tvDetallePubTexto)
        val tvComentarios: TextView = dialogView.findViewById(R.id.tvDetallePubComentarios)
        val etNuevoComentario: EditText = dialogView.findViewById(R.id.etNuevoComentarioPub)
        val btnEnviar: MaterialButton = dialogView.findViewById(R.id.btnEnviarComentarioPub)

        val imageModel: Any = if (pub.fotoUri.startsWith("http")) pub.fotoUri else Uri.parse(pub.fotoUri)
        Glide.with(ctx)
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
                Toast.makeText(ctx, "Escribe un comentario primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val autor = ServiceLocator.getSessionRepository(ctx).getUsuarioActual()?.nombre ?: "Viajero Invitado"
            val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val nuevo = ComentarioPublicacion(
                id = "com_${System.currentTimeMillis()}",
                autorNombre = autor,
                texto = texto,
                fecha = fecha
            )
            val actualizada = ServiceLocator.getComunidadRepository(ctx).agregarComentario(pub.id, nuevo)
            etNuevoComentario.setText("")
            renderComments(actualizada?.comentarios ?: (pub.comentarios + nuevo))
            recargarPublicaciones()
            Toast.makeText(ctx, "Comentario publicado", Toast.LENGTH_SHORT).show()
        }

        MaterialAlertDialogBuilder(ctx)
            .setView(dialogView)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun abrirDestinoDesdePublicacion(pub: PublicacionViajero) {
        val destino = destinosDisponibles.find { it.id == pub.destinoId }
        if (destino != null) {
            val intent = Intent(requireContext(), DetalleDestinoActivity::class.java).apply {
                putExtra(DetalleDestinoActivity.EXTRA_DESTINO, destino)
            }
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "Destino: ${pub.destinoNombre} (${pub.region})", Toast.LENGTH_SHORT).show()
        }
    }
}
