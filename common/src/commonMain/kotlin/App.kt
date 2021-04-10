import com.squareup.sqldelight.db.SqlDriver
import io.ktor.client.statement.*
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

expect fun getPlatformName(): String
expect fun formatString(format: String, vararg args: Any?): String
expect fun parseColor(hexStr: String): Int
expect fun isValidUrl(url: String): Boolean

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

interface HtmlClient{
    suspend fun loadDoc(url: String): HtmlDocument
}

expect class JsoupParser() {
    suspend fun parse(response: HttpResponse, baseUri: String): HtmlDocument
}

expect class HtmlElement {
    fun text(): String
    fun select(cssQuery: String): HtmlElements
    fun selectFirst(cssQuery: String): HtmlElement
}

expect class HtmlElements : Iterable<HtmlElement>

expect class HtmlDocument {
    fun select(cssQuery: String): HtmlElements
    fun body(): HtmlElement
    fun title(): String
}

expect class HtmlClientFactory {
    fun create(): HtmlClient
}

expect class StreamWriter {
    fun write(content: String)
}