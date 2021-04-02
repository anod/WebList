package info.anodsplace.weblists.extensions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import okhttp3.OkHttpClient

class JsoupClient(private val client: OkHttpClient) {

    constructor(): this(OkHttpClient())

    suspend fun loadDoc(url: String): Document = withContext(Dispatchers.IO) {
        val httpUrl = url.toHttpUrl()
        val request = Request.Builder().url(httpUrl).build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Unexpected code $response")
        }
        val body = response.body ?: throw Exception("Empty body")
        val charset = body.contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
        val stream = response.body?.byteStream()
        return@withContext Jsoup.parse(stream, charset.name(), url).normalise()
    }
}
