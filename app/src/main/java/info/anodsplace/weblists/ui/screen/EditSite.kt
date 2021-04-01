package info.anodsplace.weblists.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.weblists.MainViewModel
import info.anodsplace.weblists.rules.WebSite
import info.anodsplace.weblists.rules.WebSiteLists
import info.anodsplace.weblists.ui.theme.WebListTheme

@Composable
fun EditSiteScreen(viewModel: MainViewModel) {
    val coroutineScope = rememberCoroutineScope()

    // viewModel.
}

@Composable
fun EditSiteLists(webSiteLists: WebSiteLists) {
    val site = webSiteLists.site
    val lists = webSiteLists.lists
    val editSite = remember { mutableStateOf(site.url.isEmpty()) }
    MainSurface(
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(4.dp),
            color = MaterialTheme.colors.primary,
            shape = RoundedCornerShape(8.dp)
        ) {
            if (editSite.value) {
                EditSite(site = site)
            } else {
                PreviewSite(site = site)
            }
        }
    }
}

@Composable
fun EditSite(site: WebSite) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = site.url,
            onValueChange = { newUrl -> },
            placeholder = { Text(text = "Url") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri
            )
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = site.title,
            onValueChange = { newTitle -> },
            placeholder = { Text(text = "Title") },
            singleLine = true
        )
    }
}

@Composable
fun DocumentPreview() {

}

@Composable
fun PreviewSite(site: WebSite) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
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
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditSiteListsPreview() {
    WebListTheme {
        EditSiteLists(WebSiteLists(
            WebSite(0, "http://sample1", "Sample 1"),
            listOf()
        ))
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditSitePreview() {
    WebListTheme {
        EditSiteLists(WebSiteLists(
            WebSite(0, "", ""),
            listOf()
        ))
    }
}