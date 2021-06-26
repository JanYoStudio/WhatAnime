package pw.janyo.whatanime.ui.state

import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

@Composable
fun AlertDialog(
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
) {
    val show = remember { mutableStateOf(true) }
    if (show.value) {
        androidx.compose.material.AlertDialog(
            onDismissRequest = { show.value = false },
            confirmButton,
            modifier,
            dismissButton,
            title,
            text,
            shape,
            backgroundColor,
            contentColor
        )
    }
}

@Composable
fun <T> AlertDialog(
    dialogShowState: DialogShowState<T>,
    confirmButton: @Composable DialogFunction<T>.() -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (DialogFunction<T>.() -> Unit)? = null,
    title: @Composable (DialogFunction<T>.() -> Unit)? = null,
    text: @Composable (DialogFunction<T>.() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
) {
    if (dialogShowState.value == null) {
        return
    }
    var dismiss: @Composable (() -> Unit)? = null
    var titleData: @Composable (() -> Unit)? = null
    var textData: @Composable (() -> Unit)? = null
    dismissButton?.let {
        dismiss = { it(dialogShowState) }
    }
    title?.let {
        titleData = { it(dialogShowState) }
    }
    text?.let {
        textData = { it(dialogShowState) }
    }
    androidx.compose.material.AlertDialog(
        onDismissRequest = { dialogShowState.dismiss() },
        confirmButton = { confirmButton(dialogShowState) },
        modifier = modifier,
        dismissButton = dismiss,
        title = titleData,
        text = textData,
        shape = shape,
        backgroundColor = backgroundColor,
        contentColor = contentColor
    )
}

@Composable
fun AlertDialog(
    buttons: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
) {
    val show = remember { mutableStateOf(true) }
    if (show.value) {
        androidx.compose.material.AlertDialog(
            onDismissRequest = { show.value = false },
            buttons,
            modifier,
            title,
            text,
            shape,
            backgroundColor,
            contentColor
        )
    }
}

@Composable
fun <T> AlertDialog(
    dialogShowState: DialogShowState<T>,
    buttons: @Composable DialogFunction<T>.() -> Unit,
    modifier: Modifier = Modifier,
    title: @Composable (DialogFunction<T>.() -> Unit)? = null,
    text: @Composable (DialogFunction<T>.() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
) {
    if (dialogShowState.value == null) {
        return
    }
    var titleData: @Composable (() -> Unit)? = null
    var textData: @Composable (() -> Unit)? = null
    title?.let {
        titleData = { it(dialogShowState) }
    }
    text?.let {
        textData = { it(dialogShowState) }
    }
    androidx.compose.material.AlertDialog(
        onDismissRequest = { dialogShowState.dismiss() },
        buttons = { buttons(dialogShowState) },
        modifier = modifier,
        title = titleData,
        text = textData,
        shape = shape,
        backgroundColor = backgroundColor,
        contentColor = contentColor
    )
}