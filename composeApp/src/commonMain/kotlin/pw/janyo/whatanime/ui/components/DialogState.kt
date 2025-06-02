package pw.janyo.whatanime.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

class ShowDialogState(
    initialValue: Boolean,
) {
    internal var show by mutableStateOf(initialValue)

    val isShowing: Boolean
        get() = show

    fun show() {
        show = true
    }

    fun hide() {
        show = false
    }

    companion object {
        fun Saver() = androidx.compose.runtime.saveable.Saver<ShowDialogState, Boolean>(
            save = { it.show },
            restore = { ShowDialogState(it) }
        )
    }
}

@Composable
fun rememberShowDialogState(
    initialValue: Boolean = false,
): ShowDialogState {
    return rememberSaveable(
        saver = ShowDialogState.Saver()
    ) {
        ShowDialogState(initialValue)
    }
}