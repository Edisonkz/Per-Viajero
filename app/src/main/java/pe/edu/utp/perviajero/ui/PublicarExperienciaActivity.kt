package pe.edu.utp.perviajero.ui

import android.Manifest
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla para publicar experiencias en la Comunidad de Viajeros usando la Cámara real del dispositivo.
 */
class PublicarExperienciaActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRESELECTED_REGION = "extra_preselected_region"
        const val EXTRA_PRESELECTED_DESTINO_ID = "extra_preselected_destino_id"
        private const val TAG = "PublicarExperiencia"
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var spRegion: Spinner
    private lateinit var spDestino: Spinner
    private lateinit var ivPreviewFoto: ImageView
    private lateinit var btnTomarFoto: MaterialButton
    private lateinit var btnElegirGaleria: MaterialButton
    private lateinit var rbCalificacion: RatingBar
    private lateinit var etComentario: TextInputEditText
    private lateinit var btnPublicar: MaterialButton

    private var todosDestinos: List<Destino> = emptyList()
    private var destinosFiltrados: List<Destino> = emptyList()
    private var regionesDisponibles: List<String> = emptyList()

    private var pendingCameraContentUri: Uri? = null
    private var pendingCameraFileUriString: String = ""
    private var selectedPhotoUriString: String = ""
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
            selectedPhotoUriString = pendingCameraFileUriString
            mostrarVistaPrevia(selectedPhotoUriString)
            AppLogger.i(TAG, "Foto capturada con cámara: $selectedPhotoUriString")
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val savedPath = CameraFileHelper.copyUriToInternalStorage(this, uri, "pub_gal")
            if (savedPath != null) {
                selectedPhotoUriString = savedPath
                mostrarVistaPrevia(selectedPhotoUriString)
            } else {
                Toast.makeText(this, "No se pudo importar la imagen", Toast.LENGTH_SHORT).show()
            }
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
    }

    private fun inicializarVistas() {
        toolbar = findViewById(R.id.toolbarPublicar)
        pe.edu.utp.perviajero.util.WindowInsetsHelper.applyToolbarActivityInsets(this, toolbar)
        spRegion = findViewById(R.id.spPubRegion)
        spDestino = findViewById(R.id.spPubDestino)
        ivPreviewFoto = findViewById(R.id.ivPreviewFotoPub)
        btnTomarFoto = findViewById(R.id.btnTomarFotoCamara)
        btnElegirGaleria = findViewById(R.id.btnElegirGaleria)
        rbCalificacion = findViewById(R.id.rbPubCalificacion)
        etComentario = findViewById(R.id.etPubComentario)
        btnPublicar = findViewById(R.id.btnGuardarPublicacion)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
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
            solicitarPermisoYTomarFoto()
        }

        btnElegirGaleria.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        btnPublicar.setOnClickListener {
            publicarExperiencia()
        }
    }

    private fun solicitarPermisoYTomarFoto() {
        when {
            PermissionHelper.hasCameraPermission(this) -> lanzarCamara()
            PermissionHelper.shouldShowRationale(this, Manifest.permission.CAMERA) -> {
                PermissionHelper.showRationaleDialog(
                    this,
                    "Acceso a la Cámara",
                    "PerúViajero utiliza la cámara únicamente para capturar la fotografía de tu visita turística."
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

        // Si el turista no tomó foto ni eligió de galería, usar foto oficial del destino como respaldo visual
        val fotoFinal = selectedPhotoUriString.ifEmpty {
            destinoObj?.imageUrl ?: "https://images.unsplash.com/photo-1526392060635-9d6019884377?w=800"
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
            fotoUri = fotoFinal,
            calificacion = rbCalificacion.rating,
            comentario = comentario,
            fechaPublicacion = fecha
        )

        ServiceLocator.getComunidadRepository(this).agregarPublicacion(nueva)
        AppLogger.i(TAG, "Nueva experiencia publicada en $region ($destinoNombre)")
        Toast.makeText(this, "¡Experiencia publicada en Comunidad!", Toast.LENGTH_SHORT).show()
        setResult(Activity.RESULT_OK)
        finish()
    }
}
