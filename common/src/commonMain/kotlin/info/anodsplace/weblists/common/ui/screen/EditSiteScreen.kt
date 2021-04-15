package info.anodsplace.weblists.common.ui.screen

import HtmlDocument
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.charleskorn.kaml.Yaml
import info.anodsplace.weblists.common.AppViewModel
import info.anodsplace.weblists.common.StringProvider
import info.anodsplace.weblists.common.db.WebList
import info.anodsplace.weblists.common.db.WebSite
import info.anodsplace.weblists.common.db.WebSiteLists
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

@Composable
fun EditSiteScreen(siteId: Long, viewModel: AppViewModel, strings: StringProvider, navigate: (Screen) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val editSiteState by remember { viewModel.loadDraft(siteId) }.collectAsState()
    //val document by viewModel.docSource.collectAsState()
    var backupMessage by remember { mutableStateOf<String?>(null) }
    var yamlValue by remember { mutableStateOf(viewModel.yaml.encodeToString(editSiteState)) }
    Scaffold(
        topBar = {
            EditTopBar(
                siteId = siteId,
                title = editSiteState?.site?.title ?: "",
                strings = strings
            ) {
                when (it) {
                    is Screen.Export -> {
                        coroutineScope.launch {
                            viewModel.export(it.siteId, yamlValue).collect { code ->
                                backupMessage = "Finished with code $code"
                            }
                        }
                        ///navigate(Screen.Export(siteId, yamlValue))
                    }
                    is Screen.Import -> {

                    }
                    else -> navigate(it)
                }
            }
        },
        snackbarHost = {
            if (backupMessage != null) {
                coroutineScope.launch {
                    it.showSnackbar(message = backupMessage!!)
                }
            }
        }
    ) {
        if (editSiteState == null) {
            LoadingCatalog()
        } else {
            EditSiteLists(
                yamlValue = yamlValue
            ) { newYaml ->
                    yamlValue = newYaml
            }
        }
    }
}

@Composable
fun EditSiteLists(
    yamlValue: String,
    onChange: (newYaml: String) -> Unit = { },
) {
    MainSurface {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
        ) {
            OutlinedTextField(
                modifier = Modifier.padding(8.dp),
                value = yamlValue,
                onValueChange = { newText -> onChange(newText) },
                readOnly = true,
                textStyle = MaterialTheme.typography.body2,
                visualTransformation = Highlight
            )
        }
    }
}

val Highlight: VisualTransformation = VisualTransformation { text ->
    TransformedText(text, OffsetMapping.Identity)
}

@Composable
fun EditTopBar(siteId: Long, title: String, strings: StringProvider, navigate: (Screen) -> Unit) {
    TopAppBar(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(4.dp)),
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = { if (siteId == 0L) navigate(Screen.Catalog) else navigate(Screen.Site(siteId)) }) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = strings.backToCatalog)
            }
        },
        backgroundColor = MaterialTheme.colors.primary,
        actions = {
            IconButton(onClick = { navigate(Screen.Export(siteId)) }) {
                Icon(imageVector = Icons.Outlined.CloudUpload, contentDescription = strings.export)
            }
            IconButton(onClick = { navigate(Screen.Import(siteId))}) {
                Icon(imageVector = Icons.Outlined.CloudDownload, contentDescription = strings.import)
            }
        }
    )
}

@Composable
fun DocumentPreview(document: HtmlDocument? = null) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        value = document?.body()?.toString() ?: "",
        onValueChange = { }
    )
}