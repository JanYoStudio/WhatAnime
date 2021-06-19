package pw.janyo.whatanime.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = Dark.Primary,
    primaryVariant = Dark.PrimaryVariant,
    secondary = Dark.Secondary,
    secondaryVariant = Dark.SecondaryVariant,
    surface = Dark.Surface,
    onPrimary = Dark.OnPrimary,
    onSecondary = Dark.OnSecondary
)

private val LightColorPalette = lightColors(
    primary = Light.Primary,
    primaryVariant = Light.PrimaryVariant,
    secondary = Light.Secondary,
    secondaryVariant = Light.SecondaryVariant,
    surface = Light.Surface,
    onPrimary = Light.OnPrimary,
    onSecondary = Light.OnSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes
    ) {
        content()
    }
}