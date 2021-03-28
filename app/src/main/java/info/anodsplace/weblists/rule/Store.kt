package info.anodsplace.weblists.rule

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import info.anodsplace.weblists.toAnnotatedString
import info.anodsplace.weblists.ui.theme.Teal200

class Store {

    suspend fun load(siteId: Int): WebLists {
        val lists = listOf(
            WebList(
                id = 0,
                cssQuery = ".media-paging .media-paging__item_active",
                transformations = listOf(
                    CssResultTransformation.def(".media-paging__label") {
                        ConcatTransformation(
                            values = listOf(
                                ConstTransformation("Дата:".toAnnotatedString(SpanStyle(
                                    fontSize = 24f.sp,
                                    fontWeight = FontWeight.Bold
                                ))),
                                CssTransformation(".media-paging__date") {
                                    StyleTransformation.span(SpanStyle(
                                        fontSize = 24f.sp,
                                        fontWeight = FontWeight.Bold
                                    ))
                                },
                                CssTransformation(".media-paging__weekday")
                            ),
                            separator = " "
                        )
                    },
                )
            ),
            WebList(
                id = 1,
                cssQuery = ".media-paging .media-paging__item",
                isHorizontal = true,
                transformations = listOf(
                    CssResultTransformation.def(".media-paging__label") {
                        ConcatTransformation(
                            values = listOf(
                                CssTransformation(".media-paging__date") {
                                    StyleTransformation.span(SpanStyle(
                                        fontSize = 24f.sp,
                                        fontWeight = FontWeight.Bold
                                    ))
                                },
                                CssTransformation(".media-paging__weekday")
                            ),
                            separator = " "
                        )
                    },
                )
            ),
            WebList(
                id = 2,
                cssQuery = "ul.list li.tv-programm__chanels-item",
                transformations = listOf(
                    FilterResultTransformation.def("h3 .heading-3", listOf("Матч ТВ", "Матч! Арена", "Матч! Игра"), excludes = false),
                    CssResultTransformation.def("h3 .heading-3") {
                        StyleTransformation.span(SpanStyle(
                            color = Teal200,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4f.sp
                        ))
                    },
                    CssResultTransformation.def("ul.tv-programm__tvshows-list li.tv-programm__tvshows-item") {
                        ConcatTransformation(
                            values = listOf(
                                CssTransformation(".tv-programm__tvshow-time") {
                                    StyleTransformation.span(SpanStyle(
                                        fontWeight = FontWeight.Bold
                                    ))
                                },
                                CssTransformation(".tv-programm__tvshow-title")
                            )
                        )
                    },
                )
            )
        )

        return WebLists(
            id = siteId,
            url = "https://matchtv.ru/tvguide",
            title = "match.tv",
            lists = lists
        )
    }
}