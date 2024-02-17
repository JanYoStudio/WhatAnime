package pw.janyo.whatanime.ui.preference

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.ListPreferenceType
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.TextFieldPreference

@Composable
fun CheckboxSetting(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    icon: @Composable () -> Unit = {},
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit = {},
) {
    SwitchPreference(
        value = checked,
        onValueChange = onCheckedChange,
        title = {
            Text(title)
        },
        summary = subtitle?.let {
            { Text(it) }
        },
        modifier = modifier,
        icon = icon,
    )
}

@Composable
fun <T> ListSetting(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    icon: @Composable () -> Unit = {},
    defaultValue: T,
    onValueChange: (T) -> Unit = {},
    values: List<T>,
    valueToText: (T) -> AnnotatedString = { AnnotatedString(it.toString()) },
) {
    ListPreference(
        value = defaultValue,
        onValueChange = onValueChange,
        title = {
            Text(title)
        },
        summary = subtitle?.let {
            { Text(it) }
        },
        modifier = modifier,
        icon = icon,
        values = values,
        valueToText = valueToText,
        type = ListPreferenceType.DROPDOWN_MENU,
    )
}

@Composable
fun TextSettings(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    icon: @Composable () -> Unit = {},
    defaultValue: String,
    onValueChange: (String) -> Unit = {},
) {
    TextFieldPreference(
        value = defaultValue,
        onValueChange = onValueChange,
        title = {
            Text(title)
        },
        summary = subtitle?.let {
            { Text(it) }
        },
        modifier = modifier,
        icon = icon,
        valueToText = { it },
        textToValue = { it },
    )
}

@Composable
fun SettingsMenuLink(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {},
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit = {},
) {
    Preference(
        title = { Text(text = title) },
        summary = subtitle?.let { { Text(text = it) } },
        icon = icon,
        onClick = onClick,
        modifier = modifier,
    )
}