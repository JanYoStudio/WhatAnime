package pw.janyo.whatanime.ui.preference

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.zhanghai.compose.preference.PreferenceCategory
import me.zhanghai.compose.preference.ProvidePreferenceTheme
import me.zhanghai.compose.preference.preferenceTheme

@Composable
fun SettingsGroup(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ProvidePreferenceTheme(
        theme = myPreferenceTheme(),
    ) {
        PreferenceCategory(
            title = title,
        )
        Column(
            modifier = modifier.fillMaxWidth(),
            content = content,
        )
    }
}

@Composable
private fun myPreferenceTheme() = preferenceTheme(
    summaryColor = MaterialTheme.colorScheme.outline,
)