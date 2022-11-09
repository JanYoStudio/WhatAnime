package pw.janyo.whatanime.ui.preference.internal

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
internal fun SettingsTileSubtitle(subtitle: String) {
    ProvideTextStyle(value = MaterialTheme.typography.bodyMedium) {
        Text(text = subtitle, fontWeight = FontWeight.Light)
    }
}