package info.anodsplace.weblists.common

import HtmlDocument
import com.charleskorn.kaml.Yaml
import info.anodsplace.weblists.common.db.WebList
import info.anodsplace.weblists.common.db.WebSection
import info.anodsplace.weblists.common.db.WebSite
import info.anodsplace.weblists.common.db.WebSiteLists
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

sealed class ContentState {
    object Loading: ContentState()
    class Catalog(val sites: List<WebSite>): ContentState()
    class SiteDefinition(val site: WebSite): ContentState()
    class SiteSections(val site: WebSite, val sections: List<WebSection>): ContentState()
    class Error(val message: String): ContentState()
}

interface AppViewModel {
    var lastError: String
    val prefs: AppPreferences
    val yaml: Yaml
    val sites: MutableStateFlow<ContentState>
    val docSource: MutableStateFlow<HtmlDocument?>
    var currentSections: ContentState.SiteSections?

    fun loadSites()
    fun loadSite(siteId: Long): Flow<ContentState>
    fun updateDraft(site: WebSite, lists: List<WebList>, loadPreview: Boolean)
    fun loadDraft(siteId: Long): MutableStateFlow<WebSiteLists?>
    fun export(siteId: Long, content: String): Flow<Int>
}