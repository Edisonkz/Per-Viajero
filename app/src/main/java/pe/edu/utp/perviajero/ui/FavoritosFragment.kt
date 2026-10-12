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
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// REQ-09: Pantalla de Favoritos persistidos en SharedPreferences
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
            listaOriginal = emptyList(),
            onItemClick = { destino ->
                abrirDetalle(destino)
            },
            onFavoritoClick = { _, _ ->
                cargarFavoritos()
            }
        )
        rvFavoritos.adapter = destinoAdapter

        cargarDatos()
    }

    override fun onResume() {
        super.onResume()
        cargarFavoritos()
    }

    private fun cargarDatos() {
        ApiClient.service.getDestinos().enqueue(object : Callback<List<Destino>> {
            override fun onResponse(call: Call<List<Destino>>, response: Response<List<Destino>>) {
                if (response.isSuccessful && response.body() != null) {
                    listaCompleta = response.body()!!
                    cargarFavoritos()
                }
            }
            override fun onFailure(call: Call<List<Destino>>, t: Throwable) {
                cargarFavoritos()
            }
        })
    }

    // Filtrar la lista según los IDs guardados en SharedPreferences
    private fun cargarFavoritos() {
        val prefs = PreferencesHelper.getInstance(requireContext())
        val idsFavoritos = prefs.getFavoritosIds()
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

