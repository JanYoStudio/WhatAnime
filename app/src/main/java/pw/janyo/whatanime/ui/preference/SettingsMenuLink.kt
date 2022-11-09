package pw.janyo.whatanime.ui.preference

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pw.janyo.whatanime.ui.preference.internal.SettingsTileAction
import pw.janyo.whatanime.ui.preference.internal.SettingsTileIcon
import pw.janyo.whatanime.ui.preference.internal.SettingsTileTexts


@Composable
fun SettingsMenuLink(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    Surface {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        ) {
            Row(
                modifier = Modifier
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SettingsTileIcon(icon = icon)
                SettingsTileTexts(title = title, subtitle = subtitle)
            }
            if (action != null) {
                SettingsTileAction {
                    action.invoke()
                }
            } else {
                Spacer(modifier = Modifier.width(24.dp))
            }
        }
    }
}