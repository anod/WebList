package info.anodsplace.weblists.common

import com.charleskorn.kaml.Yaml
import info.anodsplace.weblists.common.db.WebSiteLists
import info.anodsplace.weblists.common.export.Code
import info.anodsplace.weblists.common.export.Importer
import org.koin.core.logger.Logger
import java.io.File
import java.io.FileReader

class DesktopImporter(logger: Logger, yaml: Yaml) : Importer(logger, yaml) {

    override suspend fun import(srcUri: String): Pair<Int, WebSiteLists> {
        val reader = try {
            FileReader(File(srcUri))
        } catch (e: Exception) {
            return Pair(Code.errorRead, WebSiteLists.empty)
        }
        return readContent(reader)
    }
}