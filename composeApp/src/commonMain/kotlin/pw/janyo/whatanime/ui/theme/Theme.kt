package pw.janyo.whatanime.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.MutableStateFlow
import pw.janyo.whatanime.Configure

val DarkColorScheme = darkColorScheme()

val LightColorScheme = lightColorScheme()

@Composable
expect fun getColorScheme(): ColorScheme

@Composable
fun WhatAnimeTheme(
    content: @Composable() () -> Unit
) {
    val colorScheme = getColorScheme()

    MaterialExpressiveTheme(
        typography = MaterialTheme.typography,
        colorScheme = colorScheme,
        content = content,
    )
}

object Theme {
    val nightMode = MutableStateFlow(Configure.nightMode)
}