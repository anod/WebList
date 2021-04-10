package info.anodsplace.weblists.common.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import info.anodsplace.weblists.common.StringProvider

@Composable
fun MainSurface(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    backgroundColor: Color = MaterialTheme.colors.background,
    elevation: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Surface(
        color = backgroundColor,
        elevation = elevation,
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(),
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
fun EmptyContent(strings: StringProvider) {
    MainSurface(
        modifier = Modifier.padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = strings.noListsFound,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
            Button(onClick = { }) {
                Text(text = strings.addNew, textAlign = TextAlign.Center)
            }
        }
    }
}

