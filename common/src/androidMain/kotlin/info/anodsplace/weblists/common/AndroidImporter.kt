package info.anodsplace.weblists.common

import android.content.Context
import android.net.Uri
import com.charleskorn.kaml.Yaml
import info.anodsplace.weblists.common.db.WebSiteLists
import info.anodsplace.weblists.common.export.Code
import info.anodsplace.weblists.common.export.Importer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.logger.Logger
import java.io.*

class AndroidImporter(private val context: Context, logger: Logger, yaml: Yaml): Importer(logger, yaml) {
    override suspend fun import(srcUri: String): Pair<Int, WebSiteLists> = withContext(Dispatchers.IO) {
        val uri = Uri.parse(srcUri)
        val inputStream: InputStream?
        return@withContext try {
            inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Pair(Code.errorUnexpected, WebSiteLists.empty)
            readContent(InputStreamReader(inputStream))
        } catch (e: FileNotFoundException) {
            logger.error("export error: ${e.message}")
            Pair(Code.errorRead, WebSiteLists.empty)
        }
    }
}