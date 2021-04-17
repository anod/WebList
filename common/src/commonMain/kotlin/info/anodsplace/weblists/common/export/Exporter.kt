package info.anodsplace.weblists.common.export

import info.anodsplace.weblists.common.export.Code.errorWrite
import info.anodsplace.weblists.common.export.Code.resultDone
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.logger.Logger
import java.io.BufferedWriter
import java.io.IOException
import java.io.Writer

abstract class Exporter(val logger: Logger) {
    private val mutex = Mutex()

    abstract suspend fun export(destUri: String, content: String): Int
    suspend fun writeContent(writer: Writer, content: String): Int {
        logger.debug("Write content ${content.length}")
        return try {
            mutex.withLock {
                BufferedWriter(writer).use { out -> out.write(content) }
            }
            resultDone
        } catch (e: IOException) {
            logger.error("Export error $e")
            errorWrite
        }
    }
}