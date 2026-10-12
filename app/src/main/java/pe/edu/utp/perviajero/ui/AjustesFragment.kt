package pe.edu.utp.perviajero.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import pe.edu.utp.perviajero.R
import pe.edu.utp.perviajero.data.local.PreferencesHelper

// REQ-10: Pantalla de Ajustes con selector de tema visual (Claro/Oscuro) persistido
class AjustesFragment : Fragment() {

    private lateinit var switchModoOscuro: SwitchMaterial

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ajustes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        switchModoOscuro = view.findViewById(R.id.switchModoOscuro)
        val prefs = PreferencesHelper.getInstance(requireContext())

        // Cargar estado inicial del tema guardado en SharedPreferences
        switchModoOscuro.isChecked = prefs.isDarkMode()

        // Listener de cambio de tema en tiempo real
        switchModoOscuro.setOnCheckedChangeListener { _, isChecked ->
            prefs.setDarkMode(isChecked)
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }
}

