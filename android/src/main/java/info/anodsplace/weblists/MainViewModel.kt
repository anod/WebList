package info.anodsplace.weblists

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.charleskorn.kaml.Yaml
import info.anodsplace.weblists.common.*
import info.anodsplace.weblists.common.db.WebList
import info.anodsplace.weblists.common.db.WebSite
import kotlinx.coroutines.flow.*
import org.koin.core.logger.Logger

class MainViewModel(application: Application) : AndroidViewModel(application), AppViewModel {
    private val common = CommonAppViewModel(viewModelScope)
    override val prefs: AppPreferences
        get() = common.prefs
    override val yaml: Yaml
        get() = common.yaml
    override val log: Logger
        get() = common.log
    override var lastError: String
        get() = common.lastError
        set(value) { common.lastError = value }
    override val sites: MutableStateFlow<ContentState>
        get() = common.sites
    override val docSource: MutableStateFlow<HtmlDocument?>
        get() = common.docSource
    override var currentSections: ContentState.SiteSections?
        get() = common.currentSections
        set(value) { common.currentSections = value }
    override val documentRequest: MutableSharedFlow<DocumentRequest>
        get() = common.documentRequest
    override val documentRequestResult: MutableStateFlow<DocumentRequest.Result>
        get() = common.documentRequestResult

    override fun loadSites() = common.loadSites()
    override fun loadSite(siteId: Long): Flow<WebSite> = common.loadSite(siteId)
    override fun loadContent(siteId: Long): Flow<ContentState> = common.loadContent(siteId)
    override fun loadYaml(siteId: Long): Flow<String> = common.loadYaml(siteId)
    override fun updateDraft(site: WebSite, lists: List<WebList>, loadPreview: Boolean) {
        common.updateDraft(site, lists, loadPreview)
    }
    override fun export(title: String, content: String): Flow<Int> = common.export(title, content)
    override fun import(siteId: Long): Flow<Int> = common.import(siteId)
}