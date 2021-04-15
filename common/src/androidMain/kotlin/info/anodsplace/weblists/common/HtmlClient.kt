package info.anodsplace.weblists.common

import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.InputStream

class JsoupParser: HtmlParser {
    override suspend fun parse(inputStream: InputStream, charset: Charset, baseUrl: String): JsoupHtmlDocument = withContext(Dispatchers.Default) {
        val doc = Jsoup.parse(inputStream, charset.name, baseUrl).normalise()
        return@withContext JsoupHtmlDocument(doc)
    }
}

class JsoupHtmlDocument(private val doc: Document): HtmlDocument {
    override fun select(cssQuery: String): HtmlElements = JsoupHtmlElements(doc.select(cssQuery))
    override fun body(): HtmlElement = JsoupHtmlElement(doc.body())
    override fun title(): String = doc.title()
}

class JsoupHtmlElement(private val el: Element): HtmlElement {
    override fun text(): String = el.text()
    override fun select(cssQuery: String): HtmlElements = JsoupHtmlElements(el.select(cssQuery))
    override fun selectFirst(cssQuery: String): HtmlElement = JsoupHtmlElement(el.selectFirst(cssQuery))
}

class JsoupHtmlElements(private val elements: Elements) : HtmlElements {
    override fun iterator(): Iterator<HtmlElement> {
        val it = elements.iterator()
        return object : Iterator<HtmlElement> {
            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): HtmlElement = JsoupHtmlElement(it.next())
        }
    }
}