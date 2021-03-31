package info.anodsplace.weblists.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import info.anodsplace.weblists.rules.AnnotationAttributes
import info.anodsplace.weblists.rules.WebSection
import info.anodsplace.weblists.rules.WebSite

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SiteSearch(site: WebSite, sections: List<WebSection>, onHeaderClick: (Triple<Int, Int, AnnotatedString>) -> Unit) {
    val headers = headers(sections)
    MainSurface(
        backgroundColor = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
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
                    leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = "Search") }
                )
            }

            LazyVerticalGrid(
                cells = GridCells.Fixed(count = 2),
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
                            Text(text = title, style = MaterialTheme.typography.body1)
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