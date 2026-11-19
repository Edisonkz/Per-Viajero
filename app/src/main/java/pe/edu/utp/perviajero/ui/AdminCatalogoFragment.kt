package pe.edu.utp.perviajero.ui

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.util.RegionPeruHelper

/**
 * Módulo exclusivo del Administrador para gestionar el Catálogo de Lugares Turísticos (CRUD):
 * crear nuevos destinos, editar existentes y activar/ocultar atractivos en vivo.
 */
class AdminCatalogoFragment : Fragment() {

    private lateinit var tvResumen: TextView
    private lateinit var rvCatalogo: RecyclerView
    private lateinit var fabNuevo: ExtendedFloatingActionButton
    private lateinit var adapter: AdminDestinoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_admin_catalogo, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvResumen = view.findViewById(R.id.tvResumenCatalogoAdmin)
        rvCatalogo = view.findViewById(R.id.rvAdminCatalogo)
        fabNuevo = view.findViewById(R.id.fabNuevoDestinoAdmin)

        adapter = AdminDestinoAdapter(
            isHiddenCheck = { id -> ServiceLocator.getDestinoRepository(requireContext()).esDestinoOculto(id) },
            onEdit = { destino -> mostrarDialogoCrearOEditar(destino) },
            onToggleHidden = { destino ->
                val ocultoAhora = ServiceLocator.getDestinoRepository(requireContext())
                    .toggleOcultarDestinoAdmin(destino.id)
                val msg = if (ocultoAhora) "'${destino.nombre}' ocultado del catálogo público" else "'${destino.nombre}' activado nuevamente"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                recargarCatalogo()
            }
        )
        rvCatalogo.layoutManager = LinearLayoutManager(requireContext())
        rvCatalogo.adapter = adapter

