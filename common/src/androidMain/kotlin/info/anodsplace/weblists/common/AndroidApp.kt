package info.anodsplace.weblists.common

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import info.anodsplace.weblists.db.WebListsDb.Companion.Schema
import io.ktor.client.statement.*
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.InputStream

actual fun formatString(format: String, vararg args: Any?): String = String.format(format, *args)
actual fun parseColor(hexStr: String): Int = android.graphics.Color.parseColor(hexStr)
actual fun isValidUrl(url: String): Boolean = url.isValidUrl()

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            Schema,
            context,
            "web_lists.db",
            callback = object : AndroidSqliteDriver.Callback(Schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    db.execSQL("PRAGMA foreign_keys=ON;");
                }
            })
    }
}