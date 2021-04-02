package info.anodsplace.weblists.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.anodsplace.weblists.rules.WebList
import info.anodsplace.weblists.samples.MatchTv
import info.anodsplace.weblists.ui.theme.WebListTheme

@Composable
fun EditLists(lists: List<WebList>) {
    Surface {
        LazyColumn {
            for (list in lists) {
                item {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = list.cssQuery,
                        onValueChange = {},
                        label = { Text(text = "CSS Query") }
                    )
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "Horizontal"
                        )
                        Switch(checked = list.isHorizontal, onCheckedChange = null)
                    }
                }

                items(list.transformations.list.size) { tid ->
                    val def = list.transformations.list[tid]
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = def.type,
                        style = MaterialTheme.typography.h6
                    )
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = def.parameter,
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditListsPreview() {
    WebListTheme {
        EditLists(
            MatchTv.sample(0)
        )
    }
}