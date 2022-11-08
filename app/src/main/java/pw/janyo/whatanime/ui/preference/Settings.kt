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
    onClick: () -> Unit = {},
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?
) {
    SettingsMenuLink(
        modifier = modifier,
        title = {
            Text(text = title)
        },
        subtitle = subtitle?.let {
            {
                Text(text = it)
            }
        },
        icon = icon,
        action = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        onClick = onClick,
    )
}