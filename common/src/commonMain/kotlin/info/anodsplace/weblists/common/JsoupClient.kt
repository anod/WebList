package info.anodsplace.weblists.common

import HtmlClient
import HtmlDocument
import JsoupParser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JsoupClient(private val client: HttpClient): HtmlClient {

    override suspend fun loadDoc(url: String): HtmlDocument = withContext(Dispatchers.Default) {

        val response = client.request<HttpResponse> {
            url(url)
            method = HttpMethod.Get
        }
        if (!response.status.isSuccess()) {
            throw Exception("Unexpected code $response")
        }
        return@withContext JsoupParser().parse(response, url)
    }
}