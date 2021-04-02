package info.anodsplace.weblists.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.weblists.R
import info.anodsplace.weblists.asListHeader
import info.anodsplace.weblists.findAll
import info.anodsplace.weblists.rules.AnnotationAttributes
import info.anodsplace.weblists.rules.WebSection
import info.anodsplace.weblists.ui.theme.WebListTheme

typealias ScrollToPosition = Triple<Int, Int, AnnotatedString>

sealed class SearchAction {
    class ScrollTo(val pos: ScrollToPosition, val close: Boolean) : SearchAction()
    object Close : SearchAction()
}

@Composable
fun SiteSearch(
    sections: List<WebSection>,
    searchValue: MutableState<String> = mutableStateOf(""),
    action: (SearchAction) -> Unit = { }
) {
    val headers = headers(sections)
    val searchResults = remember { mutableStateOf(emptyList<ScrollToPosition>()) }
    val currentIndex = remember { mutableStateOf(0) }
    val focusRequester = FocusRequester()

    DisposableEffect(Unit) {
        focusRequester.requestFocus()
        onDispose { }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.primarySurface)
            .padding(bottom = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(4.dp)),
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                value = searchValue.value,
                onValueChange = { value ->
                    searchValue.value = value
                    searchResults.value = search(searchValue, sections)
                    if (searchResults.value.isNotEmpty()) {
                        action(SearchAction.ScrollTo(
                            searchResults.value.first(),
                            false
                        ))
                    }
                },
                placeholder = { Text(text = stringResource(R.string.find_text)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = stringResource(R.string.find_text)
                    )
                },
                trailingIcon = {
                    val results = searchResults.value
                    if (results.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "${currentIndex.value + 1}/${results.size}")
                            TextButton(onClick = {
                                if (currentIndex.value + 1 >= results.size) {
                                    currentIndex.value = 0
                                } else {
                                    currentIndex.value = currentIndex.value + 1
                                }
                                action(
                                    SearchAction.ScrollTo(
                                        results[currentIndex.value],
                                        false
                                    )
                                )
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = stringResource(R.string.next_result)
                                )
                            }
                            TextButton(onClick = {
                                if (currentIndex.value - 1 < 0) {
                                    currentIndex.value = results.size - 1
                                } else {
                                    currentIndex.value = currentIndex.value - 1
                                }
                                action(
                                    SearchAction.ScrollTo(
                                        results[currentIndex.value],
                                        false
                                    )
                                )
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowUp,
                                    contentDescription = stringResource(R.string.previous_result)
                                )
                            }
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions {
                    action(SearchAction.Close)
                }
            )
        }

        LazyRow(
            contentPadding = PaddingValues(4.dp)
        ) {
            items(headers.size) { index ->
                val title = headers[index].third
                Box {
                    Button(
                        onClick = { action(SearchAction.ScrollTo(headers[index], true)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 72.dp)
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(),
                    ) {
                        Text(text = title.toString(), style = MaterialTheme.typography.body1)
                    }
                }
            }
        }
    }
}

fun search(searchValue: MutableState<String>, sections: List<WebSection>): List<ScrollToPosition> {
    if (searchValue.value.isEmpty()) {
        return emptyList()
    }
    val results: MutableList<ScrollToPosition> = mutableListOf()
    iterate(sections) { row, col, value ->
        val found = value.findAll(
            string = searchValue.value,
            ignoreCase = true
        )
        if (found.isNotEmpty()) {
            results.add(ScrollToPosition(row, col, value))
        }
    }
    return results
}

fun headers(sections: List<WebSection>): List<ScrollToPosition> {
    val headers: MutableList<ScrollToPosition> = mutableListOf()
    iterate(sections) { row, col, value ->
        val annotations = value.getStringAnnotations(AnnotationAttributes.tag, 0, 0)
        val isHeader = annotations.firstOrNull { it.item == AnnotationAttributes.header } != null
        if (isHeader) {
            headers.add(ScrollToPosition(row, col, value))
        }
    }
    return headers
}

fun iterate(
    sections: List<WebSection>,
    action: (row: Int, col: Int, value: AnnotatedString) -> Unit
) {
    var row = 0
    sections.forEach { webSection ->
        if (webSection.isHorizontal) {
            var col = 0
            webSection.list.forEach { str ->
                action(row, col, str)
                col++
            }
            row++
        } else {
            webSection.list.forEach { str ->
                action(row, 0, str)
                row++
            }
        }
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
            searchValue = mutableStateOf("rry")
        ) { }
    }
}

