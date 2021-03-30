package info.anodsplace.weblists

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import info.anodsplace.weblists.rules.WebSection
import info.anodsplace.weblists.rules.WebSite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

sealed class ContentState {
    object Loading: ContentState()
    class Catalog(val sites: List<WebSite>): ContentState()
    class Site(val title: String, val sections: List<WebSection>): ContentState()
    object Empty: ContentState()
    class Error(val message: String): ContentState()
}

class MainViewModel(application: Application) : AndroidViewModel(application), KoinComponent {
    val sites = MutableStateFlow<ContentState>(ContentState.Loading)
    val site = MutableStateFlow<ContentState>(ContentState.Loading)

    private val db: AppDatabase by inject()

    fun loadSites() {
        viewModelScope.launch {
            try {
                db.webSites().preload()
                sites.emit(ContentState.Catalog(db.webSites().loadSites()))
            } catch (e: Exception) {
                sites.emit(ContentState.Error(e.message ?: "Unexpected error"))
            }
        }
    }

    fun loadSite(siteId: Long) {
        viewModelScope.launch {
            try {
                val webSite = db.webSites().loadById(siteId)
                val doc = withContext(Dispatchers.IO) { Jsoup.connect(webSite.site.url).get() }
                val sections = webSite.apply(doc)
                site.emit(ContentState.Site(webSite.site.title, sections))
            } catch (e: Exception) {
                site.emit(ContentState.Error(e.message ?: "Unexpected error"))
            }
        }
    }
}