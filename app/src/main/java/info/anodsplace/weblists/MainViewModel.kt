package info.anodsplace.weblists

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import info.anodsplace.weblists.rules.WebSection
import info.anodsplace.weblists.rules.WebSite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

sealed class ContentState {
    object Loading: ContentState()
    class Catalog(val sites: List<WebSite>): ContentState()
    class SiteDefinition(val site: WebSite): ContentState()
    class SiteSections(val site: WebSite, val sections: List<WebSection>): ContentState()
    class Error(val message: String): ContentState()
}

class MainViewModel(application: Application) : AndroidViewModel(application), KoinComponent {
    private val db: AppDatabase by inject()
    val prefs: Preferences by inject()

    val sites = MutableStateFlow<ContentState>(ContentState.Loading)

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

    fun loadSite(siteId: Long): Flow<ContentState> = flow {
        emit(ContentState.Loading)
        try {
            val webSiteLists = db.webSites().loadById(siteId)
            emit(ContentState.SiteDefinition(webSiteLists.site))
            val doc = withContext(Dispatchers.IO) { Jsoup.connect(webSiteLists.site.url).get() }
            val sections = webSiteLists.apply(doc)
            emit(ContentState.SiteSections(webSiteLists.site, sections))
        } catch (e: Exception) {
            emit(ContentState.Error(e.message ?: "Unexpected error"))
        }
    }

}