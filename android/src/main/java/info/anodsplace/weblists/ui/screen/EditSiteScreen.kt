package info.anodsplace.weblists.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import info.anodsplace.weblists.MainViewModel
import info.anodsplace.weblists.R
import info.anodsplace.weblists.rules.WebList
import info.anodsplace.weblists.rules.WebSite
import info.anodsplace.weblists.rules.WebSiteLists
import info.anodsplace.weblists.samples.MatchTv
import info.anodsplace.weblists.ui.theme.WebListTheme
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import org.jsoup.nodes.Document

@Composable
fun EditSiteScreen(siteId: Long, viewModel: MainViewModel, navigate: (Screen) -> Unit) {
    val editSiteState = remember { viewModel.loadDraft(siteId) }.collectAsState()
    val document by viewModel.docSource.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var backupMessage by remember { mutableStateOf<String?>(null) }
    EditSiteLists(
        siteId,
        editSiteState,
        viewModel.yaml,
        document,
        { site, lists, loadPreview ->
            viewModel.updateDraft(site, lists, loadPreview)
        },
    ) {
        when (it) {
            is Screen.Export -> {
                coroutineScope.launch {
                    viewModel.export(it.siteId, it.content).collect {
                        backupMessage = "Finished code $it"
                    }
                }
            }
            is Screen.Import -> {

            }
            else -> navigate(it)
        }
    }
    if (backupMessage != null) {
        Snackbar {
            Text(text = backupMessage!!)
        }
    }
}

@Composable
fun EditSiteLists(
    siteId: Long,
    webSiteLists: State<WebSiteLists?>,
    yaml: Yaml,
    document: Document? = null,
    onChange: (site: WebSite, lists: List<WebList>, loadDoc: Boolean) -> Unit = { _, _, _ -> },
    navigate: (Screen) -> Unit = { }
) {
    MainSurface {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
        ) {
            var yamlValue by remember {
                mutableStateOf(yaml.encodeToString(webSiteLists.value))
            }
            EditTopBar(
                siteId = siteId,
                title = webSiteLists.value?.site?.title ?: "",
            ) {
                when (it) {
                    is Screen.Export -> {
                        navigate(Screen.Export(siteId, yamlValue))
                    }
                    is Screen.Import -> {

                    }
                    else -> navigate(it)
                }
            }

            if (webSiteLists.value != null) {
                OutlinedTextField(
                    modifier = Modifier.padding(8.dp),
                    value = yamlValue,
                    onValueChange = { newText -> yamlValue = newText },
                    readOnly = true,
                    textStyle = MaterialTheme.typography.body2,
                    visualTransformation = Highlight
                )
            }
        }
    }
}

val Highlight: VisualTransformation = VisualTransformation { text ->
    TransformedText(text, OffsetMapping.Identity)
}

@Composable
fun EditTopBar(siteId: Long, title: String, navigate: (Screen) -> Unit) {
    TopAppBar(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(4.dp)),
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = { if (siteId == 0L) navigate(Screen.Catalog) else navigate(Screen.Site(siteId)) }) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back_to_catalog))
            }
        },
        backgroundColor = MaterialTheme.colors.primary,
        actions = {
            IconButton(onClick = { navigate(Screen.Export(siteId)) }) {
                Icon(imageVector = Icons.Outlined.CloudUpload, contentDescription = stringResource(R.string.search))
            }
            IconButton(onClick = { navigate(Screen.Import(siteId))}) {
                Icon(imageVector = Icons.Outlined.CloudDownload, contentDescription = stringResource(R.string.search))
            }
        }
    )
}

@Composable
fun DocumentPreview(document: Document? = null) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        value = document?.body()?.toString() ?: "",
        onValueChange = { }
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditSiteListsPreview() {
    WebListTheme {
        EditSiteLists(
            siteId = 0,
            webSiteLists = mutableStateOf(WebSiteLists(
                WebSite(0, "http://sample1", "Sample 1"),
                MatchTv.sample(0)
            )),
            yaml = Yaml(configuration = YamlConfiguration(polymorphismStyle = PolymorphismStyle.Property)),
            onChange = { _, _, _ -> }
        ) { }
    }
}