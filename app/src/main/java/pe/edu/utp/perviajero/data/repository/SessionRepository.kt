package pe.edu.utp.perviajero.data.repository

import pe.edu.utp.perviajero.data.model.EmpresaInfo
import pe.edu.utp.perviajero.data.model.RolUsuario
import pe.edu.utp.perviajero.data.model.Usuario

/**
 * Contrato para sesión de usuario, preferencias visuales, datos de empresa y telemetría local.
 */
interface SessionRepository {
    fun login(usuario: Usuario)
    fun logout()
    fun isLoggedIn(): Boolean
    fun getUsuarioActual(): Usuario?
    fun getRolActual(): RolUsuario
    fun cambiarRol(nuevoRol: RolUsuario)

    fun getUserFotoUri(): String
    fun setUserFotoUri(uri: String)

    fun getEmpresaInfo(): EmpresaInfo
    fun guardarEmpresaInfo(info: EmpresaInfo)

    fun isDarkMode(): Boolean
    fun setDarkMode(enabled: Boolean)

    fun getLastSearch(): String
    fun saveLastSearch(query: String)

    fun isFirstRun(): Boolean
    fun setFirstRunCompleted()
    fun resetFirstRun()

    fun saveLastLocation(lat: Double, lng: Double)
    fun getLastLocation(): Pair<Double, Double>?

    fun saveFcmToken(token: String)
    fun getFcmToken(): String
}
