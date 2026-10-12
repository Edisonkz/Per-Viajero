package pe.edu.utp.perviajero.network

import pe.edu.utp.perviajero.data.model.Destino
import retrofit2.Call
import retrofit2.http.GET

// REQ-05: Interfaz del servicio REST para obtener los destinos turísticos
interface TourismApiService {
    @GET("destinos.json")
    fun getDestinos(): Call<List<Destino>>
}

