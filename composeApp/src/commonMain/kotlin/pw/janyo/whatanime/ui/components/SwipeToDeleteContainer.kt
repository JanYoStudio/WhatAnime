package pw.janyo.whatanime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.hint_swipe_to_delete

@Composable
fun <T> SwipeToDeleteContainer(item: T, enabled: Boolean = true, onDelete: (T) -> Unit, content: @Composable (T) -> Unit) {
    val state = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        state = state, enableDismissFromStartToEnd = false, gesturesEnabled = enabled,
        backgroundContent = {
            val active = state.targetValue == SwipeToDismissBoxValue.EndToStart
            Box(Modifier.fillMaxSize().background(if (active) MaterialTheme.colorScheme.errorContainer else Color.Transparent)) {
                if (active) Icon(
                    Icons.Default.Delete, stringResource(Res.string.hint_swipe_to_delete),
                    Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) { content(item) }
    LaunchedEffect(state.currentValue) {
        if (state.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete(item)
            state.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }
}
