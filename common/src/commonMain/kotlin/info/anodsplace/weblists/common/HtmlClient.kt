package info.anodsplace.weblists.common

import io.ktor.utils.io.charsets.*
import java.io.InputStream

interface HtmlParser {
    suspend fun parse(inputStream: InputStream, charset: Charset, baseUrl: String): HtmlDocument
}

interface HtmlClient {
    suspend fun loadDoc(url: String): HtmlDocument
}

interface HtmlDocument {
    fun select(cssQuery: String): HtmlElements
    fun body(): HtmlElement
    fun title(): String
}

interface HtmlElement {
    fun text(): String
    fun select(cssQuery: String): HtmlElements
    fun selectFirst(cssQuery: String): HtmlElement
}

interface HtmlElements : Iterable<HtmlElement>