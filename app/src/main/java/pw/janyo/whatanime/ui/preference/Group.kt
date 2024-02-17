package pw.janyo.whatanime.ui.preference

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface {
        Column(
            modifier = modifier.fillMaxWidth(),
        ) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(),
                thickness = 1.dp
            )
            Spacer(
                modifier = Modifier
                    .height(12.dp),
            )
            if (title != null) {
                SettingsGroupTitle(title)
            }
            content()
        }
    }
}

@Composable
internal fun SettingsGroupTitle(title: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .padding(start = 64.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val primary = MaterialTheme.colorScheme.primary
        val titleStyle = MaterialTheme.typography.titleSmall.copy(color = primary)
        ProvideTextStyle(value = titleStyle) { title() }
    }
}