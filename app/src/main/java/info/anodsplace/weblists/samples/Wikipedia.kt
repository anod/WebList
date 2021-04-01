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
                    CssTransformation("#In_the_news") {
                        StyleTransformation(SpanStyle(
                            color = Teal200,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4f.sp
                        ))
                    },
                    CssTransformation("#mp-itn > ul li") {
                        TextTransformation()
                    },
                )
            ),
            WebList(
                siteId = siteId,
                order = 0,
                cssQuery = "#mp-right",
                transformations = listOf(
                    CssTransformation("#On_this_day") {
                        StyleTransformation(SpanStyle(
                            color = Teal200,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4f.sp
                        ))
                    },
                    CssTransformation("#mp-otd > ul li") {
                        TextTransformation()
                    },
                )
            ),
        )
    }
}