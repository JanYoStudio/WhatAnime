package pw.janyo.whatanime.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.model.entity.NightMode

private val DarkColorPalette = darkColorScheme()

private val LightColorPalette = lightColorScheme()

@Composable
fun isDarkMode(): Boolean {
    val mode by Theme.nightMode.collectAsState()
//    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    return when (mode) {
        NightMode.AUTO -> isSystemInDarkTheme()
        NightMode.ON -> true
        NightMode.OFF -> false
        NightMode.MATERIAL_YOU -> isSystemInDarkTheme()
    }
}

@Composable
fun WhatAnimeTheme(
    content: @Composable() () -> Unit
) {
    val colorScheme = if (isDarkMode()) DarkColorPalette else LightColorPalette
//    val colorScheme = LightColorPalette

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

object Theme {
    val nightMode = MutableStateFlow(Configure.nightMode)
}