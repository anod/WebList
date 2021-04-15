package info.anodsplace.weblists.common.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Blue,
    primaryVariant = BlueLight,
    secondary = Orange,
    secondaryVariant = OrangeDark,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    surface = BlueDark,
    onSurface = Color.White,
)

private val LightColorPalette = lightColors(
    primary = Blue,
    primaryVariant = BlueLight,
    secondary = Orange,
    secondaryVariant = OrangeDark,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    surface = BlueLight,
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