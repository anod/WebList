import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import com.squareup.sqldelight.db.SqlDriver

@Composable
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }

    MaterialTheme {
        Button(onClick = {
            text = "Hello, ${getPlatformName()}"
        }) {
            Text(text)
        }
    }
}

expect fun getPlatformName(): String

expect fun formatString(format: String, vararg args: Any?): String
expect fun parseColor(hexStr: String): Int

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

interface HtmlClient{
    suspend fun loadDoc(url: String): HtmlDocument
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