package info.anodsplace.weblists.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.*
import info.anodsplace.weblists.asListHeader
import info.anodsplace.weblists.common.AppViewModel
import info.anodsplace.weblists.common.ContentState
import info.anodsplace.weblists.common.StringProvider
import info.anodsplace.weblists.common.db.WebSection
import info.anodsplace.weblists.common.db.WebSite
import info.anodsplace.weblists.common.screen.*
import info.anodsplace.weblists.common.theme.WebListTheme

@Composable
fun MainScreen(viewModel: AppViewModel, strings: StringProvider) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = Screen.Catalog.route) {
        composable(Screen.Empty) { EmptyContent(strings) }
        composable(Screen.Error) {
            val message = if (viewModel.lastError.isEmpty()) "Unexpected error" else viewModel.lastError
            viewModel.log.error("error screen $message")
            ErrorContent(message)
        }
        composable(Screen.Catalog) {
            val state = viewModel.sites.collectAsState(initial = ContentState.Loading)
            when (val c = state.value) {
                is ContentState.Error -> {
                    viewModel.lastError = c.message
                    navController.navigate(Screen.Error)
                }
                is ContentState.Catalog -> CatalogContent(c.sites, strings = strings) {
                    navController.navigate(it)
                }
                is ContentState.Loading -> {
                    viewModel.loadSites()
                    LoadingCatalog()
                }
                else -> {
                    viewModel.lastError = "Unknown state $c"
                    navController.navigate(Screen.Error)
                }
            }
        }
        composable(Screen.EditSite()) { backStackEntry ->
            val siteId = backStackEntry.arguments?.getLong("siteId") ?: 0L
            EditSiteScreen(siteId, viewModel = viewModel, strings = strings) {
                navController.navigate(it)
            }
        }
        navigation(startDestination = Screen.Site().template, route = "sites") {
            composable(Screen.Site()) { backStackEntry ->
                val siteId = backStackEntry.arguments?.getLong("siteId") ?: 0L
                val siteState = remember { viewModel.loadSite(siteId) }.collectAsState(initial = ContentState.Loading)
                when (val siteValue = siteState.value) {
                    is ContentState.Error -> {
                        viewModel.prefs.lastSiteId = -1
                        viewModel.lastError = siteValue.message
                        navController.navigate(Screen.Error)
                    }
                    is ContentState.SiteDefinition -> {
                        viewModel.prefs.lastSiteId = siteId
                        SiteContent(siteValue.site, emptyList(), strings = strings, isLoading = true) {
                            navController.navigate(it)
                        }
                    }
                    is ContentState.SiteSections -> {
                        SiteContent(siteValue.site, siteValue.sections, strings = strings, isLoading = false, addBackPressHandler = true) {
                            navController.navigate(it)
                        }
                    }
                    is ContentState.Loading -> LoadingCatalog()
                    else -> {
                        viewModel.lastError = "Unknown state $siteState"
                        navController.navigate(Screen.Error)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EmptyPreview() {
    WebListTheme {
        EmptyContent(strings = StringProvider.Default())
    }
}


@Preview(
    showBackground = true,
    backgroundColor = android.graphics.Color.MAGENTA.toLong(),
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SiteSearchPreview() {
    WebListTheme {
        SiteSearch(
            sections = listOf(
                WebSection(
                    true, listOf(
                        AnnotatedString("Banana").asListHeader(),
                        AnnotatedString("Blackcurrant"),
                        AnnotatedString("Blueberry").asListHeader(),
                        AnnotatedString("Chili pepper"),
                        AnnotatedString("Cranberry"),
                    )
                ),
                WebSection(
                    false, listOf(
                        AnnotatedString("Eggplant"),
                        AnnotatedString("Gooseberry").asListHeader(),
                        AnnotatedString("Grape"),
                        AnnotatedString("Guava"),
                        AnnotatedString("Kiwifruit").asListHeader(),
                        AnnotatedString("Lucuma"),
                        AnnotatedString("Pomegranate"),
                        AnnotatedString("Redcurrant"),
                        AnnotatedString("Tomato")
                    )
                )
            ),
            searchValue = mutableStateOf("rry"),
            strings = StringProvider.Default()
        ) { }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = android.graphics.Color.MAGENTA.toLong(),
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun SiteSearchLightPreview() {
    WebListTheme {
        SiteSearch(
            sections = listOf(
                WebSection(
                    true, listOf(
                        AnnotatedString("Banana").asListHeader(),
                        AnnotatedString("Blackcurrant"),
                        AnnotatedString("Blueberry").asListHeader(),
                        AnnotatedString("Chili pepper"),
                        AnnotatedString("Cranberry"),
                    )
                ),
                WebSection(
                    false, listOf(
                        AnnotatedString("Eggplant"),
                        AnnotatedString("Gooseberry").asListHeader(),
                        AnnotatedString("Grape"),
                        AnnotatedString("Guava"),
                        AnnotatedString("Kiwifruit").asListHeader(),
                        AnnotatedString("Lucuma"),
                        AnnotatedString("Pomegranate"),
                        AnnotatedString("Redcurrant"),
                        AnnotatedString("Tomato")
                    )
                )
            ),
            searchValue = mutableStateOf("rry"),
            strings = StringProvider.Default()
        ) { }
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DefaultPreview() {
    WebListTheme {
        SiteContent(
            WebSite(0, "url", "Android"),
            listOf(
                WebSection(true, listOf(AnnotatedString("Banana"), AnnotatedString("Kiwi"))),
                WebSection(false, listOf(AnnotatedString("Banana"), AnnotatedString("Kiwi")))
            ),
            isLoading = false,
            strings = StringProvider.Default()
        ) { }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun DefaultLightPreview() {
    WebListTheme {
        SiteContent(
            WebSite(0, "url", "Android"),
            listOf(
                WebSection(true, listOf(AnnotatedString("Banana"), AnnotatedString("Kiwi"))),
                WebSection(false, listOf(AnnotatedString("Banana"), AnnotatedString("Kiwi")))
            ),
            isLoading = false,
            strings = StringProvider.Default()
        ) { }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SearchPreview() {
    WebListTheme {
        SiteContent(
            WebSite(0, "url", "Android"),
            listOf(
                WebSection(true, listOf(AnnotatedString("Banana"), AnnotatedString("Kiwi"))),
                WebSection(false, listOf(AnnotatedString("Banana"), AnnotatedString("Kiwi")))
            ),
            isLoading = false,
            initialSearch = true,
            strings = StringProvider.Default()
        ) { }
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ListPreview() {
    WebListTheme {
        CatalogContent(
            listOf(
                WebSite(0, "http://sample1", "Sample 1"),
                WebSite(1, "http://sample2", "Sample 2")
            ),
            strings = StringProvider.Default()
        ) { }
    }
}