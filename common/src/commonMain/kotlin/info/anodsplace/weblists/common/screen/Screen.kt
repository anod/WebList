package info.anodsplace.weblists.common.screen

import kotlin.reflect.KClass

class ScreenArg(
    val name: String,
    val type: KClass<*>
)

sealed class Screen(
    val route: String,
    val template: String = route,
    val initial: Boolean = false,
    val arguments: List<ScreenArg> = emptyList()
) {
    object Empty : Screen("empty")
    object Catalog : Screen("catalog", initial = true)
    object Error : Screen("error")

    class Site(val siteId: Long = 0L) : Screen(
        "sites/$siteId",
        "sites/{siteId}",
        arguments = listOf(ScreenArg("siteId", type = Long::class))
    )

    class EditSite(val siteId: Long = 0L) : Screen(
        "sites/$siteId/edit",
        "sites/{siteId}/edit",
        arguments = listOf(ScreenArg("siteId", type = Long::class))
    )

    class SearchSite(val siteId: Long = 0L) : Screen(
        "sites/$siteId/search",
        "sites/{siteId}/search",
        arguments = listOf(ScreenArg("siteId", type = Long::class))
    )

    class Export(val siteId: Long = 0L, val content: String = "") : Screen(
        "sites/$siteId/export",
        "sites/{siteId}/export",
        arguments = listOf(ScreenArg("siteId", type = Long::class))
    )

    class Import(val siteId: Long = 0L) : Screen(
        "sites/import",
        "sites/import",
    )
}

