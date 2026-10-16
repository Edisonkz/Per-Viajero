package pe.edu.utp.perviajero.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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

// REQ-05 / REQ-07 / REQ-08: Pantalla de exploración con consumo de API REST, 3 estados y buscador
class DestinosFragment : Fragment() {

    private lateinit var etBuscar: EditText
    private lateinit var rvDestinos: RecyclerView
    private lateinit var layoutCargando: View
    private lateinit var layoutError: View
    private lateinit var layoutVacio: View
    private lateinit var btnReintentar: Button

    private lateinit var destinoAdapter: DestinoAdapter
    private var listaDestinos: List<Destino> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_destinos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicialización de componentes visuales
        etBuscar = view.findViewById(R.id.etBuscar)
        rvDestinos = view.findViewById(R.id.rvDestinos)
        layoutCargando = view.findViewById(R.id.layoutCargando)
        layoutError = view.findViewById(R.id.layoutError)
        layoutVacio = view.findViewById(R.id.layoutVacio)
        btnReintentar = view.findViewById(R.id.btnReintentar)

        // Configuración de RecyclerView
        rvDestinos.layoutManager = LinearLayoutManager(requireContext())
        destinoAdapter = DestinoAdapter(
            listaOriginal = emptyList(),
            onItemClick = { destino ->
                abrirDetalle(destino)
            }
        )
        rvDestinos.adapter = destinoAdapter

        // Botón para reintentar la llamada HTTP en caso de error
        btnReintentar.setOnClickListener {
            cargarDestinosDesdeApi()
        }

        // REQ-08: Buscador reactivo (estado dinámico en pantalla sin recrear la Activity)
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                PreferencesHelper.getInstance(requireContext()).saveLastSearch(query)
                val countResults = destinoAdapter.filter(query)

                // Si no hay resultados del filtro, mostrar vista vacía
                if (countResults == 0 && query.isNotEmpty()) {
                    layoutVacio.visibility = View.VISIBLE
                    rvDestinos.visibility = View.GONE
                } else {
                    layoutVacio.visibility = View.GONE
                    rvDestinos.visibility = View.VISIBLE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Iniciar carga de datos desde la API
        cargarDestinosDesdeApi()
    }

    override fun onResume() {
        super.onResume()
        // Refrescar favoritos en caso de cambios en otras pantallas
        destinoAdapter.notifyDataSetChanged()
    }

    // REQ-05 / REQ-07: Llamada asíncrona mediante Retrofit y manejo visible de 3 estados
    private fun cargarDestinosDesdeApi() {
        mostrarEstado(EstadoUi.CARGANDO) // 1. Estado Cargando (ProgressBar visible)
        ApiClient.service.getDestinos().enqueue(object : Callback<List<Destino>> {
            override fun onResponse(call: Call<List<Destino>>, response: Response<List<Destino>>) {
                if (response.isSuccessful && response.body() != null) {
                    val destinos = response.body()!!
                    PreferencesHelper.getInstance(requireContext()).guardarDestinosEnCache(destinos)
                    destinoAdapter.updateData(destinos)
                    mostrarEstado(EstadoUi.EXITO) // 2. Estado Éxito (RecyclerView visible)
                } else {
                    manejarFalloDeConexion()
                }
            }
            override fun onFailure(call: Call<List<Destino>>, t: Throwable) {
                manejarFalloDeConexion() // 3. Estado Error / Offline
            }
        })
    }

    // Si se corta el internet, intenta recuperar la copia guardada en memoria local
    private fun manejarFalloDeConexion() {
        val destinosEnMemoria = PreferencesHelper.getInstance(requireContext()).obtenerDestinosEnCache()
        if (destinosEnMemoria.isNotEmpty()) {
            listaDestinos = destinosEnMemoria
            destinoAdapter.updateData(destinosEnMemoria)
            mostrarEstado(EstadoUi.EXITO)
            android.widget.Toast.makeText(
                requireContext(),
                "Modo Offline: Mostrando catálogo guardado en memoria local.",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } else {
            mostrarEstado(EstadoUi.ERROR)
        }
    }

    // REQ-07: Control explícito de los 3 estados de interfaz
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

    // REQ-03: Navegación a pantalla de detalle con Intent explícito y Bundle
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
