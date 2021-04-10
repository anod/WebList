package info.anodsplace.weblists.backup

import StreamWriter
import android.content.Context
import android.net.Uri
import android.util.Log
import info.anodsplace.weblists.BuildConfig
import info.anodsplace.weblists.common.export.Export
import info.anodsplace.weblists.common.export.Export.Companion.ERROR_FILE_READ
import info.anodsplace.weblists.common.export.Export.Companion.ERROR_FILE_WRITE
import info.anodsplace.weblists.common.export.Export.Companion.ERROR_UNEXPECTED
import info.anodsplace.weblists.common.export.Export.Companion.RESULT_DONE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException
import java.io.OutputStream

class Backup(private val context: Context): Export {
    private val mutex = Mutex()

    companion object {
        const val AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider"
    }

    override suspend fun export(destUri: String, content: String): Int = withContext(Dispatchers.IO) {
        val uri = Uri.parse(destUri)
        val outputStream: OutputStream?
        return@withContext try {
            outputStream = context.contentResolver.openOutputStream(uri) ?: return@withContext ERROR_UNEXPECTED
            writeToStream(outputStream, content)
        } catch (e: FileNotFoundException) {
            Log.e("export", e.message, e)
            ERROR_FILE_READ
        }
    }

    private suspend fun writeToStream(outputStream: OutputStream, content: String): Int {
        return try {
            mutex.withLock {
                StreamWriter(outputStream).write(content)
            }
            RESULT_DONE
        } catch (e: IOException) {
            Log.e("export", e.message, e)
            ERROR_FILE_WRITE
        }
    }
}