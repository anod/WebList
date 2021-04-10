package info.anodsplace.weblists

import HtmlDocument
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.charleskorn.kaml.Yaml
import info.anodsplace.weblists.common.AppPreferences
import info.anodsplace.weblists.common.AppViewModel
import info.anodsplace.weblists.common.CommonAppViewModel
import info.anodsplace.weblists.common.ContentState
import info.anodsplace.weblists.common.db.WebList
import info.anodsplace.weblists.common.db.WebSite
import info.anodsplace.weblists.common.db.WebSiteLists
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application), AppViewModel {
    private val common = CommonAppViewModel(viewModelScope)
    override var lastError: String
        get() = common.lastError
        set(value) { common.lastError = value }
    override val prefs: AppPreferences
        get() = common.prefs
    override val yaml: Yaml
        get() = common.yaml
    override val sites: MutableStateFlow<ContentState>
        get() = common.sites
    override val docSource: MutableStateFlow<HtmlDocument?>
        get() = common.docSource
    override var currentSections: ContentState.SiteSections?
        get() = common.currentSections
        set(value) { common.currentSections = value }

    override fun loadSites() = common.loadSites()

    override fun loadSite(siteId: Long): Flow<ContentState> = common.loadSite(siteId)

    override fun updateDraft(site: WebSite, lists: List<WebList>, loadPreview: Boolean) {
        common.updateDraft(site, lists, loadPreview)
    }

    override fun loadDraft(siteId: Long): MutableStateFlow<WebSiteLists?> {
        return common.loadDraft(siteId)
    }

    val createDocument = MutableSharedFlow<String>()
    override fun export(siteId: Long, content: String): Flow<Int> {
        return common.export(siteId, content).onStart {
            createDocument.emit("export-$siteId.yaml")
        }
    }

    override val onExportUri: MutableSharedFlow<String?>
        get() = common.onExportUri
    fun exportTo(destUri: String?) {
        viewModelScope.launch {
            onExportUri.emit(destUri ?: "")
        }
    }

    val openDocument = MutableSharedFlow<Boolean>()
    private val onImportUri = MutableSharedFlow<String?>()
    fun importFrom(destUri: String?) {
        onImportUri.tryEmit(destUri)
    }
}