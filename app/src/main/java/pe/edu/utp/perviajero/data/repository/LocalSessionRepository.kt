package pe.edu.utp.perviajero.data.repository

import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.EmpresaInfo
import pe.edu.utp.perviajero.data.model.RolUsuario
import pe.edu.utp.perviajero.data.model.Usuario
import pe.edu.utp.perviajero.services.UsuarioService

class LocalSessionRepository(
    private val prefs: PreferencesHelper,
    private val usuarioService: UsuarioService
) : SessionRepository {
    override fun authenticate(email: String, password: String): Usuario? =
        usuarioService.authenticate(email, password)

    override fun registerUser(nombre: String, email: String, password: String): Usuario =
        usuarioService.registerUser(nombre, email, password)

    override fun getQuickAccessUser(rol: RolUsuario): Usuario =
        usuarioService.getQuickAccessUser(rol)

    override fun login(usuario: Usuario) = prefs.login(usuario)
    override fun logout() = prefs.logout()
    override fun isLoggedIn(): Boolean = prefs.isLoggedIn()
    override fun getUsuarioActual(): Usuario? = prefs.getUsuarioActual()
    override fun getRolActual(): RolUsuario = prefs.getRolActual()
    override fun cambiarRol(nuevoRol: RolUsuario) = prefs.cambiarRol(nuevoRol)
    override fun getUserFotoUri(): String = prefs.getUserFotoUri()
    override fun setUserFotoUri(uri: String) = prefs.setUserFotoUri(uri)

    override fun getEmpresaInfo(): EmpresaInfo {
        return prefs.getEmpresaInfoOrNull() ?: usuarioService.getEmpresaInfoDefault().also {
            prefs.guardarEmpresaInfo(it)
        }
    }

    override fun guardarEmpresaInfo(info: EmpresaInfo) = prefs.guardarEmpresaInfo(info)
    override fun isDarkMode(): Boolean = prefs.isDarkMode()
    override fun setDarkMode(enabled: Boolean) = prefs.setDarkMode(enabled)
    override fun getLastSearch(): String = prefs.getLastSearch()
    override fun saveLastSearch(query: String) = prefs.saveLastSearch(query)
    override fun isFirstRun(): Boolean = prefs.isFirstRun()
    override fun setFirstRunCompleted() = prefs.setFirstRunCompleted()
    override fun resetFirstRun() = prefs.resetFirstRun()
    override fun saveLastLocation(lat: Double, lng: Double) = prefs.saveLastLocation(lat, lng)
    override fun getLastLocation(): Pair<Double, Double>? = prefs.getLastLocation()
    override fun saveFcmToken(token: String) = prefs.saveFcmToken(token)
    override fun getFcmToken(): String = prefs.getFcmToken()
}
