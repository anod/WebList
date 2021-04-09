package info.anodsplace.weblists.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.weblists.R
import info.anodsplace.weblists.common.db.WebSite
import info.anodsplace.weblists.common.ui.theme.WebListTheme

@Composable
fun LoadingCatalog() {
    MainSurface {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CatalogContent(sites: List<WebSite>, navigate: (Screen) -> Unit) {
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
                title = { Text(text = stringResource(R.string.app_name)) },
                backgroundColor = MaterialTheme.colors.primary
            )

            LazyVerticalGrid(
                cells = GridCells.Fixed(count = 2),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(sites.size) { index ->
                    SiteButton(title = sites[index].title) { navigate(Screen.Site(sites[index].id)) }
                }

                item {
                    SiteButton(title = stringResource(R.string.add_new)) {
                        navigate(Screen.EditSite())
                    }
                }
            }
        }
    }
}

@Composable
fun SiteButton(title: String, onClick: () -> Unit) {
    Box {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 72.dp)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colors.onSurface
            ),
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

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ListPreview() {
    WebListTheme {
        CatalogContent(
            listOf(
                WebSite(0, "http://sample1", "Sample 1"),
                WebSite(1, "http://sample2", "Sample 2")
            )
        ) { }
    }
}