package pe.edu.utp.perviajero.ui

import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.data.model.UbigeoCatalogo
import pe.edu.utp.perviajero.data.model.UbigeoNode
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.services.UbigeoPeruService
import pe.edu.utp.perviajero.util.CameraFileHelper
import pe.edu.utp.perviajero.util.RegionPeruHelper

/**
 * Módulo exclusivo del Administrador para gestionar el Catálogo de Lugares Turísticos (CRUD):
 * crear nuevos destinos con selector en cascada Departamento -> Provincia -> Distrito (API Ubigeo Perú),
 * editar existentes y activar/ocultar atractivos en vivo.
 */
class AdminCatalogoFragment : Fragment() {

    private lateinit var tvResumen: TextView
    private lateinit var rvCatalogo: RecyclerView
    private lateinit var fabNuevo: ExtendedFloatingActionButton
    private lateinit var adapter: AdminDestinoAdapter

    private var activeFotoUrlInput: TextInputEditText? = null

    private val pickDestinoImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && isAdded) {
            val savedPath = CameraFileHelper.copyUriToInternalStorage(requireContext(), uri, "dest_admin")
            if (savedPath != null) {
                activeFotoUrlInput?.setText(savedPath)
                Toast.makeText(requireContext(), "Fotografía adjuntada al destino", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "No se pudo importar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
        val dialogView = LayoutInflater.from(ctx).inflate(R.layout.dialog_admin_destino, null)

        val etNombre: TextInputEditText = dialogView.findViewById(R.id.etAdminDestNombre)
        val pbUbigeo: ProgressBar = dialogView.findViewById(R.id.pbAdminUbigeo)
        val tvUbigeoFuente: TextView = dialogView.findViewById(R.id.tvAdminUbigeoFuente)
        val spDepto: Spinner = dialogView.findViewById(R.id.spAdminDepartamento)
        val spProv: Spinner = dialogView.findViewById(R.id.spAdminProvincia)
        val spDist: Spinner = dialogView.findViewById(R.id.spAdminDistrito)
        val spCategoria: Spinner = dialogView.findViewById(R.id.spAdminCategoria)
        val etDesc: TextInputEditText = dialogView.findViewById(R.id.etAdminDestDescripcion)
        val etFotoUrl: TextInputEditText = dialogView.findViewById(R.id.etAdminDestFotoUrl)
        val btnGaleria: MaterialButton = dialogView.findViewById(R.id.btnAdminDestGaleria)
        val etCalif: TextInputEditText = dialogView.findViewById(R.id.etAdminDestCalif)
        val etLat: TextInputEditText = dialogView.findViewById(R.id.etAdminDestLat)
        val etLng: TextInputEditText = dialogView.findViewById(R.id.etAdminDestLng)

        activeFotoUrlInput = etFotoUrl
        btnGaleria.setOnClickListener { pickDestinoImageLauncher.launch("image/*") }

        etNombre.setText(existente?.nombre ?: "")
        etDesc.setText(existente?.descripcion ?: "")
        etFotoUrl.setText(existente?.imageUrl ?: "")
        etCalif.setText((existente?.calificacion ?: 4.8).toString())
        etLat.setText((existente?.latitud ?: -12.0464).toString())
        etLng.setText((existente?.longitud ?: -77.0428).toString())

        val categorias = listOf("Arqueología", "Naturaleza", "Aventura", "Cultura", "Playa")
        spCategoria.adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, categorias)
        val catIdx = categorias.indexOfFirst { it.equals(existente?.categoria, ignoreCase = true) }
        if (catIdx >= 0) spCategoria.setSelection(catIdx)

        var catalogoActual: UbigeoCatalogo? = null
        var deptosActuales: List<UbigeoNode> = emptyList()
        var provsActuales: List<UbigeoNode> = emptyList()
        var distsActuales: List<UbigeoNode> = emptyList()
        var pendingProvName: String? = existente?.let { RegionPeruHelper.resolveProvincia(it) }
        var pendingDistName: String? = existente?.distrito

        fun actualizarDistritos(provNode: UbigeoNode) {
            val cat = catalogoActual ?: return
            distsActuales = UbigeoPeruService.resolveDistritos(cat, provNode.id)
            val labels = if (distsActuales.isNotEmpty()) distsActuales.map { it.nombre } else listOf(provNode.nombre)
            spDist.adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, labels)

            val targetDist = pendingDistName
            if (!targetDist.isNullOrBlank() && distsActuales.isNotEmpty()) {
                val idx = UbigeoPeruService.findNodeIndexByName(distsActuales, targetDist)
                spDist.setSelection(idx)
                pendingDistName = null
            }
        }

        fun actualizarProvincias(deptoNode: UbigeoNode) {
            val cat = catalogoActual ?: return
            provsActuales = UbigeoPeruService.resolveProvincias(cat, deptoNode.id)
            val labels = if (provsActuales.isNotEmpty()) provsActuales.map { it.nombre } else listOf(deptoNode.nombre)
            spProv.adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, labels)

            val targetProv = pendingProvName
            val initialProvIdx = if (!targetProv.isNullOrBlank() && provsActuales.isNotEmpty()) {
                pendingProvName = null
                UbigeoPeruService.findNodeIndexByName(provsActuales, targetProv)
            } else 0
            spProv.setSelection(initialProvIdx)

            spProv.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val prov = provsActuales.getOrNull(position) ?: return
                    actualizarDistritos(prov)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            provsActuales.getOrNull(initialProvIdx)?.let { actualizarDistritos(it) }
        }

        // Cargar Ubigeo Perú desde API (con fallback automático offline)
        viewLifecycleOwner.lifecycleScope.launch {
            val catalogo = ServiceLocator.getUbigeoService(ctx).obtenerCatalogo()
            if (!isAdded) return@launch
            catalogoActual = catalogo
            deptosActuales = catalogo.departamentos
            pbUbigeo.visibility = View.GONE
            tvUbigeoFuente.text =
                "${catalogo.fuente} • ${catalogo.departamentos.size} deptos • ${catalogo.totalProvincias()} provs • ${catalogo.totalDistritos()} distritos"

            spDepto.adapter = ArrayAdapter(
                ctx,
                android.R.layout.simple_spinner_dropdown_item,
                deptosActuales.map { it.nombre }
            )

            val initialDeptoIdx = if (existente != null) {
                UbigeoPeruService.findNodeIndexByName(deptosActuales, existente.departamento)
            } else {
                UbigeoPeruService.findNodeIndexByName(deptosActuales, "Cusco")
            }
            spDepto.setSelection(initialDeptoIdx)

            spDepto.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val depto = deptosActuales.getOrNull(position) ?: return
                    actualizarProvincias(depto)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            deptosActuales.getOrNull(initialDeptoIdx)?.let { actualizarProvincias(it) }
        }

        val dialog = MaterialAlertDialogBuilder(ctx)
            .setTitle(if (existente == null) "Crear Nuevo Lugar Turístico" else "Editar Lugar Turístico")
            .setView(dialogView)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnDismissListener {
            activeFotoUrlInput = null
        }

        dialog.setOnShowListener {
            val positiveBtn = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
            positiveBtn.setOnClickListener {
                val nombre = etNombre.text?.toString()?.trim().orEmpty()
                val desc = etDesc.text?.toString()?.trim().orEmpty()
                val depto = deptosActuales.getOrNull(spDepto.selectedItemPosition)?.nombre
                    ?: existente?.departamento.orEmpty()
                val prov = provsActuales.getOrNull(spProv.selectedItemPosition)?.nombre
                    ?: existente?.provincia
                    ?: depto
                val dist = distsActuales.getOrNull(spDist.selectedItemPosition)?.nombre
                    ?: existente?.distrito
                    ?: prov

                if (nombre.isEmpty() || depto.isEmpty() || desc.isEmpty()) {
                    Toast.makeText(ctx, "Completa Nombre, Ubicación (Ubigeo) y Descripción", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val calif = etCalif.text?.toString()?.toDoubleOrNull()?.coerceIn(1.0, 5.0) ?: 4.8
                val lat = etLat.text?.toString()?.toDoubleOrNull() ?: -12.0464
                val lng = etLng.text?.toString()?.toDoubleOrNull() ?: -77.0428
                val nuevo = Destino(
                    id = existente?.id ?: "dest_admin_${System.currentTimeMillis()}",
                    nombre = nombre,
                    departamento = depto,
                    categoria = spCategoria.selectedItem.toString(),
                    descripcion = desc,
                    calificacion = calif,
                    imageUrl = etFotoUrl.text?.toString()?.trim().orEmpty(),
                    latitud = lat,
                    longitud = lng,
                    provincia = prov,
                    distrito = dist
                )
                ServiceLocator.getDestinoRepository(ctx).guardarDestinoAdmin(nuevo)
                recargarCatalogo()
                Toast.makeText(
                    ctx,
                    "Destino '${nuevo.nombre}' ($dist, $prov, $depto) guardado en Explorar",
                    Toast.LENGTH_LONG
                ).show()
                dialog.dismiss()
            }
        }
        dialog.show()
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
            val ubicacion = listOfNotNull(
                dest.distrito?.takeIf { it.isNotBlank() },
                prov.takeIf { it.isNotBlank() },
                dest.departamento.takeIf { it.isNotBlank() }
            ).distinct().joinToString(", ")
            holder.tvUbicacion.text = "$ubicacion • ${dest.categoria} • ★ ${dest.calificacion}"

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

            val imageModel: Any = when {
                dest.imageUrl.isBlank() -> R.drawable.bg_destino_placeholder
                dest.imageUrl.startsWith("http") -> dest.imageUrl
                else -> Uri.parse(dest.imageUrl)
            }
            Glide.with(ctx)
                .load(imageModel)
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
