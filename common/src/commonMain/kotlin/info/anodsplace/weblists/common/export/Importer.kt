package info.anodsplace.weblists.common.export

import com.charleskorn.kaml.Yaml
import info.anodsplace.weblists.common.db.WebSiteLists
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import org.koin.core.logger.Logger
import java.io.BufferedReader
import java.io.IOException
import java.io.Reader

abstract class Importer(val logger: Logger, val yaml: Yaml) {
    private val mutex = Mutex()

    abstract suspend fun import(srcUri: String): Pair<Int, WebSiteLists>
    suspend fun readContent(reader: Reader): Pair<Int, WebSiteLists> {
        logger.debug("Read content $reader")
        return try {
            var webSiteLists: WebSiteLists? = null
            mutex.withLock {
                val text: String = BufferedReader(reader).use { reader -> reader.readText() }
                webSiteLists = yaml.decodeFromString(text)
            }
            if (webSiteLists != null) {
                Pair(Code.resultDone, webSiteLists!!)
            } else {
                Pair(Code.errorDeserialize, WebSiteLists.empty)
            }
        } catch (e: IOException) {
            logger.error("Export error $e")
            Pair(Code.errorWrite, WebSiteLists.empty)
        }
    }
}