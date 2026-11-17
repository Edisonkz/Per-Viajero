package pe.edu.utp.perviajero.util

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogLevel {
    DEBUG, INFO, WARN, ERROR
}

data class LogEntry(
    val timestamp: String,
    val level: LogLevel,
    val tag: String,
    val message: String
)

/**
 * Logger centralizado con persistencia en buffer circular (máx. 120 entradas) para observabilidad y soporte.
 */
object AppLogger {

    private const val PREFS_LOGS = "peruviajero_logs_prefs"
    private const val KEY_LOGS = "key_log_entries"
    private const val MAX_ENTRIES = 120

    private var prefs: SharedPreferences? = null
    private var isDebugBuild: Boolean = false
    private val gson = Gson()
    private val memoryLogs = mutableListOf<LogEntry>()
    private val dateFormat = SimpleDateFormat("dd/MM HH:mm:ss", Locale.US)

    @Synchronized
    fun init(context: Context) {
        val appCtx = context.applicationContext
        isDebugBuild = (appCtx.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (prefs == null) {
            prefs = appCtx.getSharedPreferences(PREFS_LOGS, Context.MODE_PRIVATE)
            cargarDesdePrefs()
        }
    }

    fun d(tag: String, message: String) = log(LogLevel.DEBUG, tag, message)
    fun i(tag: String, message: String) = log(LogLevel.INFO, tag, message)
    fun w(tag: String, message: String) = log(LogLevel.WARN, tag, message)

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val fullMsg = if (throwable != null) {
            "$message | ${throwable.javaClass.simpleName}: ${throwable.message}"
        } else {
            message
        }
        log(LogLevel.ERROR, tag, fullMsg)
        if (isDebugBuild && throwable != null) {
            Log.e(tag, message, throwable)
        }
    }

    @Synchronized
    private fun log(level: LogLevel, tag: String, message: String) {
        if (isDebugBuild) {
            when (level) {
                LogLevel.DEBUG -> Log.d(tag, message)
                LogLevel.INFO -> Log.i(tag, message)
                LogLevel.WARN -> Log.w(tag, message)
                LogLevel.ERROR -> Log.e(tag, message)
            }
        }

        val entry = LogEntry(
            timestamp = dateFormat.format(Date()),
            level = level,
            tag = tag,
            message = message
        )
        memoryLogs.add(0, entry)
        if (memoryLogs.size > MAX_ENTRIES) {
            memoryLogs.subList(MAX_ENTRIES, memoryLogs.size).clear()
        }
        guardarEnPrefs()
    }

    @Synchronized
    fun obtenerLogs(): List<LogEntry> = memoryLogs.toList()

    @Synchronized
    fun limpiarLogs() {
        memoryLogs.clear()
        prefs?.edit()?.remove(KEY_LOGS)?.apply()
    }

    private fun cargarDesdePrefs() {
        val json = prefs?.getString(KEY_LOGS, null) ?: return
        try {
            val type = object : TypeToken<List<LogEntry>>() {}.type
            val restored: List<LogEntry> = gson.fromJson(json, type) ?: emptyList()
            memoryLogs.clear()
            memoryLogs.addAll(restored.take(MAX_ENTRIES))
        } catch (_: Exception) {
            memoryLogs.clear()
        }
    }

    private fun guardarEnPrefs() {
        try {
            prefs?.edit()?.putString(KEY_LOGS, gson.toJson(memoryLogs))?.apply()
        } catch (_: Exception) {
            // Ignorar fallos de serialización de logs para no afectar flujo principal
        }
    }
}
