package pe.edu.utp.perviajero.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import pe.edu.utp.perviajero.MainActivity
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.adapter.DestinoAdapter
import pe.edu.utp.perviajero.data.common.AppResult
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.di.ServiceLocator
import pe.edu.utp.perviajero.ui.common.ChipStyleHelper
import pe.edu.utp.perviajero.ui.common.VoucherDialogHelper
import pe.edu.utp.perviajero.util.LocationHelper
import pe.edu.utp.perviajero.util.RegionPeruHelper

/**
 * Pantalla de Favoritos persistidos con disponibilidad offline, filtro por región natural
 * (Sierra, Costa, Selva), distancia GPS en vivo y accesos rápidos a Ruta GPS y Reservar.
 */
class FavoritosFragment : Fragment() {

    private lateinit var tvResumenFavoritos: TextView
    private lateinit var chipGroupRegion: ChipGroup
    private lateinit var rvFavoritos: RecyclerView
    private lateinit var layoutFavoritosVacio: View
    private lateinit var tvVacioTitulo: TextView
    private lateinit var tvVacioSubtitulo: TextView
    private lateinit var btnExplorar: MaterialButton

    private lateinit var destinoAdapter: DestinoAdapter
    private var listaCompleta: List<Destino> = emptyList()
    private var regionSeleccionada: String = "Todas"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favoritos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvResumenFavoritos = view.findViewById(R.id.tvResumenFavoritos)
        chipGroupRegion = view.findViewById(R.id.chipGroupRegionFavoritos)
        rvFavoritos = view.findViewById(R.id.rvFavoritos)
        layoutFavoritosVacio = view.findViewById(R.id.layoutFavoritosVacio)
        tvVacioTitulo = view.findViewById(R.id.tvFavoritosVacioTitulo)
        tvVacioSubtitulo = view.findViewById(R.id.tvFavoritosVacioSubtitulo)
        btnExplorar = view.findViewById(R.id.btnExplorarDesdeFavoritos)

        rvFavoritos.layoutManager = LinearLayoutManager(requireContext())
        destinoAdapter = DestinoAdapter(
            showQuickActions = true,
            onItemClick = { destino -> abrirDetalle(destino) },
            onFavoritoClick = { _, _ -> cargarFavoritos() },
            onRutaGpsClick = { destino -> abrirMapa(destino) },
            onReservarClick = { destino -> VoucherDialogHelper.openCotizadorConValidacion(requireContext(), destino) }
        )
        rvFavoritos.adapter = destinoAdapter

        btnExplorar.setOnClickListener {
            (activity as? MainActivity)?.seleccionarTab(MainActivity.MainTab.EXPLORAR)
        }

        configurarChipsRegion()
        cargarDistanciaGpsSiDisponible()
        cargarDatos()
    }

    override fun onResume() {
        super.onResume()
        cargarFavoritos()
    }

    private fun configurarChipsRegion() {
        chipGroupRegion.removeAllViews()
        for (region in RegionPeruHelper.REGIONES_PRINCIPALES) {
            val chip = ChipStyleHelper.createFilterChip(
                context = requireContext(),
                label = region,
                isCheckedInitial = region == regionSeleccionada,
                id = View.generateViewId()
            )
            chipGroupRegion.addView(chip)
        }

        chipGroupRegion.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            for (i in 0 until group.childCount) {
                val c = group.getChildAt(i) as? Chip ?: continue
                ChipStyleHelper.applyStyle(requireContext(), c, c.id == checkedId)
            }
            val selected = group.findViewById<Chip>(checkedId) ?: return@setOnCheckedStateChangeListener
            regionSeleccionada = selected.text.toString()
            cargarFavoritos()
        }
    }

    private fun cargarDistanciaGpsSiDisponible() {
        ServiceLocator.getSessionRepository(requireContext()).getLastLocation()?.let {
            destinoAdapter.setUserOrigin(it)
        }
        LocationHelper(requireContext()).obtainCurrentLocation { loc ->
            if (!isAdded || loc == null) return@obtainCurrentLocation
            requireActivity().runOnUiThread {
                destinoAdapter.setUserOrigin(loc.latitude to loc.longitude)
            }
        }
    }

    private fun cargarDatos() {
        val destinoRepo = ServiceLocator.getDestinoRepository(requireContext())
        val cachedInitial = destinoRepo.obtenerDestinosEnCache()
        if (cachedInitial.isNotEmpty()) {
            listaCompleta = cachedInitial
            cargarFavoritos()
        }

        destinoRepo.obtenerDestinos { result ->
            if (!isAdded) return@obtenerDestinos
            when (result) {
                is AppResult.Success -> {
                    listaCompleta = result.data
                    cargarFavoritos()
                }
                is AppResult.Error -> {
                    if (listaCompleta.isEmpty() && !result.cachedData.isNullOrEmpty()) {
                        listaCompleta = result.cachedData
                    }
                    cargarFavoritos()
                }
            }
        }
    }

    private fun cargarFavoritos() {
        val idsFavoritos = ServiceLocator.getFavoritoRepository(requireContext()).getFavoritosIds()
        val favoritosTotales = listaCompleta.filter { idsFavoritos.contains(it.id) }
        val favoritosFiltrados = RegionPeruHelper.filterByNaturalRegion(favoritosTotales, regionSeleccionada)

        val suffix = if (favoritosTotales.size == 1) "destino guardado" else "destinos guardados"
        tvResumenFavoritos.text = "${favoritosTotales.size} $suffix • Disponible sin conexión"

        if (favoritosFiltrados.isEmpty()) {
            layoutFavoritosVacio.visibility = View.VISIBLE
            rvFavoritos.visibility = View.GONE
            if (favoritosTotales.isEmpty()) {
                tvVacioTitulo.text = "Aún no tienes destinos favoritos"
                tvVacioSubtitulo.text = getString(R.string.sin_favoritos)
                btnExplorar.visibility = View.VISIBLE
            } else {
                tvVacioTitulo.text = "Sin favoritos en región $regionSeleccionada"
                tvVacioSubtitulo.text = "Cambia el filtro superior o explora nuevos atractivos en esta región."
                btnExplorar.visibility = View.GONE
            }
        } else {
            layoutFavoritosVacio.visibility = View.GONE
            rvFavoritos.visibility = View.VISIBLE
            destinoAdapter.updateData(favoritosFiltrados)
        }
    }

    private fun abrirDetalle(destino: Destino) {
        startActivity(Intent(requireContext(), DetalleDestinoActivity::class.java).apply {
            putExtra(DetalleDestinoActivity.EXTRA_DESTINO, destino)
        })
    }

    private fun abrirMapa(destino: Destino) {
        startActivity(Intent(requireContext(), MapaRutaActivity::class.java).apply {
            putExtra(MapaRutaActivity.EXTRA_DESTINO, destino)
        })
    }
}
