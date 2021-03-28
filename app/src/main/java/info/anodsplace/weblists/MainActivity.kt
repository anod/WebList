package info.anodsplace.weblists

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import info.anodsplace.weblists.ui.theme.WebListTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import info.anodsplace.weblists.rule.WebSection

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WebListTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    val state = viewModel.contentChanged.collectAsState(initial = ContentState.Loading)
                    MainScreen(state.value)
                }
            }
        }

        viewModel.load()
    }
}

@Composable
fun MainScreen(state: ContentState) {
    when (state) {
        is ContentState.Loading -> LoadingContent()
        is ContentState.Ready -> WebListContent(state.title, state.sections)
        is ContentState.Error -> ErrorContent(message = state.message)
    }
}

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WebListContent(source: String, sections: List<WebSection>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        stickyHeader {
            TopAppBar(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                title = { Text(text = source) },
                navigationIcon = {
                    Icon(
                        modifier = Modifier.padding(start = 8.dp),
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Menu")
                },
                backgroundColor = MaterialTheme.colors.primary
            )
        }

        for (section in sections) {
            if (section.isHorizontal) {
                item {
                    LazyRow {
                        items(section.list.size) { index ->
                            Text(
                                text = section.list[index],
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
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

@Composable
fun ErrorContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, textAlign = TextAlign.Center)
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun DefaultPreview() {
    WebListTheme {
        MainScreen(ContentState.Ready("Android", listOf(
            WebSection(true, listOf(AnnotatedString("Banana"), AnnotatedString("Kiwi"))),
            WebSection(false, listOf(AnnotatedString("Banana"), AnnotatedString("Kiwi")))
        )))
    }
}