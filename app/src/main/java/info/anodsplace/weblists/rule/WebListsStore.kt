package info.anodsplace.weblists.rule

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.room.Dao
import androidx.room.Query
import info.anodsplace.weblists.ui.theme.Teal200

@Dao
interface WebSiteStore {

    @Query("SELECT * FROM web_site WHERE id IN (:siteId)")
    fun loadById(siteId: Int): WebSiteLists

    companion object {
        suspend fun load(siteId: Int): WebSiteLists {
            val lists = listOf(
                WebList(
                    siteId = 1,
                    order = 0,
                    cssQuery = ".media-paging .media-paging__item_active",
                    transformations = listOf(
                        CssTransformation(".media-paging__label") {
                            ConcatTransformation(
                                values = listOf(
                                    ConstTransformation("Дата:", SpanStyle(
                                        fontSize = 24f.sp,
                                        fontWeight = FontWeight.Bold
                                    )),
                                    CssTransformation(".media-paging__date") {
                                        StyleTransformation(SpanStyle(
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
                    siteId = 1,
                    order = 1,
                    cssQuery = ".media-paging .media-paging__item",
                    isHorizontal = true,
                    transformations = listOf(
                        CssTransformation(".media-paging__label") {
                            ConcatTransformation(
                                values = listOf(
                                    CssTransformation(".media-paging__date") {
                                        StyleTransformation(SpanStyle(
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
                    siteId = 1,
                    order = 2,
                    cssQuery = "ul.list li.tv-programm__chanels-item",
                    transformations = listOf(
                        FilterTransformation("h3 .heading-3", listOf("Матч ТВ", "Матч! Арена", "Матч! Игра"), excludes = false),
                        CssTransformation("h3 .heading-3") {
                            StyleTransformation(SpanStyle(
                                color = Teal200,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 4f.sp
                            ))
                        },
                        CssTransformation("ul.tv-programm__tvshows-list li.tv-programm__tvshows-item") {
                            ConcatTransformation(
                                values = listOf(
                                    CssTransformation(".tv-programm__tvshow-time") {
                                        StyleTransformation(SpanStyle(
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

            return WebSiteLists(
                site = WebSite(
                    id = siteId,
                    url = "https://matchtv.ru/tvguide",
                    title = "match.tv"
                ),
                lists = lists
            )
        }
    }
}
