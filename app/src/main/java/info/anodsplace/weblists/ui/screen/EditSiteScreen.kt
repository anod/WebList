package info.anodsplace.weblists.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.weblists.MainViewModel
import info.anodsplace.weblists.extensions.isValidUrl
import info.anodsplace.weblists.rules.WebList
import info.anodsplace.weblists.rules.WebSite
import info.anodsplace.weblists.rules.WebSiteLists
import info.anodsplace.weblists.ui.theme.WebListTheme
import org.jsoup.nodes.Document

@Composable
fun EditSiteScreen(siteId: Long, viewModel: MainViewModel) {
    val editSiteState = viewModel.draftSite.collectAsState()
    val documentState = viewModel.docSource.collectAsState()
    EditSiteLists(
        editSiteState,
        documentState
    ) { site, lists, loadPreview ->
        viewModel.updateDraft(site, lists, loadPreview)
    }

    viewModel.loadDraft(siteId)
}

@Composable
fun EditSiteLists(
    webSiteLists: State<WebSiteLists>,
    documentState: State<Document?> = mutableStateOf(null),
    onChange: (site: WebSite, lists: List<WebList>, loadDoc: Boolean) -> Unit = { _, _, _ -> }) {
    val site = webSiteLists.value.site
    val lists = webSiteLists.value.lists
    val editSite = remember { mutableStateOf(site.url.isEmpty()) }
    val currentUrl= remember { mutableStateOf<String?>(null) }
    MainSurface(
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(4.dp),
            color = MaterialTheme.colors.primaryVariant,
            shape = RoundedCornerShape(8.dp)
        ) {
            if (editSite.value) {
                EditSite(
                    site = site,
                    onChange = { site -> onChange(site, lists, false) },
                    loadPreview = { site -> onChange(site, lists, true) }
                ) {
                    if (site.url.isValidUrl() && currentUrl.value != site.url) {
                        DocumentPreview(documentState)
                        currentUrl.value = site.url
                    }
                }
            } else {
                PreviewSite(site = site) {
                    if (site.url.isValidUrl() && currentUrl.value != site.url) {
                        DocumentPreview(documentState)
                        currentUrl.value = site.url
                    }
                }
            }
        }
    }
}

@Composable
fun EditSite(site: WebSite,
             onChange: (site: WebSite) -> Unit = {},
             loadPreview: (site: WebSite) -> Unit = {},
             content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val urlValid = remember { mutableStateOf(site.url.isValidUrl()) }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = site.url,
            onValueChange = { newUrl ->
                onChange(site.copy(url = newUrl))
            },
            isError = urlValid.value && site.url.isNotEmpty(),
            placeholder = { Text(text = "Url") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri
            )
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = site.title,
            onValueChange = { newTitle ->
                onChange(site.copy(url = newTitle))
            },
            placeholder = { Text(text = "Title") },
            singleLine = true
        )
        Button(onClick = { loadPreview(site) }) {
            Text(text = "Preview HTML")
        }
        content()
    }
}

@Composable
fun PreviewSite(site: WebSite, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = site.url,
            maxLines = 1
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = site.title,
            style = MaterialTheme.typography.h6,
            maxLines = 1
        )
        content()
    }
}

@Composable
fun DocumentPreview(document: State<Document?> = mutableStateOf(null)) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        value = document.value?.toString() ?: "",
        onValueChange = { },
        singleLine = false,
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditSiteListsPreview() {
    WebListTheme {
        EditSiteLists(mutableStateOf(WebSiteLists(
            WebSite(0, "http://sample1", "Sample 1"),
            listOf()
        )))
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditSitePreview() {
    WebListTheme {
        EditSiteLists(mutableStateOf(WebSiteLists(
            WebSite(0, "", ""),
            listOf()
        )))
    }
}