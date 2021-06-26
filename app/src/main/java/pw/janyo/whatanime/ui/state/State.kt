package pw.janyo.whatanime.ui.state

import androidx.compose.runtime.*
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

interface DialogFunction<T> {
    var data: T?

    fun requiredData(): T

    fun dismiss()
}

class DialogShowState<T>(val initValue: T?) : State<T?>, DialogFunction<T> {
    private val state: MutableState<T?> = mutableStateOf(initValue)

    override val value: T? = state.value
    override var data: T? = state.value

    override fun requiredData(): T = state.value!!

    fun show(data: T) {
        state.value = data
    }

    override fun dismiss() {
        state.value = null
    }
}

@Composable
fun rememberDialogShowState() = rememberDialogShowState(true)

@Composable
fun <T> rememberDialogShowState(init: T?): DialogShowState<T> = remember { DialogShowState(init) }

@Composable
fun <T> LiveData<T>.observerAsShowState(owner: LifecycleOwner): DialogShowState<T> {
    val showState = rememberDialogShowState<T>(null)
    observe(owner) {
        showState.show(it)
    }
    return showState
}