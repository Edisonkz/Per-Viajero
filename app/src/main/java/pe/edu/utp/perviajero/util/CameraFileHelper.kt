package pe.edu.utp.perviajero.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Utilidad de archivos multimedia para captura con Cámara vía FileProvider y copiado local de galería.
 */
object CameraFileHelper {

    private const val TAG = "CameraFileHelper"

    fun createPhotoOutputUri(context: Context, prefix: String = "pv_photo"): Pair<Uri, String> {
        val photosDir = File(context.filesDir, "Pictures").apply {
            if (!exists()) mkdirs()
        }
        val photoFile = File(photosDir, "${prefix}_${System.currentTimeMillis()}.jpg")
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        return contentUri to Uri.fromFile(photoFile).toString()
    }

    /**
     * Copia una imagen elegida desde la galería hacia almacenamiento privado para evitar pérdida de acceso URI.
     */
    fun copyUriToInternalStorage(context: Context, sourceUri: Uri, prefix: String = "pv_gal"): String? {
        return try {
            val photosDir = File(context.filesDir, "Pictures").apply {
                if (!exists()) mkdirs()
            }
            val targetFile = File(photosDir, "${prefix}_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(targetFile).toString()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error copiando imagen de galería", e)
            null
        }
    }
}
