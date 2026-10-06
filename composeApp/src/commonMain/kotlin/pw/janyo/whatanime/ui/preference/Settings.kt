package pw.janyo.whatanime.ui.preference

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.action_ok
import whatanime.composeapp.generated.resources.action_cancel
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
    var pending by remember { mutableStateOf(defaultValue) }
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val stacked = maxWidth < 320.dp && LocalDensity.current.fontScale >= 1.5f
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = if (stacked) subtitle?.let { { Text(it) } } else null,
            leadingContent = icon,
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!stacked && subtitle != null) Text(
                        subtitle, Modifier.widthIn(max = 144.dp),
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                    )
                    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth().clickable(role = Role.Button) {
                pending = defaultValue
                expanded = true
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
    if (expanded) {
        AlertDialog(
            onDismissRequest = { expanded = false },
            title = { Text(title) },
            text = {
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).selectableGroup()) {
                    values.forEach { value ->
                        Row(
                            modifier = Modifier.fillMaxWidth().selectable(
                                selected = value == pending, role = Role.RadioButton,
                                onClick = { pending = value },
                            ).padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = value == pending, onClick = null)
                            Text(valueToText(value), Modifier.padding(start = 16.dp))
                        }
                    }
                }
            },
            confirmButton = { TextButton(enabled = pending in values, onClick = {
                onValueChange(pending)
                expanded = false
            }) { Text(stringResource(Res.string.action_ok)) } },
            dismissButton = { TextButton(onClick = { expanded = false }) { Text(stringResource(Res.string.action_cancel)) } },
        )
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
