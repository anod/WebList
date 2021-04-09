package info.anodsplace.weblists.common.samples

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import info.anodsplace.weblists.common.db.WebList
import info.anodsplace.weblists.common.rules.*
import info.anodsplace.weblists.common.ui.theme.Teal200

object Wikipedia {

    fun sample(siteId: Long): List<WebList> {
        return listOf(
            WebList(
                siteId = siteId,
                order = 0,
                cssQuery = "#mp-right",
                transformations = listOf(
                    CssTransformation("#In_the_news") {
                        StyleTransformation(
                            color = Teal200,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4f.sp,
                            annotations = listOf(AnnotationAttributes.header)
                        )
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
                        StyleTransformation(
                            color = Teal200,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4f.sp,
                            annotations = listOf(AnnotationAttributes.header)
                        )
                    },
                    CssTransformation("#mp-otd > ul li") {
                        TextTransformation()
                    },
                )
            ),
        )
    }
}