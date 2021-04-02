package info.anodsplace.weblists.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
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
    val document by viewModel.docSource.collectAsState()
    EditSiteLists(
        editSiteState,
        document
    ) { site, lists, loadPreview ->
        viewModel.updateDraft(site, lists, loadPreview)
    }

    viewModel.loadDraft(siteId)
}

@Composable
fun EditSiteLists(
    webSiteLists: State<WebSiteLists>,
    document: Document? = null,
    onChange: (site: WebSite, lists: List<WebList>, loadDoc: Boolean) -> Unit = { _, _, _ -> }) {
    val site = webSiteLists.value.site
    val lists = webSiteLists.value.lists
    val editSite by remember { mutableStateOf(site.url.isEmpty()) }
    MainSurface(
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(4.dp),
            color = MaterialTheme.colors.surface,
            shape = RoundedCornerShape(8.dp)
        ) {
            if (editSite) {
                EditSite(
                    site = site,
                    onChange = { site, loadPreview -> onChange(site, lists, loadPreview) },
                ) {
                    DocumentPreview(document)
                }
            } else {
                PreviewSite(site = site) {
                    DocumentPreview(document)
                }
            }
        }
    }
}

@Composable
fun EditSite(site: WebSite,
             onChange: (site: WebSite, loadPreview: Boolean) -> Unit = { _, _ -> },
             content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = site.url,
            onValueChange = { newUrl ->
                onChange(site.copy(url = newUrl), false)
            },
            isError = !site.url.isValidUrl() && site.url.isNotEmpty(),
            label = { Text(text = "Url") },
            placeholder = { Text(text = "https://example.com") },
            keyboardOptions = KeyboardOptions(
                autoCorrect = false,
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onAny = {
                onChange(site, true)
            })
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = site.title,
            onValueChange = { newTitle ->
                onChange(site.copy(title = newTitle), false)
            },
            label = { Text(text = "Title") },
            singleLine = true,
            keyboardActions = KeyboardActions(onAny = {
                onChange(site, true)
            })
        )
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
fun DocumentPreview(document: Document? = null) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        value = document?.body()?.toString() ?: "",
        onValueChange = { }
    )
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