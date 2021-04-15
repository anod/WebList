package info.anodsplace.weblists.common.export

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
            RESULT_DONE
        } catch (e: IOException) {
            logger.error("Export error $e")
            ERROR_FILE_WRITE
        }
    }

    companion object {
        const val NO_RESULT = -1
        const val RESULT_DONE = 0
        const val ERROR_FILE_READ = 3
        const val ERROR_FILE_WRITE = 4
        const val ERROR_DESERIALIZE = 5
        const val ERROR_UNEXPECTED = 6
        const val ERROR_INCORRECT_FORMAT = 7
    }
}