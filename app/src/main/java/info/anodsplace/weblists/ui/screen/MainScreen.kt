package info.anodsplace.weblists.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import info.anodsplace.weblists.ContentState
import info.anodsplace.weblists.MainViewModel
import info.anodsplace.weblists.ui.theme.WebListTheme

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = Screen.Catalog.route) {
        composable(Screen.Empty) { EmptyContent() }
        composable(Screen.Error()) { backStackEntry ->
            ErrorContent(backStackEntry.arguments?.getString("message") ?: "Unexpected error")
        }
        composable(Screen.Catalog) {
            val state = viewModel.sites.collectAsState(initial = ContentState.Loading)
            when (val c = state.value) {
                is ContentState.Error -> navController.navigate(Screen.Error(c.message).route)
                is ContentState.Catalog -> CatalogContent(c.sites) {
                    navController.navigate(Screen.Site(it).route)
                }
                is ContentState.Loading -> {
                    viewModel.loadSites()
                    LoadingCatalog()
                }
                else -> navController.navigate(Screen.Error("Unknown state $c").route)
            }
        }
        navigation(startDestination = Screen.Site().template, route = "sites") {
            composable(Screen.Site()) { backStackEntry ->
                val siteId = backStackEntry.arguments?.getLong("siteId") ?: 0L
                val siteState = remember { viewModel.loadSite(siteId) }
                    .collectAsState(initial = ContentState.Loading)
                when (val siteValue = siteState.value) {
                    is ContentState.Error -> {
                        viewModel.prefs.lastSiteId = -1
                        navController.navigate(Screen.Error(siteValue.message).route)
                    }
                    is ContentState.SiteDefinition -> {
                        viewModel.prefs.lastSiteId = siteId
                        SiteContent(siteValue.site, emptyList(), isLoading = true) {
                            onSiteAction(it, navController)
                        }
                    }
                    is ContentState.SiteSections -> {
                        SiteContent(siteValue.site, siteValue.sections, isLoading = false) {
                            onSiteAction(it, navController)
                        }
                    }
                    is ContentState.Loading -> LoadingCatalog()
                    else -> navController.navigate(Screen.Error("Unknown state $siteState").route)
                }
            }
            composable(Screen.Search()) { backStackEntry ->
                val siteValue = viewModel.currentSections
                if (siteValue != null) {
                    SiteSearch(siteValue.site, siteValue.sections) {

                    }
                } else {
                    navController.popBackStack()
                }
            }
        }
    }
}

fun onSiteAction(action: SiteAction, navController: NavHostController) {
    when (action) {
        is SiteAction.Catalog -> {
            navController.navigate(Screen.Catalog) {
                popUpTo = navController.graph.startDestination
                launchSingleTop = true
            }
        }
        is SiteAction.Search -> {
            navController.navigate(Screen.Search(action.siteId)) {

            }
        }
    }
}

@Composable
fun MainSurface(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    backgroundColor: Color = MaterialTheme.colors.background,
    content: @Composable () -> Unit
) {
    Surface(color = backgroundColor) {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            contentAlignment = contentAlignment
        ) {
            content()
        }
    }
}

@Composable
fun ErrorContent(message: String) {
    MainSurface(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = message, textAlign = TextAlign.Center)
    }
}

@Composable
fun EmptyContent() {
    MainSurface(
        modifier = Modifier.padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No lists were found",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
            Button(onClick = { }) {
                Text(text = "Add new", textAlign = TextAlign.Center)
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EmptyPreview() {
    WebListTheme {
        EmptyContent()
    }
}