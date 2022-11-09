package pw.janyo.whatanime.ui.preference

import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CheckboxSetting(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    icon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?
) {
    SettingsMenuLink(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        icon = icon,
        action = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        onClick = {
            if (onClick == null) {
                onCheckedChange?.invoke(!checked)
            } else {
                onClick()
            }
        },
    )
}