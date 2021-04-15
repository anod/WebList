package info.anodsplace.weblists.common

import com.charleskorn.kaml.Yaml
import info.anodsplace.weblists.common.db.*
import info.anodsplace.weblists.common.export.Exporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.logger.Logger

sealed class ContentState {
    object Loading: ContentState()
    class Catalog(val sites: List<WebSite>): ContentState()
    class SiteDefinition(val site: WebSite): ContentState()
    class SiteSections(val site: WebSite, val sections: List<WebSection>): ContentState()
    class Error(val message: String): ContentState()
}

interface AppViewModel: KoinComponent {
    val log: Logger
    var lastError: String
    val prefs: AppPreferences
    val yaml: Yaml
    val sites: MutableStateFlow<ContentState>
    val docSource: MutableStateFlow<HtmlDocument?>
    var currentSections: ContentState.SiteSections?
    val createDocumentRequest: MutableSharedFlow<String>

    fun loadSites()
    fun loadSite(siteId: Long): Flow<ContentState>
    fun updateDraft(site: WebSite, lists: List<WebList>, loadPreview: Boolean)
    fun loadDraft(siteId: Long): MutableStateFlow<WebSiteLists?>
    fun export(siteId: Long, content: String): Flow<Int>
    fun onExportUri(isSuccess: Boolean, destUri: String)
}

class CommonAppViewModel(
    private val viewModelScope: CoroutineScope
): AppViewModel {
    private val db: AppDatabase by inject()
    private val jsoup: HtmlClient by inject()
    private val exporter: Exporter by inject()
    private var draftSite: MutableStateFlow<WebSiteLists?> = MutableStateFlow(null)

    override val prefs: AppPreferences by inject()
    override val yaml: Yaml by inject()
    override val log: Logger by inject()

    override var lastError: String = ""
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
        if (loadPreview && isValidUrl(site.url)) {
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
                    // Log.e("UpdateDraft", "Cannot load ${site.url}", e)
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
                if (isValidUrl(webSiteLists.site.url)) {
                    val doc = jsoup.loadDoc(webSiteLists.site.url)
                    docSource.emit(doc)
                }
            } catch (e: Exception) {
                draftSite.value = WebSiteLists(WebSite(siteId, "", ""), emptyList())
            }
        }
        return draftSite
    }

    override val createDocumentRequest: MutableSharedFlow<String> = MutableSharedFlow()
    private val onExportUri = MutableStateFlow<ExportUriResult>(ExportUriResult.Unknown)
    private val exportState = MutableStateFlow(Exporter.NO_RESULT)
    override fun export(siteId: Long, content: String): Flow<Int> {
        exportState.value = Exporter.NO_RESULT
        onExportUri.value = ExportUriResult.Unknown

        viewModelScope.launch {
           createDocumentRequest.emit("export-$siteId.yaml")
           onExportUri.collect { exportUriResult ->
               if (exportUriResult is ExportUriResult.Success) {
                   exportState.value = exporter.export(exportUriResult.destUri, content)
               } else {
                   exportState.value = Exporter.ERROR_UNEXPECTED
               }
           }
        }
        return exportState
    }

    override fun onExportUri(isSuccess: Boolean, destUri: String) {
        viewModelScope.launch {
            onExportUri.emit(if (isSuccess) ExportUriResult.Success(destUri) else ExportUriResult.Error)
        }
    }
}

sealed class ExportUriResult {
    object Unknown: ExportUriResult()
    class Success(val destUri: String): ExportUriResult()
    object Error: ExportUriResult()
}
