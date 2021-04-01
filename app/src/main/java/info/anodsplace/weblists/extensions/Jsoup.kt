package info.anodsplace.weblists.extensions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

suspend fun loadDoc(url: String): Document = withContext(Dispatchers.IO) {
    return@withContext Jsoup.connect(url).get()
}
