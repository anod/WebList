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

class JsoupClient(private val client: HttpClient, private val parser: HtmlParser): HtmlClient {

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