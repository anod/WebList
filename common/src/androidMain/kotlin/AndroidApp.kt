import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import info.anodsplace.weblists.common.JsoupClient
import info.anodsplace.weblists.db.WebListsDb.Companion.Schema
import info.anodsplace.weblists.common.isValidUrl
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.charsets.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.*
import kotlin.text.Charsets

actual fun getPlatformName(): String = "Android"

actual fun formatString(format: String, vararg args: Any?): String = String.format(format, *args)

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(Schema, context, "web_lists.db", callback = object : AndroidSqliteDriver.Callback(Schema) {
            override fun onOpen(db: SupportSQLiteDatabase) {
                db.execSQL("PRAGMA foreign_keys=ON;");
            }
        })
    }
}

actual class HtmlDocument(private val doc: Document) {
    actual fun select(cssQuery: String): HtmlElements = HtmlElements(doc.select(cssQuery))
    actual fun body(): HtmlElement = HtmlElement(doc.body())
    actual fun title(): String = doc.title()
}

actual class HtmlClientFactory {
    actual fun create(): HtmlClient = JsoupClient(HttpClient())
}

actual class HtmlElement(private val el: Element) {
    actual fun text(): String = el.text()
    actual fun select(cssQuery: String): HtmlElements = HtmlElements(el.select(cssQuery))
    actual fun selectFirst(cssQuery: String): HtmlElement = HtmlElement(el.selectFirst(cssQuery))
}

actual class HtmlElements(private val elements: Elements) : Iterable<HtmlElement> {
    override fun iterator(): Iterator<HtmlElement> {
        val it = elements.iterator()
        return object : Iterator<HtmlElement> {
            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): HtmlElement = HtmlElement(it.next())
        }
    }
}

actual fun parseColor(hexStr: String): Int = android.graphics.Color.parseColor(hexStr)
actual class StreamWriter(private val outputWriter: Writer) {
    actual fun write(content: String) {
        val writer = BufferedWriter(outputWriter)
        writer.write(content)
        writer.close()
    }
}

actual class JsoupParser {
    actual suspend fun parse(response: HttpResponse, baseUri: String): HtmlDocument {
        val charset: Charset = response.contentType()?.charset() ?: Charsets.UTF_8
        val inputStream = response.receive<InputStream>()
        val doc = Jsoup.parse(inputStream, charset.name, baseUri).normalise()
        return HtmlDocument(doc)
    }
}

actual fun isValidUrl(url: String): Boolean = url.isValidUrl()