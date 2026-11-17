package pe.edu.utp.perviajero.services

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pe.edu.utp.perviajero.data.local.PreferencesHelper
import pe.edu.utp.perviajero.data.model.EmpresaInfo
import pe.edu.utp.perviajero.data.model.RolUsuario
import pe.edu.utp.perviajero.data.model.Usuario

data class UsuarioDto(
    val id: String,
    val nombre: String,
    val email: String,
    val password: String,
    val rol: String
) {
    fun toUsuario(): Usuario {
        val rolEnum = try {
            RolUsuario.valueOf(rol)
        } catch (_: Exception) {
            RolUsuario.CLIENTE
        }
        return Usuario(id = id, nombre = nombre, email = email, rol = rolEnum)
    }
}

/**
 * Servicio de Usuarios y Agencia alimentado desde assets/services/usuarios.json y empresa.json.
 * Elimina cualquier dato de usuario hardcodeado en las pantallas del frontend.
 */
class UsuarioService(
    private val context: Context,
    private val prefs: PreferencesHelper
) {
    private val gson = Gson()

    companion object {
        private const val ASSET_USUARIOS = "services/usuarios.json"
        private const val ASSET_EMPRESA = "services/empresa.json"
        private const val KEY_USERS_STORE = "key_service_usuarios"
    }

    fun getAllUsuarios(): List<UsuarioDto> {
        val storedJson = prefs.getRawString(KEY_USERS_STORE)
        if (!storedJson.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<UsuarioDto>>() {}.type
                val list: List<UsuarioDto>? = gson.fromJson(storedJson, type)
                if (!list.isNullOrEmpty()) return list
            } catch (_: Exception) { }
        }
        val type = object : TypeToken<List<UsuarioDto>>() {}.type
        val seed = JsonAssetReader.read<List<UsuarioDto>>(context, ASSET_USUARIOS, type).orEmpty()
        if (seed.isNotEmpty()) {
            prefs.putRawString(KEY_USERS_STORE, gson.toJson(seed))
        }
        return seed
    }

    fun authenticate(email: String, password: String): Usuario? {
        val cleanEmail = email.trim()
        val found = getAllUsuarios().firstOrNull {
            it.email.equals(cleanEmail, ignoreCase = true) && it.password == password
        }
        return found?.toUsuario()
    }

    fun getQuickAccessUser(rol: RolUsuario): Usuario {
        val dto = getAllUsuarios().firstOrNull {
            it.rol.equals(rol.name, ignoreCase = true)
        }
        if (dto != null) return dto.toUsuario()
        return Usuario(
            id = "usr_fallback_${rol.name.lowercase()}",
            nombre = if (rol == RolUsuario.ADMIN) "Administrador" else "Viajero",
            email = if (rol == RolUsuario.ADMIN) "admin@peruviajero.pe" else "viajero@peruviajero.pe",
            rol = rol
        )
    }

    fun registerUser(nombre: String, email: String, password: String): Usuario {
        val current = getAllUsuarios().toMutableList()
        val existing = current.indexOfFirst { it.email.equals(email.trim(), ignoreCase = true) }
        val newDto = UsuarioDto(
            id = "usr_${System.currentTimeMillis()}",
            nombre = nombre.trim(),
            email = email.trim(),
            password = password,
            rol = RolUsuario.CLIENTE.name
        )
        if (existing >= 0) {
            current[existing] = newDto
        } else {
            current.add(newDto)
        }
        prefs.putRawString(KEY_USERS_STORE, gson.toJson(current))
        return newDto.toUsuario()
    }

    fun getEmpresaInfoDefault(): EmpresaInfo {
        val type = object : TypeToken<EmpresaInfo>() {}.type
        return JsonAssetReader.read<EmpresaInfo>(context, ASSET_EMPRESA, type) ?: EmpresaInfo()
    }
}
