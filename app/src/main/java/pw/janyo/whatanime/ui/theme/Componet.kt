package pw.janyo.whatanime.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.LiveData

@Composable
fun <T> LiveData<T>.observeValueAsState(): State<T> = observeAsState(value!!)