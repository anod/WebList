package info.anodsplace.weblists.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.weblists.R
import info.anodsplace.weblists.findAll
import info.anodsplace.weblists.rules.WebSection
import info.anodsplace.weblists.rules.WebSite
import info.anodsplace.weblists.ui.BackPressHandler
import info.anodsplace.weblists.ui.theme.WebListTheme
import kotlinx.coroutines.launch
import java.lang.Integer.min

@Composable
fun SiteTopBar(site: WebSite, navigate: (Screen) -> Unit) {

    TopAppBar(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(4.dp)),
        title = { Text(text = site.title) },
        navigationIcon = {
            IconButton(onClick = { navigate(Screen.Catalog) }) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back_to_catalog))
            }
        },
        backgroundColor = MaterialTheme.colors.primary,
        actions = {
            IconButton(onClick = { navigate(Screen.SearchSite(site.id)) }) {
                Icon(imageVector = Icons.Filled.Search, contentDescription = stringResource(R.string.search))
            }
            IconButton(onClick = { navigate(Screen.EditSite(site.id))}) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = stringResource(R.string.search))
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SiteContent(
    site: WebSite,
    sections: List<WebSection>,
    isLoading: Boolean,
    initialSearch: Boolean = false,
    addBackPressHandler: Boolean = false,
    navigate: (Screen) -> Unit = {}
) {
    MainSurface {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            val searchValue = remember { mutableStateOf("") }
            var showSearch by remember { mutableStateOf(initialSearch) }
            val columnState = rememberLazyListState()
            val rowState = rememberLazyListState()
            val coroutineScope = rememberCoroutineScope()

            if (addBackPressHandler) {
                BackPressHandler(
                    onBackPressed = {
                        showSearch = false
                    },
                    enabled = showSearch
                )
            }

            Column {
                if (showSearch) {
                    SiteSearch(sections, searchValue = searchValue) {
                        when(it) {
                            is SearchAction.ScrollTo -> {
                                coroutineScope.launch {
                                    if (it.pos.second == 0) {
                                        columnState.animateScrollToItem(it.pos.first)
                                    } else {
                                        columnState.scrollToItem(it.pos.first)
                                        rowState.animateScrollToItem(it.pos.second)
                                    }
                                }
                                if (it.close) {
                                    showSearch = false
                                }
                            }
                            is SearchAction.Close -> {
                                showSearch = false
                            }
                        }
                    }
                } else {
                    SiteTopBar(site) { siteAction ->
                        when (siteAction) {
                            is Screen.SearchSite -> {
                                showSearch = true
                            }
                            else -> {
                                navigate(siteAction)
                            }
                        }
                    }
                }

                SiteSections(
                    sections = sections,
                    columnState = columnState,
                    rowState = rowState,
                    searchValue = searchValue
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SiteSections(
    sections: List<WebSection>,
    columnState: LazyListState = rememberLazyListState(),
    rowState: LazyListState = rememberLazyListState(),
    searchValue: MutableState<String> = mutableStateOf(""),
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        state = columnState,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (section in sections) {
            if (section.isHorizontal) {
                item {
                    LazyRow(
                        state = rowState
                    ) {
                        items(section.list.size) { index ->
                            Text(
                                text = matchSubstring(
                                    section.list[index],
                                    searchValue.value,
                                    MaterialTheme.colors.primaryVariant
                                ),
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
                        text = matchSubstring(
                            section.list[index],
                            searchValue.value,
                            MaterialTheme.colors.primaryVariant
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.body1
                    )
                }
            }
        }
    }
}

fun matchSubstring(
    annotatedString: AnnotatedString,
    value: String,
    backgroundColor: Color
): AnnotatedString {
    if (value.isEmpty()) {
        return annotatedString
    }

    val found = annotatedString.findAll(
        string = value,
        ignoreCase = true
    )

    if (found.isEmpty()) {
        return annotatedString
    }

    return with(AnnotatedString.Builder(annotatedString)) {
        for (index in found) {
            addStyle(
                style = SpanStyle(background = backgroundColor),
                start = index,
                end = min(annotatedString.length, index + value.length)
            )
        }
        toAnnotatedString()
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
            isLoading = false
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
            isLoading = false
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
            initialSearch = true
        ) { }
    }
}
