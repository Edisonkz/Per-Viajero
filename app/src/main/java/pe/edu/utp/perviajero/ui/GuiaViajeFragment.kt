package pe.edu.utp.perviajero.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import pe.edu.utp.perviajero.R

// Pantalla informativa de Guía del Viajero, Brújula Offline, Comunidad e Intent a IPERÚ
class GuiaViajeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_guia_viaje, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBrujula: MaterialButton = view.findViewById(R.id.btnAbrirBrujulaOffline)
        val btnComunidad: MaterialButton = view.findViewById(R.id.btnGuiaAbrirComunidad)
        val btnAbrirItinerario: MaterialButton = view.findViewById(R.id.btnAbrirItinerario)
        val btnLlamarIperu: MaterialButton = view.findViewById(R.id.btnLlamarIperu)

        btnBrujula.setOnClickListener {
            startActivity(Intent(requireContext(), BrujulaActivity::class.java))
        }

        btnComunidad.setOnClickListener {
            startActivity(Intent(requireContext(), ComunidadActivity::class.java))
        }

        btnAbrirItinerario.setOnClickListener {
            startActivity(Intent(requireContext(), ItinerarioActivity::class.java))
        }

        btnLlamarIperu.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:015748000")
            }
            startActivity(intent)
        }
    }
}
