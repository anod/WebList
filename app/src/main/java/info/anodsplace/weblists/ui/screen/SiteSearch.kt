package info.anodsplace.weblists.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequesterModifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.weblists.rules.AnnotationAttributes
import info.anodsplace.weblists.rules.WebSection
import info.anodsplace.weblists.rules.WebSite
import info.anodsplace.weblists.ui.theme.WebListTheme

@Composable
fun SiteSearch(site: WebSite, sections: List<WebSection>, onHeaderClick: (Triple<Int, Int, AnnotatedString>) -> Unit) {
    val headers = headers(sections)
    MainSurface(
        backgroundColor = Color.Transparent,
        contentAlignment = Alignment.TopCenter,
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.primarySurface)
                .padding(bottom = 8.dp),
        ) {
            TopAppBar(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                backgroundColor = MaterialTheme.colors.primary
            ) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = "",
                    onValueChange = { },
                    placeholder = { Text(text = "Search for text") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search"
                        )
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        leadingIconColor = MaterialTheme.colors.onPrimary,
                        placeholderColor = MaterialTheme.colors.onPrimary,
                    )
                )
            }

            LazyRow(
                contentPadding = PaddingValues(4.dp)
            ) {
                items(headers.size) { index ->
                    val title = headers[index].third
                    Box {
                        Button(
                            onClick = { onHeaderClick(headers[index]) },
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
}

fun headers(sections: List<WebSection>): List<Triple<Int, Int, AnnotatedString>> {
    var row = 0
    val headers: MutableList<Triple<Int, Int, AnnotatedString>> = mutableListOf()
    sections.forEach { webSection ->
        if (webSection.isHorizontal) {
            var col = 0
            webSection.list.forEach { str ->
                val annotations = str.getStringAnnotations(AnnotationAttributes.tag, 0, 0)
                val isHeader = annotations.firstOrNull { it.item == AnnotationAttributes.header } != null
                if (isHeader) {
                    headers.add(Triple(row, col, str))
                }
                col++
            }
            row++
        } else {
            webSection.list.forEach { str ->
                val annotations = str.getStringAnnotations(AnnotationAttributes.tag, 0, 0)
                val isHeader = annotations.firstOrNull { it.item == AnnotationAttributes.header } != null
                if (isHeader) {
                    headers.add(Triple(row, 0, str))
                }
                row++
            }
        }
    }
    return headers
}

@Preview(showBackground = true, backgroundColor = android.graphics.Color.MAGENTA.toLong(), uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SiteSearchPreview() {
    WebListTheme {
        SiteSearch(
            WebSite(0, "url", "Android"),
            listOf(
                WebSection(true, listOf(
                    with(AnnotatedString.Builder("Banana")) {
                        AnnotationAttributes.header(this)
                        toAnnotatedString()
                    },
                    AnnotatedString("Kiwi")
                )),
                WebSection(false, listOf(AnnotatedString("Banana"), AnnotatedString("Kiwi")))
            ),
        ) { }
    }
}