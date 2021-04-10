package info.anodsplace.weblists.ui

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.NamedNavArgument
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.navigate
import info.anodsplace.weblists.common.ui.screen.Screen
import info.anodsplace.weblists.common.ui.screen.ScreenArg

fun ScreenArg.toNavArg(): NamedNavArgument {
    return navArgument(name) {
        type = when(this@toNavArg.type) {
            Long::class -> NavType.LongType
            else -> throw IllegalArgumentException("not supported")
        }
    }
}

fun NavGraphBuilder.composable(screen: Screen, content: @Composable (NavBackStackEntry) -> Unit) {
    composable(
        route = screen.template,
        arguments = screen.arguments.map { it.toNavArg() },
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