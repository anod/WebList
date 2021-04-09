import com.squareup.sqldelight.db.SqlDriver

actual fun getPlatformName(): String = "Desktop"
actual fun formatString(format: String, vararg args: Any?): String = String.format(format, args)
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        TODO("Not yet implemented")
    }
}

actual class HtmlDocument {
    actual fun select(cssQuery: String): HtmlElements {
        TODO("Not yet implemented")
    }

    actual fun body(): HtmlElement {
        TODO("Not yet implemented")
    }

    actual fun title(): String {
        TODO("Not yet implemented")
    }
}

actual class HtmlClientFactory {
    actual fun create(): HtmlClient {
        TODO("Not yet implemented")
    }
}

actual class HtmlElements : Iterable<HtmlElement> {
    override fun iterator(): Iterator<HtmlElement> {
        TODO("Not yet implemented")
    }
}

actual class HtmlElement {
    actual fun text(): String {
        TODO("Not yet implemented")
    }

    actual fun select(cssQuery: String): HtmlElements {
        TODO("Not yet implemented")
    }

    actual fun selectFirst(cssQuery: String): HtmlElement {
        TODO("Not yet implemented")
    }
}

actual fun parseColor(hexStr: String): Int {
    TODO("Not yet implemented")
}

actual class StreamWriter {
    actual fun write(content: String) {
        TODO("Not yet implemented")
    }
}