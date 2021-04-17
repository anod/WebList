package info.anodsplace.weblists.common.screen

import info.anodsplace.weblists.common.HtmlDocument
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import info.anodsplace.weblists.common.AppViewModel
import info.anodsplace.weblists.common.StringProvider
import info.anodsplace.weblists.common.db.WebSite
import info.anodsplace.weblists.common.db.WebSiteLists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString

@Composable
fun EditSiteScreen(siteId: Long, viewModel: AppViewModel, strings: StringProvider, navigate: (Screen) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val site by remember { viewModel.loadSite(siteId) }.collectAsState(initial = WebSite.empty)
    val yamlValue by remember { viewModel.loadYaml(siteId) }.collectAsState(initial = "")
    //val document by viewModel.docSource.collectAsState()
    var snackBarMessage by remember { mutableStateOf<String?>(null) }
    Scaffold(
        topBar = {
            EditTopBar(
                siteId = siteId,
                title = strings.edit,
                strings = strings
            ) {
                when (it) {
                    is Screen.Export -> {
                        coroutineScope.launch {
                            val code = viewModel.export(site.title, yamlValue)
                                .onStart {
                                    viewModel.log.debug("Export started")
                                }
                                .onCompletion {
                                    viewModel.log.debug("Export completed")
                                }.single()
                            snackBarMessage = "Finished with code $code"
                        }
                    }
                    is Screen.Import -> {
                        coroutineScope.launch {
                            val code = viewModel.import(it.siteId)
                                .onStart {
                                    viewModel.log.debug("Import started")
                                }
                                .onCompletion {
                                    viewModel.log.debug("Import completed")
                                }
                                .single()
                            snackBarMessage = "Finished with code $code"
                        }
                    }
                    else -> navigate(it)
                }
            }
        },
        snackbarHost = {
            if (snackBarMessage != null) {
                coroutineScope.launch {
                    it.showSnackbar(message = snackBarMessage!!)
                    snackBarMessage = null
                }
            }
        }
    ) {
        if (yamlValue.isEmpty()) {
            LoadingCatalog()
        } else {
            EditSiteLists(
                yamlValue = yamlValue
            ) { newYaml ->
                coroutineScope.launch {
                    try {
                        val newWebLists: WebSiteLists = withContext(Dispatchers.Default) {
                            viewModel.yaml.decodeFromString(newYaml)
                        }
                        viewModel.updateDraft(newWebLists.site, newWebLists.lists, false)
                    } catch (e: Exception) {
                        snackBarMessage = "Parse error: ${e.message}"
                    }
                }
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
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(8.dp),
            value = yamlValue,
            onValueChange = { newText -> onChange(newText) },
            readOnly = true,
            textStyle = MaterialTheme.typography.body2,
            visualTransformation = Highlight
        )
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
            IconButton(onClick = { if (siteId == 0L) navigate(Screen.Catalog) else navigate(
                Screen.Site(
                    siteId
                )
            ) }) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = strings.backToCatalog)
            }
        },
        backgroundColor = MaterialTheme.colors.primary,
        actions = {
            IconButton(onClick = { navigate(Screen.Import(siteId)) }) {
                Icon(imageVector = Icons.Outlined.FolderOpen, contentDescription = strings.export)
            }
            IconButton(onClick = { navigate(Screen.Export(siteId))}) {
                Icon(imageVector = Icons.Outlined.Download, contentDescription = strings.import)
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