package pe.edu.utp.perviajero.data.model

import java.io.Serializable

// Datos corporativos y fiscales de la agencia de turismo (editables por el Administrador)
data class EmpresaInfo(
    val razonSocial: String = "PerúViajero Tours & Travel S.A.C.",
    val ruc: String = "20608945123",
    val direccion: String = "Av. El Sol 845, Wanchaq, Cusco - Perú",
    val telefono: String = "+51 84 254890 / +51 984 123 456",
    val email: String = "operaciones@peruviajero.pe",
    val sitioWeb: String = "www.peruviajero.pe",
    val registroMincetur: String = "DIRCETUR-CUS-2026-0894"
) : Serializable
