package info.anodsplace.weblists.common.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Orange,
    primaryVariant = OrangeLight,
    secondary = Orange,
    secondaryVariant = OrangeDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    surface = OrangeDark,
    onSurface = Color.White,
)

private val LightColorPalette = lightColors(
    primary = Orange,
    primaryVariant = OrangeLight,
    secondary = Orange,
    secondaryVariant = OrangeDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    surface = OrangeLight,
    onSurface = Color.White,
)

@Composable
fun WebListTheme(darkTheme: Boolean = true, content: @Composable() () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}