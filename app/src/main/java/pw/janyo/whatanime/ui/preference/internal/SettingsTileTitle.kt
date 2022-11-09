package pw.janyo.whatanime.ui.preference.internal

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
internal fun SettingsTileTitle(title: String) {
    if (title.isBlank()) {
        return
    }
    ProvideTextStyle(value = MaterialTheme.typography.titleMedium) {
        Text(text = title, fontWeight = FontWeight.Normal)
    }
}