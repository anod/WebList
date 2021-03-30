package info.anodsplace.weblists.samples

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import info.anodsplace.weblists.rules.*
import info.anodsplace.weblists.ui.theme.Teal200

object Wikipedia {

    internal fun sample(siteId: Long): List<WebList> {
        return listOf(
            WebList(
                siteId = siteId,
                order = 0,
                cssQuery = "#mp-right",
                transformations = listOf(
                    CssTransformation(".mw-headline") {
                        TextTransformation()
                    },
                    CssTransformation("ul li") {
                        TextTransformation()
                    },
                )
            ),
        )
    }
}