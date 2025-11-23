package pw.janyo.whatanime.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun <T> SwipeToDeleteContainer(
    item: T,
    onDelete: (T) -> Unit,
    content: @Composable (T) -> Unit
) {
    val state = rememberSwipeToDismissBoxState()

    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            val color by animateColorAsState(
                when (state.targetValue) {
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                    SwipeToDismissBoxValue.StartToEnd -> Color.Green
                    SwipeToDismissBoxValue.EndToStart -> Color.Red
                }
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(color)
            ) {
                when (state.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> {
                        Icon(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 16.dp),
                            imageVector = Icons.Default.Delete,
                            contentDescription = "delete",
                            tint = Color.White,
                        )
                    }

                    else -> {
                        // Nothing to do
                    }
                }
            }
        },
        enableDismissFromStartToEnd = false,
    ) { content(item) }

    when (state.currentValue) {
        SwipeToDismissBoxValue.EndToStart -> {
            LaunchedEffect(state.currentValue) {
                onDelete(item)
                state.snapTo(SwipeToDismissBoxValue.Settled)
            }

        }

        else -> {
            // Nothing to do
        }
    }
}