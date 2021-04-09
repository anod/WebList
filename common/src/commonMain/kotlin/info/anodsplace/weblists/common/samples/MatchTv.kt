package info.anodsplace.weblists.common.samples

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import info.anodsplace.weblists.common.db.WebList
import info.anodsplace.weblists.common.rules.*
import info.anodsplace.weblists.common.ui.theme.Teal200

object MatchTv {

    fun sample(siteId: Long): List<WebList> {
        return listOf(
            WebList(
                siteId = siteId,
                order = 0,
                cssQuery = ".media-paging .media-paging__item_active",
                transformations = listOf(
                    CssTransformation(".media-paging__label") {
                        ConcatTransformation(
                            values = listOf(
                                ConstTransformation(
                                    "Дата:", SpanStyle(
                                        fontSize = 24f.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                ),
                                CssTransformation(".media-paging__date") {
                                    StyleTransformation(
                                        fontSize = 24f.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                CssTransformation(".media-paging__weekday")
                            ),
                            separator = " "
                        )
                    },
                )
            ),
            WebList(
                siteId = siteId,
                order = 0,
                cssQuery = ".media-paging .media-paging__item",
                isHorizontal = true,
                transformations = listOf(
                    CssTransformation(".media-paging__label") {
                        ConcatTransformation(
                            values = listOf(
                                CssTransformation(".media-paging__date") {
                                    StyleTransformation(
                                        fontSize = 24f.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                CssTransformation(".media-paging__weekday")
                            ),
                            separator = " "
                        )
                    },
                )
            ),
            WebList(
                siteId = siteId,
                order = 0,
                cssQuery = "ul.list li.tv-programm__chanels-item",
                transformations = listOf(
                    FilterTransformation(
                        "h3 .heading-3",
                        listOf("Матч ТВ", "Матч! Арена", "Матч! Игра"),
                        excludes = false
                    ),
                    CssTransformation("h3 .heading-3") {
                        StyleTransformation(
                            color = Teal200,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4f.sp,
                            annotations = listOf(AnnotationAttributes.header)
                        )
                    },
                    CssTransformation("ul.tv-programm__tvshows-list li.tv-programm__tvshows-item") {
                        ConcatTransformation(
                            values = listOf(
                                CssTransformation(".tv-programm__tvshow-time") {
                                    StyleTransformation(
                                        SpanStyle(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                },
                                CssTransformation(".tv-programm__tvshow-title")
                            )
                        )
                    },
                )
            )
        )
    }
}