        fabNuevo.setOnClickListener { mostrarDialogoCrearOEditar(null) }
        recargarCatalogo()
    }

    override fun onResume() {
        super.onResume()
        recargarCatalogo()
    }

    private fun recargarCatalogo() {
        val repo = ServiceLocator.getDestinoRepository(requireContext())
        val lista = repo.obtenerCatalogoCompletoAdmin()
        val activos = lista.count { !repo.esDestinoOculto(it.id) }
        tvResumen.text = "Catálogo Turístico • $activos activos de ${lista.size} registrados"
        adapter.actualizar(lista)
    }

    fun mostrarDialogoCrearOEditar(existente: Destino?) {
        val ctx = requireContext()
        val scroll = ScrollView(ctx)
        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(44, 24, 44, 16)
        }

        val etNombre = EditText(ctx).apply {
            hint = "Nombre del lugar turístico *"
            setText(existente?.nombre ?: "")
        }
        val etDepto = EditText(ctx).apply {
            hint = "Departamento * (ej. Cusco, Arequipa, Ica)"
            setText(existente?.departamento ?: "")
        }
        val etProv = EditText(ctx).apply {
            hint = "Provincia (ej. Urubamba, Caylloma)"
            setText(existente?.let { RegionPeruHelper.resolveProvincia(it) } ?: "")
        }

        val categorias = listOf("Arqueología", "Naturaleza", "Aventura", "Cultura", "Playa")
        val spCategoria = Spinner(ctx).apply {
            adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, categorias)
            val idx = categorias.indexOfFirst { it.equals(existente?.categoria, ignoreCase = true) }
            if (idx >= 0) setSelection(idx)
        }

        val etCalif = EditText(ctx).apply {
            hint = "Calificación (1.0 - 5.0)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText((existente?.calificacion ?: 4.8).toString())
        }
        val etDesc = EditText(ctx).apply {
            hint = "Descripción turística *"
            minLines = 3
            setText(existente?.descripcion ?: "")
        }
        val etFotoUrl = EditText(ctx).apply {
            hint = "URL de imagen (opcional)"
            setText(existente?.imageUrl ?: "")
        }
        val etLat = EditText(ctx).apply {
            hint = "Latitud GPS (ej. -13.1631)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
            setText((existente?.latitud ?: -12.0464).toString())
        }
        val etLng = EditText(ctx).apply {
            hint = "Longitud GPS (ej. -72.5450)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
            setText((existente?.longitud ?: -77.0428).toString())
        }

        listOf(etNombre, etDepto, etProv, spCategoria, etCalif, etDesc, etFotoUrl, etLat, etLng)
            .forEach { layout.addView(it) }
        scroll.addView(layout)

        MaterialAlertDialogBuilder(ctx)
            .setTitle(if (existente == null) "Crear Nuevo Lugar Turístico" else "Editar Lugar Turístico")
            .setView(scroll)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = etNombre.text.toString().trim()
                val depto = etDepto.text.toString().trim()
                val desc = etDesc.text.toString().trim()
                if (nombre.isEmpty() || depto.isEmpty() || desc.isEmpty()) {
                    Toast.makeText(ctx, "Completa Nombre, Departamento y Descripción", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                val prov = etProv.text.toString().trim().ifEmpty { depto }
                val calif = etCalif.text.toString().toDoubleOrNull()?.coerceIn(1.0, 5.0) ?: 4.8
                val lat = etLat.text.toString().toDoubleOrNull() ?: -12.0464
                val lng = etLng.text.toString().toDoubleOrNull() ?: -77.0428
                val nuevo = Destino(
                    id = existente?.id ?: "dest_admin_${System.currentTimeMillis()}",
                    nombre = nombre,
                    departamento = depto,
                    categoria = spCategoria.selectedItem.toString(),
                    descripcion = desc,
                    calificacion = calif,
                    imageUrl = etFotoUrl.text.toString().trim(),
                    latitud = lat,
                    longitud = lng,
                    provincia = prov
                )
                ServiceLocator.getDestinoRepository(ctx).guardarDestinoAdmin(nuevo)
                recargarCatalogo()
                Toast.makeText(ctx, "Destino '${nuevo.nombre}' guardado y visible en Explorar", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private class AdminDestinoAdapter(
        private val isHiddenCheck: (String) -> Boolean,
        private val onEdit: (Destino) -> Unit,
        private val onToggleHidden: (Destino) -> Unit
    ) : RecyclerView.Adapter<AdminDestinoAdapter.VH>() {

        private var items: List<Destino> = emptyList()

        fun actualizar(nuevaLista: List<Destino>) {
            items = nuevaLista
            notifyDataSetChanged()
        }

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val ivFoto: ImageView = v.findViewById(R.id.ivAdminDestFoto)
            val tvNombre: TextView = v.findViewById(R.id.tvAdminDestNombre)
            val tvEstado: TextView = v.findViewById(R.id.tvAdminDestEstado)
            val tvUbicacion: TextView = v.findViewById(R.id.tvAdminDestUbicacion)
            val btnEditar: MaterialButton = v.findViewById(R.id.btnAdminDestEditar)
            val btnToggle: MaterialButton = v.findViewById(R.id.btnAdminDestToggleVisible)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_destino, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val dest = items[position]
            val ctx = holder.itemView.context
            val oculto = isHiddenCheck(dest.id)

            holder.tvNombre.text = dest.nombre
            val prov = RegionPeruHelper.resolveProvincia(dest)
            holder.tvUbicacion.text = "$prov, ${dest.departamento} • ${dest.categoria} • ★ ${dest.calificacion}"

            holder.tvEstado.text = if (oculto) "OCULTO" else "ACTIVO"
            holder.tvEstado.background = GradientDrawable().apply {
                cornerRadius = 100f
                setColor(
                    ContextCompat.getColor(
                        ctx,
                        if (oculto) R.color.colorAccent else R.color.colorPrimary
                    )
                )
            }
            holder.btnToggle.text = if (oculto) "Activar" else "Ocultar"
            holder.itemView.alpha = if (oculto) 0.65f else 1f

            Glide.with(ctx)
                .load(dest.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.bg_destino_placeholder)
                .error(R.drawable.bg_destino_placeholder)
                .into(holder.ivFoto)

            holder.btnEditar.setOnClickListener { onEdit(dest) }
            holder.btnToggle.setOnClickListener { onToggleHidden(dest) }
        }

        override fun getItemCount(): Int = items.size
    }
}
