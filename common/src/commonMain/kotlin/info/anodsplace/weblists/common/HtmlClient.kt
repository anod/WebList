package info.anodsplace.weblists.common

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

interface HtmlParser {
    suspend fun parse(inputStream: InputStream, charset: Charset, baseUrl: String): HtmlDocument
}

interface HtmlClient {
    suspend fun loadDoc(url: String): HtmlDocument
}

class HtmlClientNetwork(private val client: HttpClient, private val parser: HtmlParser): HtmlClient {

    override suspend fun loadDoc(url: String): HtmlDocument = withContext(Dispatchers.Default) {

        val response = client.request<HttpResponse> {
            url(url)
            method = HttpMethod.Get
        }
        if (!response.status.isSuccess()) {
            throw Exception("Unexpected code $response")
        }

        val charset: Charset = response.contentType()?.charset() ?: Charsets.UTF_8
        val inputStream = response.receive<InputStream>()

        return@withContext parser.parse(inputStream, charset, url)
    }
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