package info.anodsplace.weblists.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.weblists.rules.AnnotationAttributes
import info.anodsplace.weblists.rules.WebSection
import info.anodsplace.weblists.rules.WebSite
import info.anodsplace.weblists.ui.theme.WebListTheme

sealed class SiteAction {
    object Catalog: SiteAction()
    class Search(val siteId: Long): SiteAction()
}

@Composable
fun SiteTopBar(site: WebSite, action: (SiteAction) -> Unit) {
    TopAppBar(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(4.dp)),
        title = { Text(text = site.title) },
        navigationIcon = {
            IconButton(onClick = { action(SiteAction.Catalog) }) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back to catalog")
            }
        },
        backgroundColor = MaterialTheme.colors.primary,
        actions = {
            IconButton(onClick = { action(SiteAction.Search(site.id)) }) {
                Icon(imageVector = Icons.Filled.Search, contentDescription = "Search")
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SiteContent(site: WebSite,
                sections: List<WebSection>,
                isLoading: Boolean,
                action: (SiteAction) -> Unit = {}) {
    MainSurface {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            SiteSections(site, sections, action)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SiteSections(site: WebSite, sections: List<WebSection>, action: (SiteAction) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        stickyHeader {
            SiteTopBar(site, action)
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
            false
        ) { }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LoadingSectionsPreview() {
    WebListTheme {
        SiteContent(
            WebSite(0, "url", "Android"),
            emptyList(),
            true
        ) { }
    }
}
