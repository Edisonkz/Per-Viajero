package pe.edu.utp.perviajero.ui

import android.Manifest
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RatingBar
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
import com.google.android.material.textfield.TextInputEditText
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.data.model.PublicacionViajero
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.util.AppLogger
import pe.edu.utp.perviajero.util.CameraFileHelper
import pe.edu.utp.perviajero.util.PermissionHelper
import pe.edu.utp.perviajero.util.WindowInsetsHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla para publicar experiencias multi-foto en la Comunidad de Viajeros
 * usando la Cámara real del dispositivo o selección múltiple desde Galería (hasta 5 fotos).
 */
class PublicarExperienciaActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRESELECTED_REGION = "extra_preselected_region"
        const val EXTRA_PRESELECTED_DESTINO_ID = "extra_preselected_destino_id"
        private const val MAX_FOTOS = 5
        private const val TAG = "PublicarExperiencia"
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var spRegion: Spinner
    private lateinit var spDestino: Spinner
    private lateinit var tvContadorFotos: TextView
    private lateinit var ivPreviewFoto: ImageView
    private lateinit var rvFotosSeleccionadas: RecyclerView
    private lateinit var btnTomarFoto: MaterialButton
    private lateinit var btnElegirGaleria: MaterialButton
    private lateinit var rbCalificacion: RatingBar
    private lateinit var etComentario: TextInputEditText
    private lateinit var btnPublicar: MaterialButton

    private lateinit var thumbsAdapter: SelectedPhotosAdapter
    private val selectedPhotoUris = mutableListOf<String>()

    private var todosDestinos: List<Destino> = emptyList()
    private var destinosFiltrados: List<Destino> = emptyList()
    private var regionesDisponibles: List<String> = emptyList()

    private var pendingCameraContentUri: Uri? = null
    private var pendingCameraFileUriString: String = ""
    private var preselectedDestinoId: String? = null

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            lanzarCamara()
        } else if (!PermissionHelper.shouldShowRationale(this, Manifest.permission.CAMERA)) {
            PermissionHelper.showPermanentlyDeniedDialog(
                this,
                "Permiso de Cámara Requerido",
                "Para tomar fotos reales de tu viaje y publicarlas en la comunidad, habilita el acceso a la cámara."
            )
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCameraFileUriString.isNotEmpty()) {
            agregarFoto(pendingCameraFileUriString)
            AppLogger.i(TAG, "Foto capturada con cámara: $pendingCameraFileUriString")
        }
    }

    private val multiGalleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNullOrEmpty()) return@registerForActivityResult
        var agregadas = 0
        for (uri in uris) {
            if (selectedPhotoUris.size >= MAX_FOTOS) {
                Toast.makeText(this, "Máximo $MAX_FOTOS fotografías por publicación", Toast.LENGTH_SHORT).show()
                break
            }
            val savedPath = CameraFileHelper.copyUriToInternalStorage(this, uri, "pub_gal")
            if (savedPath != null) {
                selectedPhotoUris.add(savedPath)
                agregadas++
            }
        }
        if (agregadas > 0) {
            actualizarGaleriaSeleccionada(selectedPhotoUris.last())
        } else {
            Toast.makeText(this, "No se pudieron importar las imágenes seleccionadas", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publicar_experiencia)

        preselectedDestinoId = intent.getStringExtra(EXTRA_PRESELECTED_DESTINO_ID)
        val preselectedRegion = intent.getStringExtra(EXTRA_PRESELECTED_REGION)

        inicializarVistas()
        cargarCatalogoRegionesYDestinos(preselectedRegion)
        configurarEventos()
        actualizarGaleriaSeleccionada(null)
    }

    private fun inicializarVistas() {
        toolbar = findViewById(R.id.toolbarPublicar)
        WindowInsetsHelper.applyToolbarActivityInsets(this, toolbar)
        spRegion = findViewById(R.id.spPubRegion)
        spDestino = findViewById(R.id.spPubDestino)
        tvContadorFotos = findViewById(R.id.tvContadorFotosPub)
        ivPreviewFoto = findViewById(R.id.ivPreviewFotoPub)
        rvFotosSeleccionadas = findViewById(R.id.rvFotosSeleccionadasPub)
        btnTomarFoto = findViewById(R.id.btnTomarFotoCamara)
        btnElegirGaleria = findViewById(R.id.btnElegirGaleria)
        rbCalificacion = findViewById(R.id.rbPubCalificacion)
        etComentario = findViewById(R.id.etPubComentario)
        btnPublicar = findViewById(R.id.btnGuardarPublicacion)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        thumbsAdapter = SelectedPhotosAdapter(
            onSelect = { uri -> mostrarVistaPrevia(uri) },
            onRemove = { index ->
                if (index in selectedPhotoUris.indices) {
                    selectedPhotoUris.removeAt(index)
                    actualizarGaleriaSeleccionada(selectedPhotoUris.lastOrNull())
                }
            }
        )
        rvFotosSeleccionadas.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvFotosSeleccionadas.adapter = thumbsAdapter
    }

    private fun cargarCatalogoRegionesYDestinos(preselectedRegion: String?) {
        todosDestinos = ServiceLocator.getDestinoRepository(this).obtenerDestinosEnCache()
        val regiones = todosDestinos.map { it.departamento }.distinct().sorted()
        regionesDisponibles = if (regiones.isNotEmpty()) {
            regiones
        } else {
            listOf("Cusco", "Arequipa", "Ica", "Puno", "Áncash", "Amazonas", "Piura", "Loreto", "La Libertad", "Lima")
        }

        spRegion.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, regionesDisponibles)

        val regionTarget = preselectedRegion
            ?: todosDestinos.find { it.id == preselectedDestinoId }?.departamento
        val regionIndex = regionesDisponibles.indexOfFirst { it.equals(regionTarget, ignoreCase = true) }
        if (regionIndex >= 0) {
            spRegion.setSelection(regionIndex)
        }

        spRegion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val region = regionesDisponibles.getOrElse(position) { "Cusco" }
                actualizarSpinnerDestinosPorRegion(region)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        actualizarSpinnerDestinosPorRegion(regionesDisponibles.getOrElse(spRegion.selectedItemPosition) { "Cusco" })
    }

    private fun actualizarSpinnerDestinosPorRegion(region: String) {
        destinosFiltrados = todosDestinos.filter { it.departamento.equals(region, ignoreCase = true) }
        val nombres = if (destinosFiltrados.isNotEmpty()) {
            destinosFiltrados.map { it.nombre }
        } else {
            listOf("Atractivo Turístico de $region")
        }

        spDestino.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nombres)
        val destIdx = destinosFiltrados.indexOfFirst { it.id == preselectedDestinoId }
        if (destIdx >= 0) {
            spDestino.setSelection(destIdx)
            preselectedDestinoId = null
        }
    }

    private fun configurarEventos() {
        btnTomarFoto.setOnClickListener {
            if (selectedPhotoUris.size >= MAX_FOTOS) {
                Toast.makeText(this, "Ya alcanzaste el máximo de $MAX_FOTOS fotos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            solicitarPermisoYTomarFoto()
        }

        btnElegirGaleria.setOnClickListener {
            if (selectedPhotoUris.size >= MAX_FOTOS) {
                Toast.makeText(this, "Ya alcanzaste el máximo de $MAX_FOTOS fotos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            multiGalleryLauncher.launch("image/*")
        }

        btnPublicar.setOnClickListener {
            publicarExperiencia()
        }
    }

    private fun agregarFoto(uriString: String) {
        if (selectedPhotoUris.size >= MAX_FOTOS) {
            Toast.makeText(this, "Máximo $MAX_FOTOS fotografías por publicación", Toast.LENGTH_SHORT).show()
            return
        }
        selectedPhotoUris.add(uriString)
        actualizarGaleriaSeleccionada(uriString)
    }

    private fun actualizarGaleriaSeleccionada(previewUri: String?) {
        tvContadorFotos.text = "${selectedPhotoUris.size} / $MAX_FOTOS fotos"
        thumbsAdapter.actualizar(selectedPhotoUris)
        rvFotosSeleccionadas.visibility = if (selectedPhotoUris.isNotEmpty()) View.VISIBLE else View.GONE

        if (!previewUri.isNullOrBlank()) {
            mostrarVistaPrevia(previewUri)
        } else if (selectedPhotoUris.isEmpty()) {
            ivPreviewFoto.setImageResource(android.R.drawable.ic_menu_camera)
        }
    }

    private fun solicitarPermisoYTomarFoto() {
        when {
            PermissionHelper.hasCameraPermission(this) -> lanzarCamara()
            PermissionHelper.shouldShowRationale(this, Manifest.permission.CAMERA) -> {
                PermissionHelper.showRationaleDialog(
                    this,
                    "Acceso a la Cámara",
                    "PerúViajero utiliza la cámara únicamente para capturar las fotografías de tu visita turística."
                ) {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun lanzarCamara() {
        val (contentUri, fileUri) = CameraFileHelper.createPhotoOutputUri(this, "pub_cam")
        pendingCameraContentUri = contentUri
        pendingCameraFileUriString = fileUri
        takePictureLauncher.launch(contentUri)
    }

    private fun mostrarVistaPrevia(uriString: String) {
        Glide.with(this)
            .load(Uri.parse(uriString))
            .centerCrop()
            .into(ivPreviewFoto)
    }

    private fun publicarExperiencia() {
        val comentario = etComentario.text?.toString()?.trim().orEmpty()
        if (comentario.length < 8) {
            Toast.makeText(this, "Escribe una reseña breve (mínimo 8 caracteres)", Toast.LENGTH_SHORT).show()
            return
        }

        val region = regionesDisponibles.getOrElse(spRegion.selectedItemPosition) { "Cusco" }
        val destinoObj = destinosFiltrados.getOrNull(spDestino.selectedItemPosition)
        val destinoId = destinoObj?.id ?: "custom_${region.lowercase()}"
        val destinoNombre = destinoObj?.nombre ?: spDestino.selectedItem?.toString().orEmpty()

        val fotosFinales = if (selectedPhotoUris.isNotEmpty()) {
            selectedPhotoUris.toList()
        } else {
            listOf(destinoObj?.imageUrl ?: "https://images.unsplash.com/photo-1526392060635-9d6019884377?w=800")
        }

        val sessionRepo = ServiceLocator.getSessionRepository(this)
        val usuario = sessionRepo.getUsuarioActual()
        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        val nueva = PublicacionViajero(
            id = "pub_${System.currentTimeMillis()}",
            autorNombre = usuario?.nombre ?: "Viajero PerúViajero",
            autorEmail = usuario?.email ?: "turista@peruviajero.pe",
            autorFotoUri = sessionRepo.getUserFotoUri(),
            region = region,
            destinoId = destinoId,
            destinoNombre = destinoNombre,
            fotoUri = fotosFinales.first(),
            fotosUris = fotosFinales,
            calificacion = rbCalificacion.rating,
            comentario = comentario,
            fechaPublicacion = fecha
        )

        ServiceLocator.getComunidadRepository(this).agregarPublicacion(nueva)
        AppLogger.i(TAG, "Nueva experiencia publicada en $region ($destinoNombre) con ${fotosFinales.size} foto(s)")
        Toast.makeText(this, "¡Experiencia publicada con ${fotosFinales.size} foto(s)!", Toast.LENGTH_SHORT).show()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private class SelectedPhotosAdapter(
        private val onSelect: (String) -> Unit,
        private val onRemove: (Int) -> Unit
    ) : RecyclerView.Adapter<SelectedPhotosAdapter.VH>() {

        private var items: List<String> = emptyList()

        fun actualizar(nuevaLista: List<String>) {
            items = nuevaLista.toList()
            notifyDataSetChanged()
        }

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val ivThumb: ImageView = v.findViewById(R.id.ivThumbFoto)
            val btnQuitar: View = v.findViewById(R.id.btnQuitarThumbFoto)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_pub_foto_thumb, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val uri = items[position]
            Glide.with(holder.itemView.context)
                .load(Uri.parse(uri))
                .centerCrop()
                .into(holder.ivThumb)
            holder.ivThumb.setOnClickListener { onSelect(uri) }
            holder.btnQuitar.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onRemove(pos)
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
