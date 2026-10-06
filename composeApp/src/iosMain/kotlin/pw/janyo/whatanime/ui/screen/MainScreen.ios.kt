package pw.janyo.whatanime.ui.screen

import androidx.compose.runtime.Composable

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.compose.ui.backhandler.BackHandler(enabled = enabled, onBack = onBack)
}