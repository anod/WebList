package info.anodsplace.weblists

import HtmlClient
import HtmlDocument
import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.charleskorn.kaml.Yaml
import info.anodsplace.weblists.backup.Backup
import info.anodsplace.weblists.common.AppPreferences
import info.anodsplace.weblists.common.AppViewModel
import info.anodsplace.weblists.common.ContentState
import info.anodsplace.weblists.common.db.WebList
import info.anodsplace.weblists.common.db.WebSection
import info.anodsplace.weblists.common.db.WebSite
import info.anodsplace.weblists.common.db.WebSiteLists
import info.anodsplace.weblists.common.db.AppDatabase
import info.anodsplace.weblists.extensions.isValidUrl
import io.ktor.client.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainViewModel(application: Application) : AndroidViewModel(application), KoinComponent,
    AppViewModel {
    private val db: AppDatabase by inject()
    private val jsoup: HtmlClient by inject()
    private val backup: Backup by inject()
    private var draftSite: MutableStateFlow<WebSiteLists?> = MutableStateFlow(null)

    override var lastError: String = ""
    override val prefs: AppPreferences by inject()
    override val yaml: Yaml by inject()
    override val sites = MutableStateFlow<ContentState>(ContentState.Loading)
    override val docSource = MutableStateFlow<HtmlDocument?>(null)
    override var currentSections: ContentState.SiteSections? = null

    override fun loadSites() {
        viewModelScope.launch {
            db.preload()

            try {
                sites.emit(ContentState.Catalog(db.loadSites()))
            } catch (e: Exception) {
                sites.emit(ContentState.Error(e.message ?: "Unexpected error"))
            }
        }
    }

    override fun loadSite(siteId: Long): Flow<ContentState> = flow {
        currentSections = null
        emit(ContentState.Loading)
        try {
            val webSiteLists = db.loadSiteListsById(siteId)
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
    override fun updateDraft(site: WebSite, lists: List<WebList>, loadPreview: Boolean) {
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

    override fun loadDraft(siteId: Long): MutableStateFlow<WebSiteLists?> {
        if (siteId == 0L) {
            draftSite.value = WebSiteLists(WebSite(siteId, "", ""), emptyList())
            return draftSite
        }
        viewModelScope.launch {
            try {
                val webSiteLists = db.loadSiteListsById(siteId)
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
    override fun export(siteId: Long, content: String): Flow<Int> = flow {
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