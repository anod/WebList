import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.InputStream

class JsoupClientAndroid(private val client: HttpClient): HtmlClient {

    override suspend fun loadDoc(url: String): HtmlDocument = withContext(Dispatchers.IO) {

        val response = client.request<HttpResponse> {
            url(url)
            method = HttpMethod.Get
        }
        if (!response.status.isSuccess()) {
            throw Exception("Unexpected code $response")
        }
        val charset = response.contentType()?.charset() ?: Charsets.UTF_8
        val inputStream = response.receive<InputStream>()
        val doc = Jsoup.parse(inputStream, charset.name(), url).normalise()
        return@withContext HtmlDocument(doc)
    }
}