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
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.adapter.DestinoAdapter
import pe.edu.utp.perviajero.data.common.AppResult
import pe.edu.utp.perviajero.data.model.Destino
import pe.edu.utp.perviajero.di.ServiceLocator

// Pantalla de exploración con repositorio REST/caché, 3 estados, acceso a Comunidad y buscador reactivo
class DestinosFragment : Fragment() {

    private lateinit var etBuscar: EditText
    private lateinit var cardBannerComunidad: View
    private lateinit var tvOfflineBanner: TextView
    private lateinit var rvDestinos: RecyclerView
    private lateinit var layoutCargando: View
    private lateinit var layoutError: View
    private lateinit var tvErrorMensaje: TextView
    private lateinit var layoutVacio: View
    private lateinit var btnReintentar: Button

    private lateinit var destinoAdapter: DestinoAdapter

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
        cardBannerComunidad = view.findViewById(R.id.cardBannerComunidad)
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

        cardBannerComunidad.setOnClickListener {
            startActivity(Intent(requireContext(), ComunidadActivity::class.java))
        }

        btnReintentar.setOnClickListener {
            cargarDestinosDesdeApi()
        }

        val sessionRepo = ServiceLocator.getSessionRepository(requireContext())
        val ultimaBusqueda = sessionRepo.getLastSearch()
        if (ultimaBusqueda.isNotEmpty()) {
            etBuscar.setText(ultimaBusqueda)
        }

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                sessionRepo.saveLastSearch(query)
                aplicarFiltroYVisibilidad(query)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        cargarDestinosDesdeApi()
    }

    override fun onResume() {
        super.onResume()
        destinoAdapter.notifyDataSetChanged()
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
        val countResults = destinoAdapter.filter(query)
        if (countResults == 0 && query.isNotEmpty()) {
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
