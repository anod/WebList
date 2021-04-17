package info.anodsplace.weblists.common

import com.charleskorn.kaml.Yaml
import info.anodsplace.weblists.common.db.*
import info.anodsplace.weblists.common.export.Code
import info.anodsplace.weblists.common.export.Exporter
import info.anodsplace.weblists.common.export.Importer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.logger.Logger

sealed class ContentState {
    object Loading : ContentState()
    class Catalog(val sites: List<WebSite>) : ContentState()
    class SiteDefinition(val site: WebSite) : ContentState()
    class SiteSections(val site: WebSite, val sections: List<WebSection>) : ContentState()
    class Error(val message: String) : ContentState()
}

sealed class DocumentRequest {
    class Create(val name: String) : DocumentRequest()
    object Open : DocumentRequest()

    sealed class Result {
        object Unknown : DocumentRequest.Result()
        class Success(val uri: String) : DocumentRequest.Result()
        object Error : DocumentRequest.Result()
    }
}

interface AppViewModel : KoinComponent {
    val log: Logger
    var lastError: String
    val prefs: AppPreferences
    val yaml: Yaml
    val sites: MutableStateFlow<ContentState>
    val docSource: MutableStateFlow<HtmlDocument?>
    var currentSections: ContentState.SiteSections?
    val documentRequest: MutableSharedFlow<DocumentRequest>
    val documentRequestResult: MutableStateFlow<DocumentRequest.Result>

    fun loadSites()
    fun loadSite(siteId: Long): Flow<WebSite>
    fun loadContent(siteId: Long): Flow<ContentState>
    fun loadYaml(siteId: Long): Flow<String>
    fun updateDraft(site: WebSite, lists: List<WebList>, loadPreview: Boolean)
    fun export(title: String, content: String): Flow<Int>
    fun import(siteId: Long): Flow<Int>
}

class CommonAppViewModel(
    private val viewModelScope: CoroutineScope
) : AppViewModel {
    override val prefs: AppPreferences by inject()
    override val yaml: Yaml by inject()
    override val log: Logger by inject()

    private val db: AppDatabase by inject()
    private val jsoup: HtmlClient by inject()
    private val exporter: Exporter by inject()
    private val importer: Importer by inject()

    private var _draftSite: MutableStateFlow<WebSiteLists> = MutableStateFlow(WebSiteLists.empty)
    private var docJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    private var draftSite: Flow<String> = _draftSite.mapLatest {
        yaml.encodeToString(it)
    }.flowOn(Dispatchers.Default)

    override var lastError: String = ""
    override val sites = MutableStateFlow<ContentState>(ContentState.Loading)
    override val docSource = MutableStateFlow<HtmlDocument?>(null)
    override var currentSections: ContentState.SiteSections? = null
    override val documentRequest: MutableSharedFlow<DocumentRequest> = MutableSharedFlow()
    override val documentRequestResult = MutableStateFlow<DocumentRequest.Result>(DocumentRequest.Result.Unknown)

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

    override fun loadSite(siteId: Long): Flow<WebSite> = flow {
        val webSite = db.loadSiteById(siteId)
        emit(webSite)
    }

    override fun loadContent(siteId: Long): Flow<ContentState> = flow {
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

    override fun updateDraft(site: WebSite, lists: List<WebList>, loadPreview: Boolean) {
        _draftSite.value = WebSiteLists(site, lists)
        if (loadPreview && isValidUrl(site.url)) {
            docJob?.cancel()
            docJob = viewModelScope.launch {
                try {
                    val doc = jsoup.loadDoc(site.url)
                    if (site.title.isEmpty()) {
                        val title = doc.title()
                        if (title.isNotEmpty()) {
                            _draftSite.emit(WebSiteLists(site.copy(title = title), lists))
                        }
                    }
                    docSource.emit(doc)
                } catch (e: Exception) {
                    log.error("Cannot load ${site.url}: ${e.message}")
                }
            }
        }
    }

    override fun loadYaml(siteId: Long): Flow<String> {
        if (siteId == 0L) {
            _draftSite.value = WebSiteLists(siteId)
            return draftSite
        }
        viewModelScope.launch {
            try {
                val webSiteLists = db.loadSiteListsById(siteId)
                _draftSite.emit(webSiteLists)
                if (isValidUrl(webSiteLists.site.url)) {
                    val doc = jsoup.loadDoc(webSiteLists.site.url)
                    docSource.emit(doc)
                }
            } catch (e: Exception) {
                _draftSite.value = WebSiteLists(siteId)
            }
        }
        return draftSite
    }

    override fun export(title: String, content: String): Flow<Int> = flow {
        documentRequestResult.value = DocumentRequest.Result.Unknown
        documentRequest.emit(DocumentRequest.Create("weblist-$title.yaml"))
        when (val result = documentRequestResult.first { it != DocumentRequest.Result.Unknown }) {
            is DocumentRequest.Result.Success -> emit(exporter.export(result.uri, content))
            DocumentRequest.Result.Error -> emit(Code.errorUnexpected)
            DocumentRequest.Result.Unknown -> {
            }
        }
    }

    override fun import(siteId: Long): Flow<Int> = flow {
        documentRequestResult.value = DocumentRequest.Result.Unknown
        documentRequest.emit(DocumentRequest.Open)
        when (val result = documentRequestResult.first { it != DocumentRequest.Result.Unknown }) {
            is DocumentRequest.Result.Success -> {
                val imported = importer.import(result.uri)
                if (imported.first == Code.resultDone) {
                    try {
                        db.upsert(siteId, imported.second.site, imported.second.lists)
                        emit(Code.resultDone)
                    } catch (e: Exception) {
                        log.error("${e.message}")
                        emit(Code.resultDone)
                    }
                } else {
                    emit(imported.first)
                }
            }
            DocumentRequest.Result.Error -> emit(Code.errorUnexpected)
            DocumentRequest.Result.Unknown -> {
            }
        }
    }
}
