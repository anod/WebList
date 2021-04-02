package info.anodsplace.weblists.ui.screen

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.NamedNavArgument
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.navigate

sealed class Screen(
    val route: String,
    val template: String = route,
    val initial: Boolean = false,
    val arguments: List<NamedNavArgument> = emptyList()
) {
    object Empty : Screen("empty")
    object Catalog : Screen("catalog", initial = true)

    class Site(siteId: Long = 0L) : Screen(
        "sites/$siteId",
        "sites/{siteId}",
        arguments = listOf(navArgument("siteId") { type = NavType.LongType })
    )

    class EditSite(siteId: Long = 0L) : Screen(
        "sites/$siteId/edit",
        "sites/{siteId}/edit",
        arguments = listOf(navArgument("siteId") { type = NavType.LongType })
    )

    class SearchSite(val siteId: Long = 0L) : Screen(
        "sites/$siteId/search",
        "sites/{siteId}/search",
        arguments = listOf(navArgument("siteId") { type = NavType.LongType })
    )

    class Error(message: String = "") : Screen(
        "error?message=$message",
        "error?message={message}",
        arguments = listOf(navArgument("message") { defaultValue = "Unexpected error" })
    )
}

fun NavGraphBuilder.composable(screen: Screen, content: @Composable (NavBackStackEntry) -> Unit) {
    composable(
        route = screen.template,
        arguments = screen.arguments,
        content = content
    )
}

fun NavController.navigate(screen: Screen) {
    navigate(screen.route) {
        if (screen.initial) {
            popUpTo = graph.startDestination
            launchSingleTop = true
        }
    }
}
