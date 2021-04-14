package info.anodsplace.weblists.common

import androidx.compose.runtime.*
import info.anodsplace.weblists.common.ui.screen.*

@Composable
fun MainScreen(viewModel: AppViewModel, strings: StringProvider) {
    val navState: MutableState<Screen> = remember { mutableStateOf(Screen.Catalog) }

    when (val nav = navState.value) {
        is Screen.Empty -> EmptyContent(strings)
        is  Screen.Error -> {
            val message = if (viewModel.lastError.isEmpty()) "Unexpected error" else viewModel.lastError
            // Log.e("error", message)
            ErrorContent(message)
        }
        is Screen.Catalog -> {
            val state = viewModel.sites.collectAsState(initial = ContentState.Loading)
            when (val c = state.value) {
                is ContentState.Error -> {
                    viewModel.lastError = c.message
                    navState.value = Screen.Error
                }
                is ContentState.Catalog -> CatalogContent(c.sites, strings = strings) {
                    navState.value = it
                }
                is ContentState.Loading -> {
                    viewModel.loadSites()
                    LoadingCatalog()
                }
                else -> {
                    viewModel.lastError = "Unknown state $c"
                    navState.value = Screen.Error
                }
            }
        }
        is Screen.EditSite -> {
            val siteId = nav.siteId
            EditSiteScreen(siteId, viewModel = viewModel, strings = strings) {
                navState.value = it
            }
        }
        is Screen.Site -> {
            val siteId = nav.siteId
            val siteState = remember { viewModel.loadSite(siteId) }.collectAsState(initial = ContentState.Loading)
            when (val siteValue = siteState.value) {
                is ContentState.Error -> {
                    viewModel.prefs.lastSiteId = -1
                    viewModel.lastError = siteValue.message
                    navState.value = Screen.Error
                }
                is ContentState.SiteDefinition -> {
                    viewModel.prefs.lastSiteId = siteId
                    SiteContent(siteValue.site, emptyList(), strings = strings, isLoading = true) {
                        navState.value = it
                    }
                }
                is ContentState.SiteSections -> {
                    SiteContent(siteValue.site, siteValue.sections, strings = strings, isLoading = false, addBackPressHandler = true) {
                        navState.value = it
                    }
                }
                is ContentState.Loading -> LoadingCatalog()
                else -> {
                    viewModel.lastError = "Unknown state $siteState"
                    navState.value = Screen.Error
                }
            }
        }
    }
}