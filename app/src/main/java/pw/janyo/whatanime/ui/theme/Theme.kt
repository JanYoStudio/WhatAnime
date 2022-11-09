package pw.janyo.whatanime.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.model.entity.NightMode

private val DarkColorPalette = darkColorScheme()

private val LightColorPalette = lightColorScheme()

@Composable
fun isDarkMode(): Boolean {
    val mode by Theme.nightMode.collectAsState()
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
    val isDark = isDarkMode()
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = if (dynamicColor) {
        val context = LocalContext.current
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (isDarkMode()) DarkColorPalette else LightColorPalette
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

object Theme {
    val nightMode = MutableStateFlow(Configure.nightMode)
}