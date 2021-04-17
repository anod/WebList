package info.anodsplace.weblists.common

import info.anodsplace.weblists.common.export.Code
import info.anodsplace.weblists.common.export.Exporter
import org.koin.core.logger.Logger
import java.io.File
import java.io.FileWriter

class DesktopExporter(logger: Logger) : Exporter(logger) {

    override suspend fun export(destUri: String, content: String): Int {
        val writer = try {
            FileWriter(File(destUri))
        } catch (e: Exception) {
            return Code.errorWrite
        }
        return writeContent(writer, content)
    }
}