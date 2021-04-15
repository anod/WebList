import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import info.anodsplace.weblists.common.JsoupClient
import info.anodsplace.weblists.db.WebListsDb.Companion.Schema
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
import java.lang.IllegalArgumentException
import kotlin.text.Charsets

actual fun getPlatformName(): String = "Desktop"
actual fun formatString(format: String, vararg args: Any?): String = String.format(format, *args)

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        Schema.create(driver)
        return driver
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

actual fun parseColor(hexStr: String): Int {
    if (hexStr[0] == '#') {
        // Use a long to avoid rollovers on #ffXXXXXX
        var color: Long = hexStr.substring(1).toLong(16)
        if (hexStr.length == 7) {
            // Set the alpha value
            color = color or -0x1000000
        } else require(hexStr.length == 9) { "Unknown color" }
        return color.toInt()
    }
    throw IllegalArgumentException("Unknown color")
}

actual class JsoupParser {
    actual suspend fun parse(response: HttpResponse, baseUri: String): HtmlDocument {
        val charset: Charset = response.contentType()?.charset() ?: Charsets.UTF_8
        val inputStream = response.receive<InputStream>()
        val doc = Jsoup.parse(inputStream, charset.name, baseUri).normalise()
        return HtmlDocument(doc)
    }
}

actual fun isValidUrl(url: String): Boolean {
    return url.startsWith("http", ignoreCase = true)
}