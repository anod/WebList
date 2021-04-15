package info.anodsplace.weblists.common

import info.anodsplace.weblists.common.export.Exporter
import org.koin.core.logger.Logger
import java.io.File
import java.io.FileWriter

class DesktopExporter(logger: Logger) : Exporter(logger) {

    override suspend fun export(destUri: String, content: String): Int {
        return writeContent(FileWriter(File(destUri)), content)
    }
}