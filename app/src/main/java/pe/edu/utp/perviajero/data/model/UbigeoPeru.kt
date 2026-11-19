package pe.edu.utp.perviajero.data.model

import com.google.gson.annotations.SerializedName

/**
 * Nodo oficial de Ubigeo del Perú (Departamento, Provincia o Distrito)
 * compatible con la API pública joseluisq/ubigeos-peru.
 */
data class UbigeoNode(
    @SerializedName("id_ubigeo")
    val id: String = "",
    @SerializedName("nombre_ubigeo")
    val nombre: String = "",
    @SerializedName("codigo_ubigeo")
    val codigo: String = "",
    @SerializedName("id_padre_ubigeo")
    val idPadre: String = ""
)

data class UbigeoCatalogo(
    val departamentos: List<UbigeoNode>,
    val provinciasByDepto: Map<String, List<UbigeoNode>>,
    val distritosByProv: Map<String, List<UbigeoNode>>,
    val fuente: String
) {
    fun totalProvincias(): Int = provinciasByDepto.values.sumOf { it.size }
    fun totalDistritos(): Int = distritosByProv.values.sumOf { it.size }
}
