package pe.edu.utp.perviajero.network

import pe.edu.utp.perviajero.data.model.UbigeoNode
import retrofit2.http.GET

/**
 * API REST pública oficial de Ubigeos del Perú (25 Departamentos, 196 Provincias y 1,874 Distritos).
 * Fuente: https://raw.githubusercontent.com/joseluisq/ubigeos-peru/master/json/
 */
interface UbigeoApiService {

    @GET("departamentos.json")
    suspend fun getDepartamentos(): List<UbigeoNode>

    @GET("provincias.json")
    suspend fun getProvincias(): Map<String, List<UbigeoNode>>

    @GET("distritos.json")
    suspend fun getDistritos(): Map<String, List<UbigeoNode>>
}
