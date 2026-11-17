package pe.edu.utp.perviajero.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.adapter.DestinoAdapter
import pe.edu.utp.perviajero.data.common.AppResult
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.di.ServiceLocator

// Pantalla de Favoritos persistidos mediante FavoritoRepository con respaldo offline automático
class FavoritosFragment : Fragment() {

    private lateinit var rvFavoritos: RecyclerView
    private lateinit var layoutFavoritosVacio: View
    private lateinit var destinoAdapter: DestinoAdapter
    private var listaCompleta: List<Destino> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favoritos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvFavoritos = view.findViewById(R.id.rvFavoritos)
        layoutFavoritosVacio = view.findViewById(R.id.layoutFavoritosVacio)

        rvFavoritos.layoutManager = LinearLayoutManager(requireContext())
        destinoAdapter = DestinoAdapter(
            onItemClick = { destino -> abrirDetalle(destino) },
            onFavoritoClick = { _, _ -> cargarFavoritos() }
        )
        rvFavoritos.adapter = destinoAdapter

        cargarDatos()
    }

    override fun onResume() {
        super.onResume()
        cargarFavoritos()
    }

    private fun cargarDatos() {
        val destinoRepo = ServiceLocator.getDestinoRepository(requireContext())
        // Carga inmediata desde caché para respuesta instantánea
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
        val listaFiltrada = listaCompleta.filter { idsFavoritos.contains(it.id) }

        if (listaFiltrada.isEmpty()) {
            layoutFavoritosVacio.visibility = View.VISIBLE
            rvFavoritos.visibility = View.GONE
        } else {
            layoutFavoritosVacio.visibility = View.GONE
            rvFavoritos.visibility = View.VISIBLE
            destinoAdapter.updateData(listaFiltrada)
        }
    }

    private fun abrirDetalle(destino: Destino) {
        val intent = Intent(requireContext(), DetalleDestinoActivity::class.java).apply {
            putExtra(DetalleDestinoActivity.EXTRA_DESTINO, destino)
        }
        startActivity(intent)
    }
}
