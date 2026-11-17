package pe.edu.utp.perviajero.services

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.Reserva

/**
 * Servicio de Reservas alimentado inicialmente desde assets/services/reservas.json.
 */
class ReservaService(
    private val context: Context,
    private val prefs: PreferencesHelper
) {
    private val gson = Gson()

    companion object {
        private const val ASSET_RESERVAS = "services/reservas.json"
        private const val KEY_RESERVAS = "key_reservas"
    }

    fun getReservas(): List<Reserva> {
        val json = prefs.getRawString(KEY_RESERVAS)
        if (!json.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<Reserva>>() {}.type
                val list: List<Reserva>? = gson.fromJson(json, type)
                if (list != null) return list
            } catch (_: Exception) { }
        }
        val type = object : TypeToken<List<Reserva>>() {}.type
        val seed = JsonAssetReader.read<List<Reserva>>(context, ASSET_RESERVAS, type).orEmpty()
        guardarLista(seed)
        return seed
    }

    fun guardarReserva(reserva: Reserva) {
        val lista = getReservas().toMutableList()
        lista.add(0, reserva)
        guardarLista(lista)
    }

    fun actualizarEstadoReserva(codigo: String, nuevoEstado: String): Boolean {
        val lista = getReservas().toMutableList()
        val index = lista.indexOfFirst { it.codigoReserva == codigo }
        if (index == -1) return false
        lista[index].estado = nuevoEstado
        guardarLista(lista)
        return true
    }

    private fun guardarLista(lista: List<Reserva>) {
        prefs.putRawString(KEY_RESERVAS, gson.toJson(lista))
    }
}
