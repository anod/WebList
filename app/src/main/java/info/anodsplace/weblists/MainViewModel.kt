package info.anodsplace.weblists

import android.app.Application
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import info.anodsplace.weblists.rule.Store
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

sealed class ContentState {
    object Loading: ContentState()
    class Ready(val title: String, val list: List<AnnotatedString>): ContentState()
    class Error(val message: String): ContentState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val store = Store()
    private val _contentChanged: MutableStateFlow<ContentState> = MutableStateFlow(ContentState.Loading)
    val contentChanged: Flow<ContentState> = _contentChanged

    fun load() {
        viewModelScope.launch {
            try {
                val webRule = store.load(0)
                val doc = withContext(Dispatchers.IO) { Jsoup.connect(webRule.url).get() }
                val list = mutableListOf<AnnotatedString>()
                for (rule in webRule.rules) {
                    val elements = doc.select(rule.cssQuery)
                    list.addAll(
                        rule.apply(elements)
                    )
                }
                _contentChanged.emit(ContentState.Ready(webRule.title, list))
            } catch (e: Exception) {
                _contentChanged.emit(ContentState.Error(e.message ?: "Unexpected error"))
            }
        }
    }
}