package info.anodsplace.weblists.backup

import StreamWriter
import android.content.Context
import android.net.Uri
import android.util.Log
import info.anodsplace.weblists.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException
import java.io.OutputStream

class Backup(val context: Context) {

    companion object {
        const val AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider"
        const val NO_RESULT = -1
        const val RESULT_DONE = 0
        const val ERROR_FILE_READ = 3
        const val ERROR_FILE_WRITE = 4
        const val ERROR_DESERIALIZE = 5
        const val ERROR_UNEXPECTED = 6
        const val ERROR_INCORRECT_FORMAT = 7
    }

    suspend fun export(uri: Uri, content: String): Int = withContext(Dispatchers.IO) {
        val outputStream: OutputStream?
        return@withContext try {
            outputStream = context.contentResolver.openOutputStream(uri) ?: return@withContext ERROR_UNEXPECTED
            writeToStream(outputStream, content)
        } catch (e: FileNotFoundException) {
            Log.e("export", e.message, e)
            ERROR_FILE_READ
        }
    }

    private val mutex = Mutex()


    suspend fun writeToStream(outputStream: OutputStream, content: String): Int {
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