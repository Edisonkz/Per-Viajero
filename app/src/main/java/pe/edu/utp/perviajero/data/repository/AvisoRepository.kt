package pe.edu.utp.perviajero.data.repository

import pe.edu.utp.perviajero.data.model.AvisoTuristico
import pe.edu.utp.perviajero.services.AvisoService

interface AvisoRepository {
    fun listarAvisos(): List<AvisoTuristico>
    fun filtrarPorTipo(tipo: String): List<AvisoTuristico>
    fun obtenerPorId(id: Int): AvisoTuristico?
    fun agregarAviso(aviso: AvisoTuristico)
    fun marcarLeido(id: Int)
    fun marcarTodosLeidos()
    fun contarNoLeidos(): Int
    fun limpiarHistorial()
}

class LocalAvisoRepository(
    private val avisoService: AvisoService
) : AvisoRepository {

    override fun listarAvisos(): List<AvisoTuristico> = avisoService.listar()

    override fun filtrarPorTipo(tipo: String): List<AvisoTuristico> {
        val todos = avisoService.listar()
        if (tipo.isBlank() || tipo.equals("Todos", ignoreCase = true)) return todos
        return todos.filter { it.tipo.equals(tipo, ignoreCase = true) }
    }

    override fun obtenerPorId(id: Int): AvisoTuristico? = avisoService.obtenerPorId(id)

    override fun agregarAviso(aviso: AvisoTuristico) = avisoService.agregar(aviso)

    override fun marcarLeido(id: Int) = avisoService.marcarLeido(id)

    override fun marcarTodosLeidos() = avisoService.marcarTodosLeidos()

    override fun contarNoLeidos(): Int = avisoService.contarNoLeidos()

    override fun limpiarHistorial() = avisoService.limpiar()
}
