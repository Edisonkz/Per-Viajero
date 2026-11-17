package pe.edu.utp.perviajero.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pe.edu.utp.perviajero.MainActivity
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.adapter.DestinoAdapter
import pe.edu.utp.perviajero.data.common.AppResult
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.ui.common.ChipStyleHelper
import pe.edu.utp.perviajero.util.LocationHelper
import pe.edu.utp.perviajero.util.RegionPeruHelper

// Pantalla Explorar: Buscador, 4 Accesos Rápidos, Categorías, Departamento/Provincia y distancia GPS
class DestinosFragment : Fragment() {

    companion object {
        private val CATEGORIAS = listOf("Todos", "Arqueología", "Naturaleza", "Aventura", "Cultura", "Playa")
    }

    private lateinit var etBuscar: EditText
    private lateinit var btnLimpiarBusqueda: ImageButton
    private lateinit var qaMapaGps: View
    private lateinit var qaBrujulaOffline: View
    private lateinit var qaPublicarFoto: View
    private lateinit var qaGuiaIperu: View
    private lateinit var chipGroupCategorias: ChipGroup
    private lateinit var cardFiltroDepartamento: MaterialCardView
    private lateinit var tvFiltroDepartamento: TextView
    private lateinit var ivFiltroDepartamento: ImageView
    private lateinit var cardFiltroProvincia: MaterialCardView
    private lateinit var tvFiltroProvincia: TextView
    private lateinit var ivFiltroProvincia: ImageView
    private lateinit var tvOfflineBanner: TextView
    private lateinit var rvDestinos: RecyclerView
    private lateinit var layoutCargando: View
    private lateinit var layoutError: View
    private lateinit var tvErrorMensaje: TextView
    private lateinit var layoutVacio: View
    private lateinit var btnReintentar: Button

    private lateinit var destinoAdapter: DestinoAdapter
    private var selectedCategory: String = "Todos"
    private var selectedDepartment: String = "Todos"
    private var selectedProvince: String = "Todas"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_destinos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etBuscar = view.findViewById(R.id.etBuscar)
        btnLimpiarBusqueda = view.findViewById(R.id.btnLimpiarBusqueda)
        qaMapaGps = view.findViewById(R.id.qaMapaGps)
        qaBrujulaOffline = view.findViewById(R.id.qaBrujulaOffline)
        qaPublicarFoto = view.findViewById(R.id.qaPublicarFoto)
        qaGuiaIperu = view.findViewById(R.id.qaGuiaIperu)
        chipGroupCategorias = view.findViewById(R.id.chipGroupCategorias)
        cardFiltroDepartamento = view.findViewById(R.id.cardFiltroDepartamento)
        tvFiltroDepartamento = view.findViewById(R.id.tvFiltroDepartamento)
        ivFiltroDepartamento = view.findViewById(R.id.ivFiltroDepartamento)
        cardFiltroProvincia = view.findViewById(R.id.cardFiltroProvincia)
        tvFiltroProvincia = view.findViewById(R.id.tvFiltroProvincia)
        ivFiltroProvincia = view.findViewById(R.id.ivFiltroProvincia)
        tvOfflineBanner = view.findViewById(R.id.tvOfflineBanner)
        rvDestinos = view.findViewById(R.id.rvDestinos)
        layoutCargando = view.findViewById(R.id.layoutCargando)
        layoutError = view.findViewById(R.id.layoutError)
        tvErrorMensaje = view.findViewById(R.id.tvErrorMensaje)
        layoutVacio = view.findViewById(R.id.layoutVacio)
        btnReintentar = view.findViewById(R.id.btnReintentar)

        rvDestinos.layoutManager = LinearLayoutManager(requireContext())
        destinoAdapter = DestinoAdapter(
            onItemClick = { destino -> abrirDetalle(destino) }
        )
        rvDestinos.adapter = destinoAdapter

