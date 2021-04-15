package info.anodsplace.weblists.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import info.anodsplace.weblists.BuildConfig
import info.anodsplace.weblists.common.export.Exporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.logger.Logger
import java.io.FileNotFoundException
import java.io.OutputStream
import java.io.OutputStreamWriter

class AndroidExporter(private val context: Context, logger: Logger): Exporter(logger) {

    companion object {
        const val AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider"
    }

    override suspend fun export(destUri: String, content: String): Int = withContext(Dispatchers.IO) {
        val uri = Uri.parse(destUri)
        val outputStream: OutputStream?
        return@withContext try {
            outputStream = context.contentResolver.openOutputStream(uri) ?: return@withContext ERROR_UNEXPECTED
            writeContent(OutputStreamWriter(outputStream), content)
        } catch (e: FileNotFoundException) {
            Log.e("export", e.message, e)
            ERROR_FILE_READ
        }
    }
}