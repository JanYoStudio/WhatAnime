package pw.janyo.whatanime.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import whatanime.composeapp.generated.resources.*

@Composable
fun HistoryEmptyState() {
    Column(
        Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painterResource(if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) {
                Res.drawable.illustration_history_empty_dark
            } else Res.drawable.illustration_history_empty),
            contentDescription = null,
            modifier = Modifier.widthIn(max = 240.dp).fillMaxWidth().aspectRatio(4f / 3f),
        )
        Text(stringResource(Res.string.ui_no_history), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            stringResource(Res.string.ui_history_empty_hint),
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
