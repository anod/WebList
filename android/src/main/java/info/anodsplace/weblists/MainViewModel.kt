package info.anodsplace.weblists

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.charleskorn.kaml.Yaml
import info.anodsplace.weblists.backup.Backup
import info.anodsplace.weblists.extensions.JsoupClient
import info.anodsplace.weblists.extensions.isValidUrl
import info.anodsplace.weblists.rules.WebList
import info.anodsplace.weblists.rules.WebSection
import info.anodsplace.weblists.rules.WebSite
import info.anodsplace.weblists.rules.WebSiteLists
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jsoup.nodes.Document
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
    var lastError: String = ""
    private val db: AppDatabase by inject()
    private val jsoup: JsoupClient by inject()
    private val backup: Backup by inject()
    val prefs: Preferences by inject()
    val yaml: Yaml by inject()
    val sites = MutableStateFlow<ContentState>(ContentState.Loading)
    private var draftSite: MutableStateFlow<WebSiteLists?> = MutableStateFlow(null)
    val docSource = MutableStateFlow<Document?>(null)
    var currentSections: ContentState.SiteSections? = null

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
        currentSections = null
        emit(ContentState.Loading)
        try {
            val webSiteLists = db.webSites().loadById(siteId)
            emit(ContentState.SiteDefinition(webSiteLists.site))
            val doc = jsoup.loadDoc(webSiteLists.site.url)
            val sections = webSiteLists.apply(doc)
            val state = ContentState.SiteSections(webSiteLists.site, sections)
            currentSections = state
            emit(state)
        } catch (e: Exception) {
            emit(ContentState.Error(e.message ?: "Unexpected error"))
        }
    }

    private var docJob: Job? = null
    fun updateDraft(site: WebSite, lists: List<WebList>, loadPreview: Boolean) {
        draftSite.value = WebSiteLists(site, lists)
        if (loadPreview && site.url.isValidUrl()) {
            docJob?.cancel()
            docJob = viewModelScope.launch {
                    try {
                        val doc = jsoup.loadDoc(site.url)
                        if (site.title.isEmpty()) {
                            val title = doc.title()
                            if (title.isNotEmpty()) {
                                draftSite.emit(WebSiteLists(site.copy(title = title), lists))
                            }
                        }
                        docSource.emit(doc)
                    } catch (e: Exception) {
                        Log.e("UpdateDraft", "Cannot load ${site.url}", e)
                    }
                }
        }
    }

    fun loadDraft(siteId: Long): MutableStateFlow<WebSiteLists?> {
        if (siteId == 0L) {
            draftSite.value = WebSiteLists(WebSite(siteId, "", ""), emptyList())
            return draftSite
        }
        viewModelScope.launch {
            try {
                val webSiteLists = db.webSites().loadById(siteId)
                draftSite.emit(webSiteLists)
                if (webSiteLists.site.url.isValidUrl()) {
                    val doc = jsoup.loadDoc(webSiteLists.site.url)
                    docSource.emit(doc)
                }
            } catch (e: Exception) {
                draftSite.value = WebSiteLists(WebSite(siteId, "", ""), emptyList())
            }
        }
        return draftSite
    }

    val createDocument = MutableSharedFlow<String>()
    private val onExportUri = MutableSharedFlow<Uri?>()
    fun export(siteId: Long, content: String): Flow<Int> = flow {
        createDocument.emit("export-$siteId.yaml")
        val destUri = onExportUri.first { it != null }!!
        if (destUri != Uri.EMPTY) {
            val result = backup.export(destUri, content)
            emit(result)
        } else {
            emit(Backup.ERROR_UNEXPECTED)
        }
    }

    fun exportTo(destUri: Uri?) {
        viewModelScope.launch {
            onExportUri.emit(destUri ?: Uri.EMPTY)
        }
    }

    val openDocument = MutableSharedFlow<Boolean>()
    private val onImportUri = MutableSharedFlow<Uri?>()
    fun importFrom(destUri: Uri?) {
        onImportUri.tryEmit(destUri)
    }
}