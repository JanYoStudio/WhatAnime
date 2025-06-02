package pw.janyo.whatanime.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
actual fun getColorScheme(): ColorScheme {
    val mode by Theme.nightMode.collectAsState()
    val isSystemInDarkTheme = isSystemInDarkTheme()
    return when (mode) {
        //强制开启夜间模式
        NightMode.ON -> DarkColorScheme
        //强制关闭夜间模式
        NightMode.OFF -> LightColorScheme

        else -> {
            if (isSystemInDarkTheme) {
                DarkColorScheme
            } else {
                LightColorScheme
            }
        }
    }
}