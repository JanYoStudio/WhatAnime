package pw.janyo.whatanime.ui.preference

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString

@Composable
fun CheckboxSetting(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    icon: @Composable (() -> Unit)? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit = {},
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = icon,
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        // 整行只有一个开关动作，避免点击文字与开关产生不同的行为或重复回调。
        modifier = modifier.fillMaxWidth().toggleable(
            value = checked, role = Role.Switch, onValueChange = onCheckedChange,
        ),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
fun <T> ListSetting(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    icon: @Composable (() -> Unit)? = null,
    defaultValue: T,
    onValueChange: (T) -> Unit = {},
    values: List<T>,
    valueToText: @Composable (T) -> AnnotatedString = { AnnotatedString(it.toString()) },
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = subtitle?.let { { Text(it) } },
            leadingContent = icon,
            trailingContent = { Icon(Icons.Outlined.ArrowDropDown, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().clickable(role = Role.Button) { expanded = true },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            values.forEach { value ->
                DropdownMenuItem(
                    text = { Text(valueToText(value)) },
                    modifier = Modifier.semantics { selected = value == defaultValue },
                    trailingIcon = if (value == defaultValue) {
                        { Icon(Icons.Outlined.Check, contentDescription = null) }
                    } else null,
                    onClick = { expanded = false; onValueChange(value) },
                )
            }
        }
    }
}

@Composable
fun SettingsMenuLink(
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = icon,
        // 版本等只读信息不伪装成按钮，链接与操作仍可整行点击。
        modifier = modifier.fillMaxWidth().then(
            if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier,
        ),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
