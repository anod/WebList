package info.anodsplace.weblists.common

import android.content.Context
import android.net.Uri
import info.anodsplace.weblists.common.export.Exporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.logger.Logger
import java.io.FileNotFoundException
import java.io.OutputStream
import java.io.OutputStreamWriter

class AndroidExporter(private val context: Context, logger: Logger): Exporter(logger) {

    override suspend fun export(destUri: String, content: String): Int = withContext(Dispatchers.IO) {
        val uri = Uri.parse(destUri)
        val outputStream: OutputStream?
        return@withContext try {
            outputStream = context.contentResolver.openOutputStream(uri) ?: return@withContext ERROR_UNEXPECTED
            writeContent(OutputStreamWriter(outputStream), content)
        } catch (e: FileNotFoundException) {
            logger.error("export error: ${e.message}")
            ERROR_FILE_READ
        }
    }
}