        configurarAccesosRapidos()
        configurarChipsCategorias()
        configurarFiltrosUbicacion()
        configurarBuscador()
        cargarDistanciaGpsSiDisponible()
        cargarDestinosDesdeApi()
    }

    override fun onResume() {
        super.onResume()
        destinoAdapter.notifyDataSetChanged()
    }

    private fun configurarAccesosRapidos() {
        qaMapaGps.setOnClickListener {
            (activity as? MainActivity)?.abrirMapaDestinoPrincipal()
        }
        qaBrujulaOffline.setOnClickListener {
            startActivity(Intent(requireContext(), BrujulaActivity::class.java))
        }
        qaPublicarFoto.setOnClickListener {
            startActivity(Intent(requireContext(), PublicarExperienciaActivity::class.java))
        }
        qaGuiaIperu.setOnClickListener {
            startActivity(Intent(requireContext(), GuiaViajeActivity::class.java))
        }
    }

    private fun configurarChipsCategorias() {
        chipGroupCategorias.removeAllViews()
        for (cat in CATEGORIAS) {
            val chip = ChipStyleHelper.createFilterChip(
                context = requireContext(),
                label = cat,
                isCheckedInitial = cat == selectedCategory,
                id = View.generateViewId()
            )
            chipGroupCategorias.addView(chip)
        }

        chipGroupCategorias.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            for (i in 0 until group.childCount) {
                val c = group.getChildAt(i) as? Chip ?: continue
                ChipStyleHelper.applyStyle(requireContext(), c, c.id == checkedId)
            }
            val chip = group.findViewById<Chip>(checkedId) ?: return@setOnCheckedStateChangeListener
            selectedCategory = chip.text.toString()
            aplicarFiltroYVisibilidad(etBuscar.text?.toString().orEmpty())
        }
    }

    private fun configurarFiltrosUbicacion() {
        actualizarEstilosFiltrosUbicacion()

        cardFiltroDepartamento.setOnClickListener {
            val catalog = destinoAdapter.getListaBase()
            val opciones = listOf("Todos") + RegionPeruHelper.extractDepartamentos(catalog)
            val checkedIndex = opciones.indexOfFirst { it.equals(selectedDepartment, ignoreCase = true) }.coerceAtLeast(0)

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Filtrar por Departamento")
                .setSingleChoiceItems(opciones.toTypedArray(), checkedIndex) { dialog, which ->
                    selectedDepartment = opciones[which]
                    val provinciasDisponibles = RegionPeruHelper.extractProvincias(catalog, selectedDepartment)
                    if (!selectedProvince.equals("Todas", ignoreCase = true) &&
                        provinciasDisponibles.none { it.equals(selectedProvince, ignoreCase = true) }
                    ) {
                        selectedProvince = "Todas"
                    }
                    actualizarEstilosFiltrosUbicacion()
                    aplicarFiltroYVisibilidad(etBuscar.text?.toString().orEmpty())
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        cardFiltroProvincia.setOnClickListener {
            val catalog = destinoAdapter.getListaBase()
            val opciones = listOf("Todas") + RegionPeruHelper.extractProvincias(catalog, selectedDepartment)
            val checkedIndex = opciones.indexOfFirst { it.equals(selectedProvince, ignoreCase = true) }.coerceAtLeast(0)

            val titulo = if (selectedDepartment.equals("Todos", ignoreCase = true)) {
                "Filtrar por Provincia"
            } else {
                "Provincias de $selectedDepartment"
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(titulo)
                .setSingleChoiceItems(opciones.toTypedArray(), checkedIndex) { dialog, which ->
                    selectedProvince = opciones[which]
                    actualizarEstilosFiltrosUbicacion()
                    aplicarFiltroYVisibilidad(etBuscar.text?.toString().orEmpty())
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun actualizarEstilosFiltrosUbicacion() {
        val deptoActivo = !selectedDepartment.equals("Todos", ignoreCase = true)
        val provActiva = !selectedProvince.equals("Todas", ignoreCase = true)

        tvFiltroDepartamento.text = if (deptoActivo) "Depto: $selectedDepartment ▾" else "Depto: Todos ▾"
        tvFiltroProvincia.text = if (provActiva) "Prov: $selectedProvince ▾" else "Prov: Todas ▾"

        aplicarEstiloPillUbicacion(cardFiltroDepartamento, tvFiltroDepartamento, ivFiltroDepartamento, deptoActivo)
        aplicarEstiloPillUbicacion(cardFiltroProvincia, tvFiltroProvincia, ivFiltroProvincia, provActiva)
    }

    private fun aplicarEstiloPillUbicacion(
        card: MaterialCardView,
        label: TextView,
        icon: ImageView,
        activo: Boolean
    ) {
        val primary = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        val cardBg = ContextCompat.getColor(requireContext(), R.color.colorCardBackground)
        val textPrimary = ContextCompat.getColor(requireContext(), R.color.colorTextPrimary)

        if (activo) {
            card.setCardBackgroundColor(primary)
            card.strokeWidth = 0
            label.setTextColor(Color.WHITE)
            icon.setColorFilter(Color.WHITE)
        } else {
            card.setCardBackgroundColor(cardBg)
            card.strokeColor = Color.argb(35, 0, 0, 0)
            card.strokeWidth = 2
            label.setTextColor(textPrimary)
            icon.setColorFilter(primary)
        }
    }

    private fun configurarBuscador() {
        val sessionRepo = ServiceLocator.getSessionRepository(requireContext())
        val ultimaBusqueda = sessionRepo.getLastSearch()
        if (ultimaBusqueda.isNotEmpty()) {
            etBuscar.setText(ultimaBusqueda)
            btnLimpiarBusqueda.visibility = View.VISIBLE
        }

        btnLimpiarBusqueda.setOnClickListener {
            etBuscar.setText("")
        }

        btnReintentar.setOnClickListener {
            cargarDestinosDesdeApi()
        }

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                btnLimpiarBusqueda.visibility = if (query.isEmpty()) View.GONE else View.VISIBLE
                sessionRepo.saveLastSearch(query)
                aplicarFiltroYVisibilidad(query)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun cargarDistanciaGpsSiDisponible() {
        val sessionRepo = ServiceLocator.getSessionRepository(requireContext())
        sessionRepo.getLastLocation()?.let { destinoAdapter.setUserOrigin(it) }

        LocationHelper(requireContext()).obtainCurrentLocation { loc ->
            if (!isAdded || loc == null) return@obtainCurrentLocation
            requireActivity().runOnUiThread {
                destinoAdapter.setUserOrigin(loc.latitude to loc.longitude)
            }
        }
    }

    private fun cargarDestinosDesdeApi() {
        mostrarEstado(EstadoUi.CARGANDO)
        tvOfflineBanner.visibility = View.GONE

        ServiceLocator.getDestinoRepository(requireContext()).obtenerDestinos { result ->
            if (!isAdded) return@obtenerDestinos

            when (result) {
                is AppResult.Success -> {
                    tvOfflineBanner.visibility = View.GONE
                    destinoAdapter.updateData(result.data)
                    mostrarEstado(EstadoUi.EXITO)
                    aplicarFiltroYVisibilidad(etBuscar.text?.toString().orEmpty())
                }
                is AppResult.Error -> {
                    val cached = result.cachedData
                    if (!cached.isNullOrEmpty()) {
                        tvOfflineBanner.visibility = View.VISIBLE
                        destinoAdapter.updateData(cached)
                        mostrarEstado(EstadoUi.EXITO)
                        aplicarFiltroYVisibilidad(etBuscar.text?.toString().orEmpty())
                    } else {
                        tvErrorMensaje.text = result.message
                        mostrarEstado(EstadoUi.ERROR)
                    }
                }
            }
        }
    }

    private fun aplicarFiltroYVisibilidad(query: String) {
        if (layoutCargando.visibility == View.VISIBLE || layoutError.visibility == View.VISIBLE) return
        val countResults = destinoAdapter.filter(query, selectedCategory, selectedDepartment, selectedProvince)
        if (countResults == 0) {
            layoutVacio.visibility = View.VISIBLE
            rvDestinos.visibility = View.GONE
        } else {
            layoutVacio.visibility = View.GONE
            rvDestinos.visibility = View.VISIBLE
        }
    }

    private fun mostrarEstado(estado: EstadoUi) {
        when (estado) {
            EstadoUi.CARGANDO -> {
                layoutCargando.visibility = View.VISIBLE
                rvDestinos.visibility = View.GONE
                layoutError.visibility = View.GONE
                layoutVacio.visibility = View.GONE
            }
            EstadoUi.EXITO -> {
                layoutCargando.visibility = View.GONE
                rvDestinos.visibility = View.VISIBLE
                layoutError.visibility = View.GONE
                layoutVacio.visibility = View.GONE
            }
            EstadoUi.ERROR -> {
                layoutCargando.visibility = View.GONE
                rvDestinos.visibility = View.GONE
                layoutError.visibility = View.VISIBLE
                layoutVacio.visibility = View.GONE
            }
        }
    }

    private fun abrirDetalle(destino: Destino) {
        val intent = Intent(requireContext(), DetalleDestinoActivity::class.java).apply {
            putExtra(DetalleDestinoActivity.EXTRA_DESTINO, destino)
        }
        startActivity(intent)
    }

    private enum class EstadoUi {
        CARGANDO,
        EXITO,
        ERROR
    }
}
