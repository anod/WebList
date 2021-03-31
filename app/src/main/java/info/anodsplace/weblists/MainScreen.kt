package info.anodsplace.weblists

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import info.anodsplace.weblists.rules.WebSection
import info.anodsplace.weblists.rules.WebSite
import info.anodsplace.weblists.ui.theme.WebListTheme

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = Screen.Catalog.route) {
        composable(Screen.Empty) { EmptyContent() }
        composable(Screen.Catalog) {
            val state = viewModel.sites.collectAsState(initial = ContentState.Loading)
            when (val c = state.value) {
                is ContentState.Error -> navController.navigate(Screen.Error(c.message).route)
                is ContentState.Catalog -> ListContent(c.sites) {
                    navController.navigate(Screen.Site(it).route)
                }
                is ContentState.Loading -> {
                    viewModel.loadSites()
                    LoadingCatalog()
                }
                else -> navController.navigate(Screen.Error("Unknown state $c").route)
            }
        }
        composable(Screen.Site(0L)) { backStackEntry ->
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
                    LoadingSiteContent(title = siteValue.site.title) {
                        navController.navigate(Screen.Catalog) {
                            popUpTo = navController.graph.startDestination
                            launchSingleTop = true
                        }
                    }
                }
                is ContentState.SiteSections -> {
                    SiteContent(siteValue.site.title, siteValue.sections) {
                        navController.navigate(Screen.Catalog) {
                            popUpTo = navController.graph.startDestination
                            launchSingleTop = true
                        }
                    }
                }
                is ContentState.Loading -> LoadingCatalog()
                else -> navController.navigate(Screen.Error("Unknown state $siteState").route)
            }
        }
        composable(Screen.Error()) { backStackEntry ->
            ErrorContent(
                backStackEntry.arguments?.getString("message") ?: "Unexpected error"
            )
        }
    }
}

@Composable
fun MainSurface(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable () -> Unit
) {
    Surface(color = MaterialTheme.colors.background) {
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
fun LoadingCatalog() {
    MainSurface {
        CircularProgressIndicator()
    }
}

@Composable
fun LoadingSiteContent(title: String, navigateToCatalog: () -> Unit) {
    MainSurface(
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            SiteTopBar(title, navigateToCatalog)
        }
        CircularProgressIndicator()

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListContent(sites: List<WebSite>, navigateToSite: (siteId: Long) -> Unit) {
    MainSurface {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
        ) {
            TopAppBar(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                title = { Text(text = "Sites") },
                backgroundColor = MaterialTheme.colors.primary
            )

            LazyVerticalGrid(
                cells = GridCells.Fixed(count = 2),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(sites.size) { index ->
                    val title = sites[index].title
                    Box {
                        Button(
                            onClick = { navigateToSite(sites[index].id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 72.dp)
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(),
                        ) {
                            Text(text = title, style = MaterialTheme.typography.body1)
                        }

                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = title,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        )
                    }

                }
            }
        }
    }
}

@Composable
fun SiteTopBar(title: String, navigateToCatalog: () -> Unit) {
    TopAppBar(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(4.dp)),
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = navigateToCatalog) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back to catalog")
            }
        },
        backgroundColor = MaterialTheme.colors.primary,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SiteContent(source: String, sections: List<WebSection>, navigateToCatalog: () -> Unit) {
    MainSurface {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            stickyHeader {
                SiteTopBar(source, navigateToCatalog)
            }

            for (section in sections) {
                if (section.isHorizontal) {
                    item {
                        LazyRow {
                            items(section.list.size) { index ->
                                Text(
                                    text = section.list[index],
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    ),
                                    style = MaterialTheme.typography.body1
                                )
                            }
                        }
                    }
                } else {
                    items(section.list.size) { index ->
                        Text(
                            text = section.list[index],
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            }
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
fun ListPreview() {
    WebListTheme {
        ListContent(
            listOf(
                WebSite(0, "http://sample1", "Sample 1"),
                WebSite(1, "http://sample2", "Sample 2")
            )
        ) { }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DefaultPreview() {
    WebListTheme {
        SiteContent(
            "Android", listOf(
                WebSection(true, listOf(AnnotatedString("Banana"), AnnotatedString("Kiwi"))),
                WebSection(false, listOf(AnnotatedString("Banana"), AnnotatedString("Kiwi")))
            )
        ) { }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LoadingSectionsPreview() {
    WebListTheme {
        LoadingSiteContent("Banana") { }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EmptyPreview() {
    WebListTheme {
        EmptyContent()
    }
}